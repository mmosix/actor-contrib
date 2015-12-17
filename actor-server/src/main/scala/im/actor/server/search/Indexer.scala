package im.actor.server.search

import java.time.Instant

import akka.actor.Status.Failure
import akka.actor.{ ActorRef, ActorSystem, PoisonPill, Props }
import akka.cluster.singleton.{ ClusterSingletonManager, ClusterSingletonManagerSettings }
import akka.stream.ActorMaterializer
import akka.pattern.pipe
import com.sksamuel.elastic4s.ElasticClient
import im.actor.config.ActorConfig
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs.{ Processor, ProcessorState }
import im.actor.server.db.DbExtension

import scala.concurrent.Future

trait IndexerCommand
trait IndexerEvent

private[search] case class IndexerState(ts: Long, rId: Long) extends ProcessorState[IndexerState] {
  import IndexerEvents._
  override def updated(e: AnyRef, ts: Instant): IndexerState = e match {
    case LastMessageUpdated(newTs, newRId) ⇒ IndexerState(newTs, newRId)
  }
}

private[search] object Indexer {

  case class Continue(ts: Long, rId: Long)
  case object DelayIndex

  def register() =
    ActorSerializer.register(
      90000 → classOf[IndexerCommands.Index],
      90001 → classOf[IndexerCommands.IndexAck],

      92000 → classOf[IndexerEvents.LastMessageUpdated]
    )

  //  private val singletonName: String = "elasticsearchIndexer"
  //
  //  def startSingleton()(implicit system: ActorSystem): ActorRef =
  //    system.actorOf(
  //      ClusterSingletonManager.props(
  //        singletonProps = props,
  //        terminationMessage = PoisonPill,
  //        settings = ClusterSingletonManagerSettings(system)
  //      ),
  //      name = s"${singletonName}Manager"
  //    )

  def props: Props = Props(classOf[Indexer])

}

private[search] final class Indexer extends Processor[IndexerState] with IndexerHandlers {
  import IndexerCommands._
  import IndexerEvents._
  import Indexer._

  protected val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  protected val db = DbExtension(system).db
  protected val searchExt = SearchExtension(system)
  protected val client: ElasticClient = searchExt.client
  protected val indexName = searchExt.indexName

  protected val Paralellism = 2

  createIndex()

  override protected def getInitialState: IndexerState = IndexerState(0L, Long.MinValue)

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case _ ⇒ Future.successful(())
  }

  override protected def handleCommand: Receive = {
    case Index(optTs) ⇒
      log.debug("Got index message with ts: {}", optTs)
      val (ts, rId) = optTs map (ts ⇒ ts → Long.MinValue) getOrElse (state.ts → state.rId)
      self ! Continue(ts, rId)
      sender() ! IndexAck()
    case Continue(timestamp, randomId) ⇒
      log.debug("Got continue message with ts: {}, randomId: {}", timestamp, randomId)
      persistTS(LastMessageUpdated(timestamp, randomId)) { (e, t) ⇒
        commit(e, t)
        runIndex(state.ts, state.rId, searchExt.bulkSize) map {
          case (ts, rId) ⇒
            if (state.ts == ts && state.rId == rId) DelayIndex else Continue(ts, rId)
        } pipeTo self
      }
    case DelayIndex ⇒
      log.debug("Indexing will start after delay with ts: {}, randomId: {}", state.ts, state.rId)
      system.scheduler.scheduleOnce(ActorConfig.defaultTimeout) { self ! Continue(state.ts, state.rId) }
    case Failure(e) ⇒
      log.error(e, "Failed to execute indexing")
      self ! DelayIndex
  }

  override def persistenceId: String = s"Indexer_${self.path.name}"
}
