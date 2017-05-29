package fi.oph.koski.pulssi

import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.json.Json
import org.json4s
import org.json4s.JsonAST.JValue

import scalaz.concurrent.Task

object MockPrometheusRepository extends PrometheusRepository {
  override def query(query: String): Task[json4s.JValue] = {
    val Array(_, filename) = query.split("=")
    Json.readFileIfExists(s"src/main/resources/mockdata/prometheus/$filename.json")
      .map(Task.now)
      .getOrElse(Task.fail(HttpStatusException(400, "Prometheus metric file not found", "GET", query)))
  }
}
