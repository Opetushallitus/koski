package fi.oph.koski.valpas.oppija

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession

class OppivelvollisuudestaVapautusServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession  {
  private val oppivelvollisuudestaVapautusService = application.valpasOppivelvollisuudestaVapautusService

  get("/pohjatiedot") {
    oppivelvollisuudestaVapautusService.pohjatiedot
  }

  post("/") {
    renderWithJsonBody(oppivelvollisuudestaVapautusService.lisääOppivelvollisuudestaVapautus)
  }

  delete("/") {
    renderWithJsonBody(oppivelvollisuudestaVapautusService.mitätöiOppivelvollisuudestaVapautus)
  }
}
