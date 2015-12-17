package im.actor.server.api.rpc.service.invites

import java.nio.file.{ Paths, Files }
import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.api.rpc.teams.ApiOutTeam
import im.actor.api.rpc.invites.{ InvitesService, ResponseInviteList }
import im.actor.api.rpc._
import im.actor.server.acl.ACLUtils
import im.actor.server.config.InvitesConfig
import im.actor.server.db.DbExtension
import im.actor.server.email._
import im.actor.server.model.InviteState
import im.actor.server.persist
import im.actor.server.persist.contact.UnregisteredEmailContactRepo
import im.actor.server.persist.{ InviteStateRepo, UserRepo }
import im.actor.server.user.UserExtension
import slick.dbio.DBIO

import scala.concurrent.{ ExecutionContext, Future }

object InvitesServiceErrors {
  val AlreadyRegistered = RpcError(400, "USER_ALREADY_REGISTERED", "User with this email already registered in Actor network", false, None)
  val InternalError = RpcError(500, "INTERNAL_ERROR", "", true, None)
}

case class InviteTemplate(private val config: InvitesConfig) {
  private final val template: String = new String(Files.readAllBytes(Paths.get(config.inviteTemplate)))
  //TODO: rewrite from replace to faster version
  def format(name: Option[String], inviterName: String, inviteLink: String): String =
    template
      .replace("$$INVITER$$", inviterName)
      .replace("$$JOIN_LINK$$", inviteLink)
      .replace("$$NAME$$", name getOrElse "")
}

final class InvitesServiceImpl(implicit val system: ActorSystem) extends InvitesService with ImplicitConversions {

  import FutureResultRpcCats._
  import InvitesServiceErrors._

  override protected implicit val ec: ExecutionContext = system.dispatcher

  private val db = DbExtension(system).db
  private val userExt = UserExtension(system)
  private val emailSender = new SmtpEmailSender(EmailConfig.load.get)

  private val invitesConfig = InvitesConfig.load.get
  private val template = InviteTemplate(invitesConfig)
  private val InviteLink = "https://corp.actor.im"

  override def jhandleLoadOwnSentInvites(clientData: ClientData): Future[HandlerResult[ResponseInviteList]] =
    authorized(clientData) { client => loadInvites(client.userId, client.authId) map (_.toScalaz) }

  override def jhandleSendInvite(email: String, name: Option[String], destTeam: Option[ApiOutTeam], clientData: ClientData): Future[HandlerResult[ResponseInviteList]] =
    authorized(clientData) { implicit client =>
      (for {
        optUserId <- fromFuture(db.run(UserRepo.findIdsByEmail(email)))
        _ <- fromBoolean(AlreadyRegistered)(optUserId.isEmpty)
        message <- fromFuture(makeInviteMessage(email, name, InviteLink))
        _ <- fromFuture(emailSender send message)
        _ <- fromFuture(db.run(UnregisteredEmailContactRepo.createIfNotExists(email, client.userId, name)))
        invite = InviteState(ACLUtils.randomLong(), client.userId, email, name, None, createdAt = LocalDateTime.now(ZoneOffset.UTC), isAccepted = false)
        exists <- fromFuture(db.run(InviteStateRepo.exists(client.userId, email, destTeam map (_.id))))
        _ <- if (exists) point(0) else fromFuture(db.run(InviteStateRepo.create(invite)))
        invites <- fromFutureEither[ResponseInviteList, RpcError](_ => InternalError)(loadInvites(client.userId, client.authId))
      } yield invites).value map (_.toScalaz)
    }

  private def makeInviteMessage(email: String, name: Option[String], inviteLink: String)(implicit client: AuthorizedClientData): Future[Message] =
    for {
      inviterName <- userExt.getApiStruct(client.userId, client.userId, client.authId) map (_.name)
      messageText = template.format(name, inviterName, inviteLink)
    } yield Message(
      to = email,
      subject = invitesConfig.inviteSubject,
      content = Content(Some(messageText), None)
    )

  private def loadInvites(userId: Int, authId: Long): Future[RpcError Xor ResponseInviteList] = {
    val inviteResp = for {
      invites <- InviteStateRepo.findByInviter(userId)
      actions = invites map { invite =>
        persist.UserRepo.findIdsByEmail(invite.inviteeEmail)
      }
      optUserIds <- DBIO.sequence(actions)
      userIds = optUserIds.flatten
      users ← DBIO.from(Future.sequence(userIds map (id ⇒ userExt.getApiStruct(id, userId, authId))))
    } yield Xor.Right(ResponseInviteList(invites.toVector, users.toVector, relatedGroups = Vector.empty, relatedTeams = Vector.empty))
    db.run(inviteResp)
  }
}
