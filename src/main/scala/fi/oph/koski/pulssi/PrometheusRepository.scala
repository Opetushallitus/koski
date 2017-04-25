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
  override def doQuery(query: String): JValue =
    runTask(http.get(ParameterizedUriWrapper(uriFromString(query), query))(Http.parseJson[JValue]))
}

trait PrometheusRepository {
  implicit val formats = GenericJsonFormats.genericFormats
  def koskiMonthlyOperations: Seq[Map[String, Any]] = {
    metric("/prometheus/api/v1/query?query=koski_monthly_operations").map { metric =>
      val operation = (metric \ "metric" \ "operation").extract[String]
      val count = value(metric).map(_.toDouble.toInt).getOrElse(0)
      (operation, Math.max(0, count))
    }.map { case(operation, count) =>
      Map(
        "nimi" -> operation.toLowerCase.capitalize.replaceAll("_", " "),
        "määrä" -> count
      )
    }
  }.sortBy(_("nimi").toString)

  def koskiAvailability: Double =
    metric("/prometheus/api/v1/query?query=koski_available_percent")
      .headOption.flatMap(value).map(_.toDouble).getOrElse(100)

  def doQuery(query: String): JValue

  private def metric(query: String) =
    (doQuery(query) \ "data" \ "result").extract[List[JValue]]

  private def value(metric: JValue): Option[String] =
    (metric \ "value").extract[List[String]].lastOption
}

