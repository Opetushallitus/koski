package fi.oph.koski.pulssi

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http.{ParameterizedUriWrapper, _}
import fi.oph.koski.json.GenericJsonFormats
import org.json4s.JValue

import scala.math.BigDecimal.RoundingMode.HALF_UP
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
    alerts <- intMetric("koski_alerts_count")
    applicationErrors <- intMetric("koski_application_errors_count")
  } yield KoskiMetriikka(
    ops.map(op => (op("nimi").toString, op("määrä").asInstanceOf[Int])).toMap,
    koskiAvailability,
    failedTransfers,
    outages,
    alerts,
    applicationErrors
  ))

  def koskiMonthlyOperations: List[Map[String, Any]] = runTask(monthlyOps)

  private def monthlyOps: Task[List[Map[String, Any]]] = metric("/prometheus/api/v1/query?query=koski_monthly_operations")
    .map(_.map { metric =>
      val operation = (metric \ "metric" \ "operation").extract[String]
      val count = value(metric).map(_.toDouble.toInt).getOrElse(0)
      (operation, Math.max(0, count))
    }.map { case (operation, count) =>
      Map(
        "nimi" -> operation.toLowerCase.capitalize.replaceAll("_", " "),
        "määrä" -> count
      )
    }.sortBy(_("nimi").toString)
  )

  private def availability: Task[Double] =
    metric("/prometheus/api/v1/query?query=koski_available_percent")
      .map(_.headOption.flatMap(value).map(_.toDouble).map(round(3)).getOrElse(100))

  private def intMetric(metricName: String): Task[Int] =
    metric(s"/prometheus/api/v1/query?query=$metricName")
      .map(_.headOption.flatMap(value).map(_.toDouble.toInt).getOrElse(0))

  private def metric(queryStr: String): Task[List[JValue]] =
    query(queryStr).map(result => (result \ "data" \ "result").extract[List[JValue]])

  private def value(metric: JValue): Option[String] =
    (metric \ "value").extract[List[String]].lastOption

  private def round(precisison: Int) = (double: Double) => BigDecimal(double).setScale(precisison, HALF_UP).toDouble
}

case class KoskiMetriikka(
  operaatiot: Map[String, Int],
  saavutettavuus: Double,
  epäonnistuneetSiirrot: Int,
  katkot: Int,
  hälytykset: Int,
  virheet: Int
) {
  def toPublic = JulkinenMetriikka(operaatiot, saavutettavuus)
}

case class JulkinenMetriikka(
  operaatiot: Map[String, Int],
  saavutettavuus: Double
)
