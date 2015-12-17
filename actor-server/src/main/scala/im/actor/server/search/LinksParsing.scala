package im.actor.server.search

import org.nibor.autolink._

import scala.collection.JavaConversions._
import scala.util.Try

trait LinksParsing {

  private val linkExtractor =
    LinkExtractor
      .builder()
      .linkTypes(java.util.EnumSet.of(LinkType.URL))
      .build()

  private val Utf8Encoding = "UTF-8"

  def extractLinks(rawText: String): List[String] = {
    val links = linkExtractor.extractLinks(rawText)
    links.toList flatMap { link ⇒
      Try(java.net.URLDecoder.decode(rawText.substring(link.getBeginIndex, link.getEndIndex), Utf8Encoding)).toOption
    }
  }

}