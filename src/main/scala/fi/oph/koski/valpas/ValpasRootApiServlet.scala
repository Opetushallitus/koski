package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession

class ValpasRootApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private lazy val organisaatioService = application.organisaatioService

  get("/user") {
    valpasSession.user
  }

  get("/organisaatiot") {
    // TODO: Kuntakäyttäjälle ei pitäisi lähettää hierarkista listaa, vaan ainoastaan flat-lista kyseisestä kunnasta! Kunta kun voi olla myös
    // perusopetuksen järjestäjä.
    organisaatioService.kaikkiKäyttöoikeudellisetOrganisaatiot
  }
}
