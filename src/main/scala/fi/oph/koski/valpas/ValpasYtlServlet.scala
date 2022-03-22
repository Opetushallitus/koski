package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasYtlSession
import fi.oph.koski.valpas.ytl.ValpasYtlService
import fi.oph.koski.ytl.YtlRequest
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.valpas.log.ValpasAuditLog.auditLogOppivelvollisuusrekisteriLuovutus

class ValpasYtlServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasYtlSession {
  private val ytlService = new ValpasYtlService(application)

  post("/oppijat") {
    withJsonBody { json =>
      YtlRequest.parseBulk(json) match {
        case Right((oidit, hetut, _)) =>
          ytlService.haeMaksuttomuustiedot(oidit, hetut)
            .tap(_.map(o => auditLogOppivelvollisuusrekisteriLuovutus(o.oppijaOid)))
        case Left(status) =>
          haltWithStatus(status)
      }
    }()
  }
}

