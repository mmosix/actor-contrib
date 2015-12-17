package im.actor.server.search

import com.github.kxbmap.configs._
import com.typesafe.config.Config
import im.actor.config.ActorConfig

import scala.util.Try

case object SearchConfig {

  def loadElastic(config: Config): Try[ElasticsearchConfig] =
    for {
      clusterName ← config.get[Try[String]]("cluster-name")
      host ← config.get[Try[String]]("host")
      port ← config.get[Try[Int]]("tcp-port")
    } yield ElasticsearchConfig(clusterName, host, port)

  def load: Try[ElasticsearchConfig] = {
    loadElastic(ActorConfig.load().getConfig("services.search.elasticsearch"))
  }

  def loadClusterName: String = ActorConfig.load().getString("services.search.index-name")

  def loadBulkSize: Int = ActorConfig.load().getInt("services.search.bulk-size")
}

case class ElasticsearchConfig(clusterName: String, host: String, port: Int)
