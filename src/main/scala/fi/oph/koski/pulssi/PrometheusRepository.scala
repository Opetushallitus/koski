package fi.oph.koski.pulssi

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http.{ParameterizedUriWrapper, _}
import fi.oph.koski.json.GenericJsonFormats
import fi.oph.koski.schema.JsonSerializer.extract
import org.json4s.JValue

import scalaz.concurrent.Task

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
  override def query(query: String): Task[JValue] =
    http.get(ParameterizedUriWrapper(uriFromString(query), query))(Http.parseJson[JValue])
}

trait PrometheusRepository {
  implicit val formats = GenericJsonFormats.genericFormats

  def query(query: String): Task[JValue]

  def koskiMetrics: KoskiMetriikka = runTask(for {
    ops <- monthlyOps
    koskiAvailability <- availability
    failedTransfers <- intMetric("koski_failed_data_transfers")
    outages <- intMetric("koski_unavailable_count")
    monthlyAlerts <- monthlyAlerts
    applicationErrors <- intMetric("koski_application_errors_count")
  } yield KoskiMetriikka(
    ops.map(op => (op("nimi").toString, op("määrä").asInstanceOf[Int])).toMap,
    koskiAvailability,
    failedTransfers,
    outages,
    monthlyAlerts,
    applicationErrors
  ))

  def koskiMonthlyOperations: List[Map[String, Any]] = runTask(monthlyOps)

  private def monthlyOps: Task[List[Map[String, Any]]] = metric("/api/v1/query?query=koski_monthly_operations")
    .map(_.map { metric =>
      val operation = extract[String](metric \ "metric" \ "operation")
      val count = value(metric).map(_.toDouble.toInt).getOrElse(0)
      (operation, Math.max(0, count))
    }.map { case (operation, count) =>
      Map(
        "nimi" -> operation.toLowerCase.capitalize.replaceAll("_", " "),
        "määrä" -> count
      )
    }.sortBy(_("nimi").toString)
  )

  private def monthlyAlerts: Task[Map[String, Int]] = metric("/api/v1/query?query=koski_alerts").map(_.map { metric =>
      val alert = extract[String](metric \ "metric" \ "alertname")
      val instance = extract[Option[String]](metric \ "metric" \ "instance").map(i => "@"+i).getOrElse("")
      val count = value(metric).map(_.toDouble.toInt).getOrElse(0)
      (s"$alert$instance", Math.max(0, count))
    }.toMap
  )

  private def availability: Task[Double] =
    metric("/api/v1/query?query=koski_available_percent")
      .map(_.headOption.flatMap(value).map(_.toDouble).map(round(3)).getOrElse(100))

  private def intMetric(metricName: String): Task[Int] =
    metric(s"/api/v1/query?query=$metricName")
      .map(_.headOption.flatMap(value).map(_.toDouble.toInt).getOrElse(0))

  private def metric(queryStr: String): Task[List[JValue]] =
    query(queryStr).map(result => extract[List[JValue]](result \ "data" \ "result"))

  private def value(metric: JValue): Option[String] =
    (extract[List[String]](metric \ "value")).lastOption
}

case class KoskiMetriikka(
  operaatiot: Map[String, Int],
  saavutettavuus: Double,
  epäonnistuneetSiirrot: Int,
  katkot: Int,
  hälytykset: Map[String, Int],
  virheet: Int
) {
  def toPublic = JulkinenMetriikka(operaatiot, saavutettavuus)
  def hälytyksetYhteensä: Int = hälytykset.values.sum
}

case class JulkinenMetriikka(
  operaatiot: Map[String, Int],
  saavutettavuus: Double
)
