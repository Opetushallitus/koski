package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServletWithSchemaBasedSerialization, NoCache}

class HealthCheckApiServlet(implicit val application: KoskiApplication) extends ApiServletWithSchemaBasedSerialization with NoCache with Unauthenticated {
  get("/") {
    renderStatus(application.healthCheck.healthcheck)
  }
}

