package im.actor.server.search

import akka.actor.ActorSystem
import akka.event.Logging
import cats.data.Xor
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.BoolQueryDefinition
import im.actor.api.rpc.messaging.{ ApiTextMessage, ApiMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.search._
import im.actor.api.rpc.PeersImplicits
import im.actor.server.group.GroupUtils
import im.actor.server.model.PeerType._
import im.actor.server.model.Peer
import im.actor.server.persist.HistoryMessageRepo
import im.actor.util.log.AnyRefLogSource
import org.elasticsearch.search.sort.SortOrder.DESC
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

trait SearchConditionHelpers extends PeersImplicits with MessageParsing with AnyRefLogSource {
  import models._

  implicit val system: ActorSystem
  protected implicit val ec: ExecutionContext
  protected val db: Database

  val log = Logging(system, this)

  def messageSearchResult(cond: ApiSearchCondition, clientId: Int, clientAuthId: Long): Future[ResponseMessageSearchResponse] = {
    val searchExt = SearchExtension(system)
    val messageQuery =
      bool(
        must(
          searchConditionQ(cond, clientId),
          should(
            termQuery("users", clientId),
            termQuery("isPublic", true)
          )
        )
      )
    val searchQuery =
      search in searchExt.indexName / "messages" query messageQuery sort (field sort "ts" order DESC) from 0 size 30
    log.debug("Search query: {}", searchQuery)
    for {
      searchResp ← searchExt.client execute { searchQuery }
      _ = log.debug("Search request: {}, response: {}", cond, searchResp)
      searchItems ← Future.sequence(searchResp.as[Message].toVector map { mess ⇒
        log.debug("Parsed messages from search: {}", mess)
        db.run(HistoryMessageRepo.findBySender(mess.senderId, mess.peer, mess.randomId).headOption) map { optHm ⇒
          for {
            hm ← optHm
            message ← Xor.fromEither(parseMessage(hm.messageContentData)).toOption
          } yield ApiMessageSearchItem(
            ApiMessageSearchResult(
              peer = mess.peer.asStruct,
              randomId = mess.randomId,
              date = mess.ts,
              senderId = mess.senderId,
              content = adjustContent(message, mess.content)
            )
          )
        }
      })
      found = searchItems.flatten
      (groupIds, userIds) = found.view.map(_.result.peer).foldLeft(Vector.empty[Int], Vector.empty[Int]) {
        case ((gids, uids), ApiPeer(pt, pid)) ⇒
          pt match {
            case ApiPeerType.Private ⇒ (gids, uids :+ pid)
            case ApiPeerType.Group ⇒ (gids :+ pid, uids)
          }
      }
      (groups, users) ← GroupUtils.getGroupsUsers(groupIds, userIds, clientId, clientAuthId)
    } yield ResponseMessageSearchResponse(searchItems.flatten, users.toVector, groups.toVector, None)
  }

  //for text messages we may store different content in index(in case of links)
  private def adjustContent(message: ApiMessage, content: String): ApiMessage = message match {
    case m: ApiTextMessage ⇒ m.copy(text = content)
    case _ ⇒ message
  }

  private def searchConditionQ(searchCondition: ApiSearchCondition, clientId: Int): BoolQueryDefinition = searchCondition match {
    case ApiSearchPeerCondition(apiPeer) ⇒
      val peer = apiPeer.asModel
      peer match {
        case Peer(Group, groupId) ⇒
          must(
            termQuery("peerType", Group.value),
            termQuery("peerId", peer.id)
          )
        case Peer(Private, peerUserId) ⇒
          should(
            must(
              termQuery("peerType", Private.value),
              termQuery("peerId", peerUserId),
              termQuery("users", clientId)
            ),
            must(
              termQuery("peerType", Private.value),
              termQuery("peerId", clientId),
              termQuery("users", peerUserId)
            )
          )
      }
    case ApiSearchPieceText(piece) ⇒
      //      must(matchQuery("content", piece) minimumShouldMatch ("75%"))
      must((piece split " ") map { txt ⇒
        should(prefixQuery("content", txt))
      })
    case ApiSearchSenderIdConfition(senderId) ⇒
      must(termQuery("senderId", senderId))
    case ApiSearchPeerContentType(ct) ⇒
      //todo: how to properly synchronize with ContentType ids in models
      must(termQuery("contentType", ct.id))
    case ApiSearchAndCondition(queries) ⇒
      must(queries map (q ⇒ searchConditionQ(q, clientId)))
    case ApiSearchOrCondition(queries) ⇒
      should(queries map (q ⇒ searchConditionQ(q, clientId)))
    case ApiSearchPeerTypeCondition(_) ⇒ should() //dummy to make pattern match exhaustive
  }

}
