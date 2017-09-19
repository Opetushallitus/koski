package fi.oph.koski.pulssi

import fi.oph.koski.http.HttpStatusException
import fi.oph.koski.json.JsonFiles
import org.json4s

import scalaz.concurrent.Task

object MockPrometheusRepository extends PrometheusRepository {
  override def query(query: String): Task[json4s.JValue] = {
    val Array(_, filename) = query.split("=")
    JsonFiles.readFileIfExists(s"src/main/resources/mockdata/prometheus/$filename.json")
      .map(Task.now)
      .getOrElse(Task.fail(HttpStatusException(400, "Prometheus metric file not found", "GET", query)))
  }
}
