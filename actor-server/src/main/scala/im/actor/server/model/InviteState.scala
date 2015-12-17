package im.actor.server.model

import java.time.LocalDateTime

final case class InviteState(
  id: Long,
  inviterUserId: Int,
  inviteeEmail: String,
  inviteeName: Option[String],
  teamId: Option[Int],
  createdAt: LocalDateTime,
  isAccepted: Boolean
)

