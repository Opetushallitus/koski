package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class HealthCheckApiServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with Unauthenticated {
  get("/") {
    renderStatus(application.healthCheck.healthcheckWithExternalSystems)
  }

  get("/internal") {
    renderStatus(application.healthCheck.internalHealthcheck)
  }
}

