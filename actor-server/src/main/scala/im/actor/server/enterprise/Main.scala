package im.actor.server.enterprise

import com.typesafe.config.ConfigFactory
import im.actor.server.ActorServer
import im.actor.server.search.SearchExtension
import im.actor.server.user.RegistrationNotifier

object Main extends App with HttpRoutes {
  val modulesConfig = ConfigFactory.parseResources("modules.conf")
  val startedServer =
    ActorServer
      .newBuilder
      .withDefaultConfig(modulesConfig)
      .withHttpRoutes(buildHttpRoutes)
      .start()
  val system = startedServer.system

  system.log.debug("Starting RegistrationNotifier")
  RegistrationNotifier(system)

  system.log.debug("Starting AuthBotNotifier")
  AuthBotNotifier(system)

  system.log.debug("Starting SearchExtension and indexer")
  SearchExtension(system).continueIndexing()
}