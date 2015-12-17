package im.actor.server.persist

import java.time.LocalDateTime

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.InviteState

class InviteStateTable(tag: Tag) extends Table[InviteState](tag, "invite_states") {
  def id = column[Long]("id", O.PrimaryKey)
  def inviterUserId = column[Int]("inviter_user_id")
  def inviteeEmail = column[String]("invitee_email")
  def inviteeName = column[Option[String]]("invitee_name")
  def teamId = column[Option[Int]]("team_id")
  def createdAt = column[LocalDateTime]("created_at")
  def isAccepted = column[Boolean]("is_accepted")

  def * = (id, inviterUserId, inviteeEmail, inviteeName, teamId, createdAt, isAccepted) <> (InviteState.tupled, InviteState.unapply)
}

object InviteStateRepo {
  val inviteStates = TableQuery[InviteStateTable]

  def create(invite: InviteState): DBIO[Int] = inviteStates += invite

  def findByInviter(userId: Int): DBIO[Seq[InviteState]] = inviteStates.filter(_.inviterUserId === userId).result

  def exists(userId: Int, email: String, teamId: Option[Int]): DBIO[Boolean] =
    inviteStates.filter(i => i.inviterUserId === userId && i.teamId === teamId && i.inviteeEmail === email).exists.result

}
