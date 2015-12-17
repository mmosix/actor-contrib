package im.actor.server.config

import com.typesafe.config.Config
import com.github.kxbmap.configs._
import im.actor.config.ActorConfig

import scala.util.Try

object InvitesConfig {
  def load(config: Config): Try[InvitesConfig] =
    for {
      inviteTemplate ← config.get[Try[String]]("invite-template")
      inviteSubject ← config.get[Try[String]]("invite-subject")
    } yield InvitesConfig(inviteTemplate, inviteSubject)

  def load: Try[InvitesConfig] = load(ActorConfig.load().getConfig("services.invites"))
}

case class InvitesConfig(inviteTemplate: String, inviteSubject: String)