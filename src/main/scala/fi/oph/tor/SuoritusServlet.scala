package fi.oph.tor

import fi.oph.tor.db.{Futures, GlobalExecutionContext}
import fi.oph.tor.json.Json
import fi.oph.tor.model.Suoritus
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraServlet

class SuoritusServlet(rekisteri: TodennetunOsaamisenRekisteri) extends ScalatraServlet with GlobalExecutionContext with Futures with Logging {
  get("/") {
    params.get("personOid")
    contentType = "application/json;charset=utf-8"
    val filter: SuoritusFilter = params.toList.foldLeft(KaikkiSuoritukset.asInstanceOf[SuoritusFilter]) {
      case (filter, ("personOid", personOid)) => filter.and(HenkilönSuoritukset(personOid))
      case (filter, ("organizationOid", personOid)) => filter.and(OrganisaationSuoritukset(personOid))
      case (filter, (key, _)) => throw new InvalidRequestException("Unexpected parameter: " + key)
    }
    Json.write(await(rekisteri.getSuoritukset(filter)).toList)
  }
  post("/") {
    val suoritus = Json.read[Suoritus](request.body)
    rekisteri.insertSuoritus(suoritus)
  }

  error {
    case InvalidRequestException(msg) =>
      halt(status = 400, msg)
    case e: Throwable =>
      logger.error("Error while processing request", e)
      halt(status = 500, "Internal server error")
  }
}

case class InvalidRequestException(msg: String) extends Exception(msg)