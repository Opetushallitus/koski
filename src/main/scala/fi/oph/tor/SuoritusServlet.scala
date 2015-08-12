package fi.oph.tor

import fi.oph.tor.date.FinnishDateParser
import fi.oph.tor.db.{Futures, GlobalExecutionContext}
import fi.oph.tor.json.Json
import fi.oph.tor.model.Identified.Id
import fi.oph.tor.model.{Identified, Suoritus}
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraServlet
import scala.collection.immutable.Iterable

class SuoritusServlet(rekisteri: TodennetunOsaamisenRekisteri) extends ScalatraServlet with GlobalExecutionContext with Futures with Logging {
  get("/") {
    params.get("personOid")
    contentType = "application/json;charset=utf-8"
    val filters: Iterable[SuoritusFilter] = params.flatMap {
      case ("personOid", personOid) => List(HenkilönSuoritukset(personOid))
      case ("organizationOid", personOid) => List(OrganisaationSuoritukset(personOid))
      case ("komoOid", personOid) => List(KoulutusModuulinSuoritukset(personOid))
      case ("completedAfter", dateString) => List(PäivämääränJälkeisetSuoritukset(FinnishDateParser.parseDate(dateString)))
      case (key, _) => throw new InvalidRequestException("Unexpected parameter: " + key)
    }
    Json.write(await(rekisteri.getSuoritukset(filters)).toList)
  }
  post("/") {
    val suoritus = Json.read[Suoritus](request.body)
    val id: Id = await(rekisteri.insertSuoritus(suoritus))
    contentType = "application/json;charset=utf-8"
    Json.write(IdResponse(id))
  }

  error {
    case InvalidRequestException(msg) =>
      halt(status = 400, msg)
    case e: Throwable =>
      logger.error("Error while processing request", e)
      halt(status = 500, "Internal server error")
  }
}
case class IdResponse(id: Identified.Id)
