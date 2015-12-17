package im.actor.server.enterprise

import akka.actor._
import im.actor.api.rpc.messaging.{ ApiJsonMessage, UpdateMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.db.DbExtension
import im.actor.server.persist.UserRepo
import im.actor.server.user.UserExtension
import im.actor.server.user.UserHook.AfterAuthHook
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

object AuthBotNotifier extends ExtensionId[AuthBotNotifier] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): AuthBotNotifier = new AuthBotNotifier(system)

  override def lookup(): ExtensionId[_ <: Extension] = AuthBotNotifier
}

final class AuthBotNotifier(system: ActorSystem) extends Extension {
  UserExtension(system).hooks.afterAuth.register(AuthBotNotifierHook.Name, new AuthBotNotifierHook(system))
}

object AuthBotNotifierHook {
  val Name = "EnterpriseAuthBotNotifier"
}

private final class AuthBotNotifierHook(system: ActorSystem) extends AfterAuthHook(system) {
  import system.dispatcher

  private val EnterpriseBotNickname = "enterprise"

  private val userExt = UserExtension(system)
  private val db = DbExtension(system).db

  override def run(userId: Int, appId: Int, deviceTitle: String): Future[Unit] = {
    db.run(UserRepo.findByNickname(EnterpriseBotNickname)) flatMap {
      case Some(botUserId) ⇒
        userExt.broadcastUserUpdate(
          userId = botUserId.id,
          update = UpdateMessage(
            peer = ApiPeer(ApiPeerType.Private, userId),
            userId,
            (new DateTime).getMillis,
            ThreadLocalRandom.current().nextLong(),
            ApiJsonMessage(s"""{dataType:"UserAuth",data:{userId:$userId,appId:$appId,deviceTitle:"${deviceTitle.replace("\"", "\\\"")}"}}""")
          ),
          pushText = None,
          isFat = true,
          deliveryId = Some(s"auth_notifier_hook_$userId")
        ) map (_ ⇒ ())
      case None ⇒ Future.successful(())
    }
  }
}