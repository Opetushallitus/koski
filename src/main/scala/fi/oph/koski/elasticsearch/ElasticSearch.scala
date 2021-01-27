package fi.oph.koski.elasticsearch

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.oph.koski.log.Logging
import fi.oph.koski.util.PaginationSettings

case class ElasticSearch(config: Config) extends Logging {
  private val host = config.getString("elasticsearch.host")
  private val port = config.getInt("elasticsearch.port")
  private val protocol = config.getString("elasticsearch.protocol")
  private val url = s"$protocol://$host:$port"

  val http = Http(url, "elasticsearch")

  val init_ = synchronized {
    if (host == "localhost") {
      new ElasticSearchRunner("./elasticsearch", port, port + 100).start
    }
    logger.info(s"Using elasticsearch at $host:$port")
  }
}

object ElasticSearch {
  private val emptyFilter = Map("match_all" -> Map.empty)

  def allFilter(queries: List[Map[String, Any]]): Map[String, AnyRef] = queries match {
    case Nil => emptyFilter
    case _ => Map(
      "bool" -> Map(
        "must" -> queries
      )
    )
  }

  def anyFilter(queries: List[Map[String, Any]]): Map[String, AnyRef] = queries match {
    case Nil => emptyFilter
    case _ => Map(
      "bool" -> Map(
        "should" -> queries
      )
    )
  }

  def noneFilter(queries: List[Map[String, Any]]): Map[String, AnyRef] = queries match {
    case Nil => emptyFilter
    case _ => Map(
      "bool" -> Map(
        "must_not" -> queries
      )
    )
  }

  def nestedFilter(path: String, query: Map[String, AnyRef]) = Map(
    "nested" -> Map(
      "path" -> path,
      "query" -> query
    )
  )

  def applyPagination(paginationSettings: Option[PaginationSettings], doc: Map[String, Any]) = paginationSettings match {
    case None => doc
    case Some(pagination) => doc ++ Map(
      "from" -> pagination.page * pagination.size,
      "size" -> pagination.size)
  }
}
