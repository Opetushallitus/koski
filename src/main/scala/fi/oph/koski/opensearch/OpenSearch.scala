package fi.oph.koski.opensearch

import com.typesafe.config.Config
import fi.oph.koski.http.{Http, RequestSigner}
import fi.oph.koski.log.Logging
import fi.oph.koski.util.PaginationSettings

case class OpenSearchConfig(host: String, protocol: String, port: Int)

case class OpenSearch(config: Config) extends Logging {
  private val OpenSearchConfig(host, protocol, port) = (sys.env.get("ES_ENDPOINT")) match {
    case Some(host) => OpenSearchConfig(host, "https", 443)
    case _ => OpenSearchConfig(config.getString("opensearch.host"), config.getString("opensearch.protocol"), config.getInt("opensearch.port"))
  }

  private val url = s"$protocol://$host:$port"

  // TOR-1466: SigV4 signing for AWS-managed OpenSearch under FGAC. Feature-flagged
  // so the signer can ship in code with no behavior change, then be activated
  // per env via AppConfig (opensearch.useIamAuth=true) before the irreversible
  // FGAC enable step on the domain.
  private val signer: Option[RequestSigner] =
    if (config.getBoolean("opensearch.useIamAuth")) {
      val region = config.getString("opensearch.region")
      logger.info(s"OpenSearch IAM auth enabled, signing requests with SigV4 (region=$region)")
      Some(new SigV4Signer(region))
    } else {
      None
    }

  val http: Http = Http(url, "opensearch").copy(signer = signer)
}

object OpenSearch {
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
