package im.actor.server.search

import java.util.concurrent.ThreadLocalRandom

import akka.stream.scaladsl.Source
import cats.data.Xor
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType.{ StringType, BooleanType, IntegerType, LongType }
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging.{ ApiDocumentMessage, ApiTextMessage }
import im.actor.server.dialog.HistoryUtils
import im.actor.server.model.{ HistoryMessage, Peer, PeerType }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.search.models.ContentTypes.ContentType
import im.actor.server.search.models.{ ContentTypes, Message }
import org.elasticsearch.action.bulk.BulkResponse
import slick.driver.PostgresDriver.api._
import slick.jdbc.{ ResultSetConcurrency, ResultSetType }

import scala.concurrent.Future

trait IndexerHandlers extends PeersImplicits with MessageParsing with LinksParsing {
  this: Indexer ⇒

  protected def createIndex(): Future[Unit] = {
    for {
      exists ← client.execute { index exists indexName } map (_.isExists)
      _ ← if (!exists) {
        client.execute {
          create index indexName shards 4 mappings (
            "messages" as (
              "randomId" typed LongType,
              "ts" typed LongType,
              "senderId" typed IntegerType,
              "peerType" typed IntegerType,
              "peerId" typed IntegerType,
              "contentType" typed IntegerType,
              "content" typed StringType,
              "isPublic" typed BooleanType,
              "users" typed IntegerType
            )
          )
        } map (_.isAcknowledged)
      } else Future.successful(())
    } yield ()
  }

  def runIndex(fromTs: Long, fromRandomId: Long, limit: Int): Future[(Long, Long)] = {
    log.debug("Run index with ts: {}, randomId: {}", fromTs, fromRandomId)
    val query = HistoryMessageRepo.uniqueAsc(fromTs, limit)
      .withStatementParameters(
        rsType = ResultSetType.ForwardOnly,
        rsConcurrency = ResultSetConcurrency.ReadOnly
      ).transactionally
    val fromDb = db.run(query)
    fromDb foreach { ms ⇒ log.debug("Got {} messages to index from database", ms.size) }
    val toIndex =
      Source(fromDb).mapConcat(identity)
        .mapAsync(Paralellism) {
          case HistoryMessage(userId, peer, date, senderUserId, randomId, _, messageData, _) ⇒
            val infoFu = peer match {
              case Peer(PeerType.Group, groupId) ⇒
                val isPublic = userId == HistoryUtils.SharedUserId
                for {
                  users ← if (isPublic) {
                    Future.successful(Set.empty[Int])
                  } else {
                    db.run(HistoryMessageRepo.findUserIds(peer, Set(randomId))) map (_.toSet)
                  }
                } yield isPublic → users
              case Peer(PeerType.Private, peerId) ⇒ Future.successful(false → Set(peerId, userId))
            }
            infoFu map {
              case (isPublic, users) ⇒
                Xor.fromEither(parseMessage(messageData)) match {
                  case Xor.Right(message) ⇒
                    message match {
                      case ApiTextMessage(text, _, _) ⇒
                        val textMess = Message(randomId, date.getMillis, senderUserId, peer, ContentTypes.Text, text, isPublic, users)
                        textMess :: (extractLinks(text) map { link ⇒
                          log.debug("Parsed link: {}", link)
                          Message(randomId, date.getMillis, senderUserId, peer, ContentTypes.Links, link, isPublic, users)
                        })
                      case ApiDocumentMessage(_, _, _, name, _, _, docExt) ⇒
                        val cType = (docExt map ContentType.getByDocumentEx) getOrElse ContentTypes.Documents
                        List(Message(randomId, date.getMillis, senderUserId, peer, cType, name, isPublic, users))
                      case _ ⇒ List.empty[Message]
                    }
                  case Xor.Left(_) ⇒
                    log.debug("Failed to parse message {}", randomId)
                    List.empty[Message]
                }
            }
        }
        .runFold(List.empty[Message])(_ ::: _)

    for {
      messages ← toIndex
      bulkResponse ← toIndexBulk(messages)
      _ = log.debug("{} indexed, result: {}", messages.length, bulkResponse)
    } yield messages.lastOption map (m ⇒ m.ts → m.randomId) getOrElse state.ts → state.rId
  }

  private def toIndexBulk(messages: Seq[Message]): Future[Option[BulkResponse]] = {
    if (messages.isEmpty) {
      Future.successful(None)
    } else {
      val queries = messages map { message ⇒
        index into indexName / "messages" source message
      }
      client.execute { bulk(queries) } map { resp ⇒ Some(resp) }
    }
  }

}
