package fi.oph.koski.pulssi

import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.json.Json
import org.json4s.JsonAST.JValue

object MockPrometheusRepository extends PrometheusRepository {
  def doQuery(query: String): JValue =
    if (query.endsWith("query=increase(fi_oph_koski_log_AuditLog[30d])")) {
      Json.readFile("src/main/resources/mockdata/prometheus/fi_oph_koski_log_AuditLog.json")
    } else {
      throw HttpStatusException(400, "Bad request", "GET", "query")
    }
}
