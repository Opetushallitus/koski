package fi.oph.tor

import fi.oph.tor.db.{Futures, GlobalExecutionContext}
import fi.oph.tor.json.Json
import org.scalatra.ScalatraServlet

class TutkintosuoritusServlet(rekisteri: TodennetunOsaamisenRekisteri) extends ScalatraServlet with GlobalExecutionContext with Futures {
  get("/") {
    contentType = "application/json;charset=utf-8"
    Json.write(await(rekisteri.getTutkintosuoritukset).toList)
  }
  post("/") {

  }
}
