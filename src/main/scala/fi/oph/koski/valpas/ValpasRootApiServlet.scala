package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession

class ValpasRootApiServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with RequiresValpasSession {
  private lazy val organisaatioService = application.organisaatioService

  get("/user") {
    valpasSession.user
  }

  get("/organisaatiot") {
    organisaatioService.kaikkiKäyttöoikeudellisetOrganisaatiot
  }
}
