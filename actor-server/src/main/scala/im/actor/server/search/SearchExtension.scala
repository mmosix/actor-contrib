package im.actor.server.search

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.sksamuel.elastic4s.{ ElasticsearchClientUri, ElasticClient }
import org.elasticsearch.common.settings.ImmutableSettings
import scala.concurrent.Future
import scala.concurrent.duration._

sealed trait SearchExtension extends Extension

final class SearchExtensionImpl(actorSystem: ActorSystem) extends SearchExtension {
  import IndexerCommands._

  Indexer.register()

  implicit val system = actorSystem
  import system.dispatcher

  implicit val timeout: Timeout = Timeout(20.seconds)

  private val elasticConfig = SearchConfig.load.get
  private val settings = ImmutableSettings.settingsBuilder().put("cluster.name", elasticConfig.clusterName).build()
  private val elasticUri = ElasticsearchClientUri(s"elasticsearch://${elasticConfig.host}:${elasticConfig.port}")

  val indexName = SearchConfig.loadClusterName
  val bulkSize = SearchConfig.loadBulkSize
  val client = ElasticClient.remote(settings, elasticUri)

  private lazy val indexer = system.actorOf(Indexer.props, "indexer")

  def continueIndexing(): Unit =
    (indexer ? Index(None)).mapTo[IndexAck] map (_ ⇒ ())

  def reindex(fromTs: Long): Future[Unit] =
    (indexer ? Index(Some(fromTs))).mapTo[IndexAck] map (_ ⇒ ())

}

object SearchExtension extends ExtensionId[SearchExtensionImpl] with ExtensionIdProvider {
  override def lookup = SearchExtension

  override def createExtension(system: ExtendedActorSystem) = new SearchExtensionImpl(system)
}
