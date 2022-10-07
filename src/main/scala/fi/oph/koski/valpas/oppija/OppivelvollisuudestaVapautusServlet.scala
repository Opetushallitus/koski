package fi.oph.koski.valpas.oppija

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.ChainingSyntax.chainingOps
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.oppivelvollisuudestavapautus.{OppivelvollisuudestaVapautuksenMitätöinti, UusiOppivelvollisuudestaVapautus}
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession

class OppivelvollisuudestaVapautusServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession  {
  private val oppivelvollisuudestaVapautusService = application.valpasOppivelvollisuudestaVapautusService
  private val organisaatioService = application.organisaatioService

  get("/pohjatiedot") {
    oppivelvollisuudestaVapautusService.pohjatiedot
  }

  post("/") {
    renderWithJsonBody[UusiOppivelvollisuudestaVapautus] { vapautus =>
      val kuntaOid = toKuntaOid(vapautus.kuntakoodi)
      oppivelvollisuudestaVapautusService
        .lisääOppivelvollisuudestaVapautus(vapautus)
        .tap(_ => ValpasAuditLog.auditOppivelvollisuudestaVapauttaminen(kuntaOid, vapautus.oppijaOid))
    }
  }

  delete("/") {
    renderWithJsonBody[OppivelvollisuudestaVapautuksenMitätöinti] { mitätöinti =>
      val kuntaOid = toKuntaOid(mitätöinti.kuntakoodi)
      oppivelvollisuudestaVapautusService
        .mitätöiOppivelvollisuudestaVapautus(mitätöinti)
        .tap(_ => ValpasAuditLog.auditOppivelvollisuudestaVapauttamisenMitätöinti(kuntaOid, mitätöinti.oppijaOid))
    }
  }

  private def toKuntaOid(kuntakoodi: String): String =
    organisaatioService.haeKuntaOid(kuntakoodi).getOrElse(s"kuntakoodi:$kuntakoodi")
}
