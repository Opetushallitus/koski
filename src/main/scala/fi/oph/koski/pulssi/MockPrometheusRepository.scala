package fi.oph.koski.pulssi

import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.json.Json
import org.json4s.JsonAST.JValue

object MockPrometheusRepository extends PrometheusRepository {
  def doQuery(query: String): JValue = {
    val Array(_, filename) = query.split("=")
    Json.readFileIfExists(s"src/main/resources/mockdata/prometheus/$filename.json").getOrElse {
      throw HttpStatusException(400, "Prometheus metric file not found", "GET", query)
    }
  }
}
