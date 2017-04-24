package fi.oph.koski.pulssi

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http.{ParameterizedUriWrapper, _}
import fi.oph.koski.json.GenericJsonFormats
import org.json4s.JValue

object PrometheusRepository {
  def apply(config: Config) = {
    if (config.getString("prometheus.url") == "mock") {
      MockPrometheusRepository
    } else {
      new RemotePrometheusRepository(Http(config.getString("prometheus.url")))
    }
  }
}

class RemotePrometheusRepository(http: Http) extends PrometheusRepository {
  override def doQuery(query: ParameterizedUriWrapper): JValue =
    runTask(http.get(query)(Http.parseJson[JValue]))
}

trait PrometheusRepository {
  implicit val formats = GenericJsonFormats.genericFormats
  def auditLogMetrics: Seq[Map[String, Any]] = {
    val json = doQuery(uri"/prometheus/api/v1/query?query=sum+by+(operation)+(increase(fi_oph_koski_log_AuditLog${"[30d]"}))")
    (json \ "data" \ "result").extract[List[JValue]].map { metric =>
      val operation = (metric \ "metric" \ "operation").extract[String]
      val count = (metric \ "value").extract[List[String]].lastOption.map(_.toDouble.toInt).getOrElse(0)
      (operation, Math.max(0, count))
    }.map { case(operation, count) =>
      Map(
        "nimi" -> operation.toLowerCase.capitalize.replaceAll("_", " "),
        "määrä" -> count
      )
    }
  }.sortBy(_("nimi").toString)

  def doQuery(query: ParameterizedUriWrapper): JValue
}
