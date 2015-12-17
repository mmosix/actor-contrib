package im.actor.server.user

import akka.actor._
import akka.event.Logging
import im.actor.server.db.DbExtension
import im.actor.server.model.UserEmail
import im.actor.server.persist.UserEmailRepo
import im.actor.server.persist.contact.UnregisteredEmailContactRepo
import im.actor.server.user.UserHook.BeforeEmailContactRegisteredHook
import slick.dbio.DBIO

import scala.collection.JavaConversions._
import scala.concurrent.Future

object RegistrationNotifier extends ExtensionId[RegistrationNotifier] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): RegistrationNotifier = new RegistrationNotifier(system)

  override def lookup(): ExtensionId[_ <: Extension] = RegistrationNotifier
}

final class RegistrationNotifier(system: ActorSystem) extends Extension {
  UserExtension(system).hooks.beforeEmailContactRegistered.register(
    name = RegistrationNotifierHook.Name,
    hook = new RegistrationNotifierHook(system)
  )
}

private object RegistrationNotifierHook {
  val Name = "EnterpriseEmailNotifier"
}

private final class RegistrationNotifierHook(system: ActorSystem) extends BeforeEmailContactRegisteredHook(system) {

  import system.dispatcher

  private val db = DbExtension(system).db
  private val publicDomains = system.settings.config.getStringList("services.email.public-domains").toSet
  private val log = Logging.getLogger(system, this)
  private val userExt = UserExtension(system)

  override def run(userId: Int, email: String): Future[Unit] = {
    email.split('@').toList match {
      case _ :: domain :: Nil ⇒
        if (!publicDomains.contains(domain)) {
          log.info("Searching for emails on domain: {}", domain)
          db.run(for {
            ues ← UserEmailRepo.findByDomain(domain) map (_.filterNot(_.userId == userId))
            _ ← DBIO.sequence(ues map (ue ⇒ addToUnregContacts(email, ue)))
            _ ← DBIO.from(userExt.addContacts(userId, ues map (ue ⇒ UserCommands.ContactToAdd(ue.userId, None, None, Some(ue.email)))))
          } yield log.info("Found emails: {}", ues))
        } else {
          log.info("Not public email: {}, skipping", email)
          Future.successful(())
        }
      case _ ⇒ Future.failed(new RuntimeException(s"Wrong email format $email"))
    }
  }

  private def addToUnregContacts(email: String, userEmail: UserEmail): DBIO[Unit] =
    UnregisteredEmailContactRepo.createIfNotExists(email, userEmail.userId, None) map (_ ⇒ ())
}