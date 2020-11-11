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
