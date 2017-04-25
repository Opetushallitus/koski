package fi.oph.koski.pulssi

import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.json.Json
import org.json4s.JsonAST.JValue

object MockPrometheusRepository extends PrometheusRepository {
  def doQuery(query: String): JValue =
    if (query.endsWith("koski_monthly_operations")) {
      Json.readFile("src/main/resources/mockdata/prometheus/koski_monthly_operations.json")
    } else if (query.endsWith("koski_available_percent")) {
      Json.readFile("src/main/resources/mockdata/prometheus/koski_available_percent.json")
    } else {
      throw HttpStatusException(400, "Bad request", "GET", "query")
    }
}
