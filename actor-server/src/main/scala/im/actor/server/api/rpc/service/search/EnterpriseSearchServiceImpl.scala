package im.actor.server.api.rpc.service.search

import akka.actor.ActorSystem
import im.actor.api.rpc._
import im.actor.api.rpc.search._
import im.actor.server.search.SearchConditionHelpers

import scala.concurrent.Future

final class EnterpriseSearchServiceImpl(implicit val system: ActorSystem) extends SearchServiceImpl with SearchConditionHelpers {
  override def jhandleMessageSearch(query: ApiSearchCondition, clientData: ClientData): Future[HandlerResult[ResponseMessageSearchResponse]] = {
    authorized(clientData) { implicit client ⇒
      messageSearchResult(query, client.userId, client.authId) map (res ⇒ Ok(res))
    }
  }
}
