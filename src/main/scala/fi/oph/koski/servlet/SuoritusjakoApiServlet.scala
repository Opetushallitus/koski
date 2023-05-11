package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Unauthenticated}
import fi.oph.koski.schema.Oppija
import fi.oph.koski.suoritusjako.suoritetuttutkinnot.{SuoritetutTutkinnotOppija}

class SuoritusjakoApiServlet(implicit application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  get("/suoritetut-tutkinnot/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)

    val result = application.suoritusjakoService.getSuoritetutTutkinnot(params("secret"))

    renderEither[SuoritetutTutkinnotOppija](result)
  }

  get("/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)

    val result = application.suoritusjakoService.get(params("secret"))

    renderEither[Oppija](result.map(_.getIgnoringWarnings))
  }
}
