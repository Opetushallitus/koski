package fi.oph.tor

import fi.oph.tor.db.{Futures, GlobalExecutionContext}
import fi.oph.tor.json.Json
import fi.oph.tor.model.Suoritus
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraServlet

import scalaj.http.HttpResponse

class SuoritusServlet(rekisteri: TodennetunOsaamisenRekisteri) extends ScalatraServlet with GlobalExecutionContext with Futures with Logging {
  get("/") {
    contentType = "application/json;charset=utf-8"
    Json.write(await(rekisteri.getSuoritukset).toList)
  }
  post("/") {
    val suoritus = Json.read[Suoritus](request.body)
    rekisteri.insertSuoritus(suoritus)
  }

  error { case e: Throwable =>
    logger.error("Error while processing request", e)
    HttpResponse("Internal server error", 500, Map())
  }
}
