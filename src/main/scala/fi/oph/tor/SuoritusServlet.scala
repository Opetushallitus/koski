package fi.oph.tor

import fi.oph.tor.date.ISO8601DateParser
import fi.oph.tor.db.{Futures, GlobalExecutionContext}
import fi.oph.tor.json.Json
import fi.oph.tor.model.Identified.Id
import fi.oph.tor.model.{Identified, Suoritus}
import fi.vm.sade.utils.slf4j.Logging

class SuoritusServlet(rekisteri: TodennetunOsaamisenRekisteri) extends ErrorHandlingServlet with GlobalExecutionContext with Futures with Logging {
  get("/") {
    params.get("personOid")
    contentType = "application/json;charset=utf-8"
    val query: SuoritusQuery = params.foldLeft(SuoritusQuery()) {
      case (query, ("oppijaId", personOid)) => query.withFilter(HenkilönSuoritukset(personOid))
      case (query, ("myontajaOrganisaatioId", personOid)) => query.withFilter(OrganisaationMyöntämätSuoritukset(personOid))
      case (query, ("jarjestajaOrganisaatioId", personOid)) => query.withFilter(OrganisaationJärjestämätSuoritukset(personOid))
      case (query, ("status", status)) => query.withFilter(SuorituksetStatuksella(status))
      case (query, ("completedAfter", dateString)) => query.withFilter(PäivämääränJälkeisetSuoritukset(ISO8601DateParser.parseDateTime(dateString)))
      case (query, ("includeChildren", includeChildren)) => query.copy(includeChildren = includeChildren.toBoolean)
      case (key, _) => throw new InvalidRequestException("Unexpected parameter: " + key)
    }
    Json.write(await(rekisteri.getSuoritukset(query)).toList)
  }
  post("/") {
    val suoritus = Json.read[Suoritus](request.body)
    val id: Id = await(rekisteri.insertSuoritus(suoritus))
    contentType = "application/json;charset=utf-8"
    Json.write(IdResponse(id))
  }

}
case class IdResponse(id: Identified.Id)
