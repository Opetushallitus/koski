package fi.oph.koski.pulssi

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class PulssiServlet(application: KoskiApplication) extends ApiServlet with NoCache with Unauthenticated {
  get("/") {
    application.perustiedotRepository.statistics
  }
}
