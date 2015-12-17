package im.actor.server.search

import com.sksamuel.elastic4s.{ RichSearchHit, HitAs }
import com.sksamuel.elastic4s.source.Indexable
import im.actor.api.rpc.messaging.{ ApiDocumentExVoice, ApiDocumentExPhoto, ApiDocumentExVideo, ApiDocumentEx }
import im.actor.server.model._
import play.api.libs.json.Json

object models extends JsonFormatters {

  object ContentTypes {
    object ContentType {
      def toInt: PartialFunction[ContentType, Int] = {
        case Any ⇒ 1
        case Text ⇒ 2
        case Links ⇒ 3
        case Documents ⇒ 4
        case Photos ⇒ 5
      }

      def fromInt: PartialFunction[Int, ContentType] = {
        case 1 ⇒ Any
        case 2 ⇒ Text
        case 3 ⇒ Links
        case 4 ⇒ Documents
        case 5 ⇒ Photos
      }

      def getByDocumentEx: PartialFunction[ApiDocumentEx, ContentType] = {
        case _: ApiDocumentExPhoto ⇒ ContentTypes.Photos
        case _: ApiDocumentExVideo | _: ApiDocumentExVoice ⇒ ContentTypes.Documents
      }
    }

    sealed trait ContentType

    case object Any extends ContentType
    case object Text extends ContentType
    case object Links extends ContentType
    case object Documents extends ContentType
    case object Photos extends ContentType
  }
  import ContentTypes._

  final case class Message(
    randomId: Long,
    ts: Long,
    senderId: Int,
    peer: Peer,
    contentType: ContentType,
    content: String,
    isPublic: Boolean,
    users: Set[Int]
  )

  implicit object MessageIndexable extends Indexable[Message] {
    override def json(m: Message): String = Json.stringify(Json.toJson(m))
  }

  implicit object MessageHitAs extends HitAs[Message] {
    override def as(hit: RichSearchHit): Message = {
      Json.parse(hit.sourceAsString).validate[Message].fold(
        err ⇒ throw new Exception("Failed to parse response from elasticsearch, cause: " + err),
        mess ⇒ mess
      )
    }
  }

}