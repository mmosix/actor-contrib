package im.actor.server.enterprise

import java.security.MessageDigest

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import im.actor.server.db.DbExtension
import im.actor.server.persist.AuthIdRepo
import im.actor.server.user.UserExtension
import scodec.bits.BitVector

import scala.concurrent.Future

private[enterprise] trait HttpRoutes {
  protected def buildHttpRoutes(system: ActorSystem): Seq[Route] = {
    Seq(authRoute(system))
  }

  private def authRoute(system: ActorSystem): Route =
    path("v1" / "users" / "auth") {
      authenticateBasicAsync("Actor", authenticator(system)) { userId =>
        complete("ok")
      }
    }

  private def authenticator(system: ActorSystem)(creds: Credentials): Future[Option[Int]] = {
    import system.dispatcher

    val log = Logging(system, getClass)

    creds match {
      case p @ Credentials.Provided(id) =>
        UserExtension(system).findUserIds(id) map (_.toList) flatMap {
          case userId :: _ =>
            DbExtension(system).db.run(AuthIdRepo.findIdByUserId(userId)) flatMap { authIds =>
              log.warning(s"Checking user {}, authIds: {}, provided id: {}", userId, authIds, id)

              if (authIds.exists(authId => p.verify(sha1hex(authId.toString))))
                Future.successful(Some(userId))
              else
                Future.successful(None)
            }
          case Nil =>
            log.warning("User not found for id: {}", id)
            Future.successful(None)
        }
      case Credentials.Missing =>
        Future.successful(None)
    }
  }

  private def sha1hex(str: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    BitVector(md.digest(str.getBytes())).toHex
  }
}
