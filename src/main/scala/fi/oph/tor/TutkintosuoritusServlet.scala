package fi.oph.tor

import fi.oph.tor.db.{Futures, GlobalExecutionContext}
import fi.oph.tor.json.Json
import fi.oph.tor.model.Tutkintosuoritus
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraServlet
import scalaj.http.HttpResponse

class TutkintosuoritusServlet(rekisteri: TodennetunOsaamisenRekisteri) extends ScalatraServlet with GlobalExecutionContext with Futures with Logging {
  get("/") {
    contentType = "application/json;charset=utf-8"
    Json.write(await(rekisteri.getTutkintosuoritukset).toList)
  }
  post("/") {
    val tutkintosuoritus = Json.read[Tutkintosuoritus](request.body)
    rekisteri.insertTutkintosuoritus(tutkintosuoritus)
  }

  error { case e: Throwable =>
    logger.error("Error while processing request", e)
    HttpResponse("Internal server error", 500, Map())
  }
}
