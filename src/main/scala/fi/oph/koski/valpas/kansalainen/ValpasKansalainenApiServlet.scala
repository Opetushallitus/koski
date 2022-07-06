package fi.oph.koski.valpas.kansalainen

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.ChainingSyntax.chainingOps
import fi.oph.koski.valpas.log.ValpasAuditLog.{auditLogKansalainenHuollettavienTiedot, auditLogKansalainenOmatTiedot}
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasKansalainenSession

class ValpasKansalainenApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasKansalainenSession {
  private val kansalainenService = application.valpasKansalainenService

  get("/user") {
    session.user
  }

  get("/tiedot") {
    kansalainenService.getKansalaisnäkymänTiedot()
      .tap(auditLogKansalainenOmatTiedot)
      .tap(auditLogKansalainenHuollettavienTiedot)
  }
}
