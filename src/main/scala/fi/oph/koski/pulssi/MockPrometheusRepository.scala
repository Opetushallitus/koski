package fi.oph.koski.pulssi

import fi.oph.koski.http.Http.ParameterizedUriWrapper
import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.json.Json
import org.json4s.JsonAST.JValue

object MockPrometheusRepository extends PrometheusRepository {
  def doQuery(query: ParameterizedUriWrapper): JValue =
    if (query.template == "/prometheus/api/v1/query?query=sum+by+(operation)+(increase(fi_oph_koski_log_AuditLog_))") {
      Json.readFile("src/main/resources/mockdata/prometheus/fi_oph_koski_log_AuditLog.json")
    } else {
      throw HttpStatusException(400, "Bad request", "GET", "query")
    }
}
