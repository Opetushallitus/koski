package fi.oph.koski.elasticsearch

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.koski.log.Logging
import fi.oph.koski.util.PaginationSettings
import org.http4s.EntityEncoder

case class ElasticSearch(config: Config) extends Logging {
  private val host = config.getString("elasticsearch.host")
  private val port = config.getInt("elasticsearch.port")
  private val url = s"http://$host:$port"

  val http = Http(url, "elasticsearch")

  val init_ = synchronized {
    if (host == "localhost") {
      new ElasticSearchRunner("./elasticsearch", port, port + 100).start
    }
    logger.info(s"Using elasticsearch at $host:$port")
  }

  def refreshIndex =
    Http.runTask(http.post(uri"/koski/_refresh", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))

  def refreshIndex(indexName: String) = {
    Http.runTask(http.post(uri"/$indexName/_refresh", "")(EntityEncoder.stringEncoder)(Http.unitDecoder))
  }
}

object ElasticSearch {
  def allFilter(queries: List[Map[String, Any]]): Map[String, AnyRef] = queries match {
    case Nil => Map.empty
    case _ => Map(
      "bool" -> Map(
        "must" -> List(
          queries
        )
      )
    )
  }

  def anyFilter(queries: List[Map[String, Any]]): Map[String, AnyRef] = queries match {
    case Nil => Map.empty
    case _ => Map(
      "bool" -> Map(
        "should" -> List(
          queries
        )
      )
    )
  }

  def applyPagination(paginationSettings: Option[PaginationSettings], doc: Map[String, Any]) = paginationSettings match {
    case None => doc
    case Some(pagination) => doc ++ Map(
      "from" -> pagination.page * pagination.size,
      "size" -> pagination.size)
  }
}
