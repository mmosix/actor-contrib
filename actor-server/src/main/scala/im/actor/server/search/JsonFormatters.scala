package im.actor.server.search

import im.actor.server.model.{ PeerType, Peer }
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.Message
import models.ContentTypes.ContentType

trait JsonFormatters {

  implicit val messageReads: Reads[Message] =
    ((JsPath \ "randomId").read[Long] and
      (JsPath \ "ts").read[Long] and
      (JsPath \ "senderId").read[Int] and
      (for {
        peerType ← (JsPath \ "peerType").read[Int]
        peerId ← (JsPath \ "peerId").read[Int]
      } yield Peer(PeerType.fromValue(peerType), peerId)) and
      ((JsPath \ "contentType").read[Int] map ContentType.fromInt) and
      (JsPath \ "content").read[String] and
      (JsPath \ "isPublic").read[Boolean] and
      (JsPath \ "users").read[Set[Int]])(Message)

  implicit val messageWrites = new Writes[Message] {
    def writes(m: Message): JsValue = Json.obj(
      "randomId" → m.randomId,
      "ts" → m.ts,
      "senderId" → m.senderId,
      "peerType" → m.peer.typ.value,
      "peerId" → m.peer.id,
      "contentType" → ContentType.toInt(m.contentType),
      "content" → m.content,
      "isPublic" → m.isPublic,
      "users" → m.users
    )
  }

}