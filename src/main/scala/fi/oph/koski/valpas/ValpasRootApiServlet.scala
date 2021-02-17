package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.organisaatio.OrganisaatioHierarkia
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession

class ValpasRootApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private lazy val organisaatioService = application.organisaatioService

  get("/user") {
    valpasSession.user
  }

  get("/organisaatiot-ja-kayttooikeusroolit") {
    organisaatioService.omatOrganisaatiotJaKayttooikeusroolit.map(o => o.copy(organisaatioHierarkia = o.organisaatioHierarkia.copy(children = List())))
  }
}
