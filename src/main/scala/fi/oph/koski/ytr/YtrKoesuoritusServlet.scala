package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresKansalainen}
import fi.oph.koski.log.KoskiOperation.KoskiOperation
import fi.oph.koski.log.{AuditLog, AuditLogMessage, KoskiMessageField}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_YLIOPPILASKOE_HAKU, KANSALAINEN_HUOLTAJA_YLIOPPILASKOE_HAKU}
import fi.oph.koski.servlet.OppijaHtmlServlet

class YtrKoesuoritusServlet(implicit val application: KoskiApplication) extends OppijaHtmlServlet with RequiresKansalainen {
  private val koesuoritukset: KoesuoritusService = KoesuoritusService(application.config)

  get("/:copyOfExamPaper") {
    val examPaper = getStringParam("copyOfExamPaper")
    val hasAccess = hasAccessTo(examPaper)
    if (koesuoritukset.koesuoritusExists(examPaper) && hasAccess) {
      contentType = if (examPaper.endsWith(".pdf")) "application/pdf" else "text/html"
      koesuoritukset.writeKoesuoritus(examPaper, response.getOutputStream)
    } else {
      logger.warn(s"Exam paper $examPaper not found, hasAccess: $hasAccess")
      haltWithStatus(KoskiErrorCategory.notFound.suoritustaEiLöydy())
    }
  }

  private def hasAccessTo(examPaper: String): Boolean = {
    logger.debug(s"Tarkistetaan ${if (isHuollettava) "huollettavan" else "oma"} koesuoritus access")
    getOppija.flatMap(application.ytrRepository.findByTunnisteet)
      .exists(_.examPapers.contains(examPaper))
  }

  private def getOppija: Option[HenkilönTunnisteet] =
    if (isHuollettava) {
      getHuollettavaOppija
    } else {
      mkAuditLog(koskiSession, KANSALAINEN_YLIOPPILASKOE_HAKU)
      application.henkilöRepository.findByOid(koskiSession.oid)
   }

  private def getHuollettavaOppija: Option[HenkilönTunnisteet] = {
    val oid = getStringParam("huollettava")
    if (koskiSession.isUsersHuollettava(oid)) {
      mkAuditLog(oid, KANSALAINEN_HUOLTAJA_YLIOPPILASKOE_HAKU)
      application.henkilöRepository.findByOid(oid)
    } else {
      None
    }
  }

  private def mkAuditLog(session: KoskiSpecificSession, operation: KoskiOperation): Unit = mkAuditLog(session.oid, operation)
  private def mkAuditLog(oid: String, operation: KoskiOperation): Unit = AuditLog.log(AuditLogMessage(operation, koskiSession, Map(KoskiMessageField.oppijaHenkiloOid -> oid)))

  private def isHuollettava = getOptionalStringParam("huollettava").isDefined
}
