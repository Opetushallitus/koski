package fi.oph.koski.ytr

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSpecificSession, RequiresKansalainen}
import fi.oph.koski.log.KoskiOperation.KoskiOperation
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_HUOLTAJA_YLIOPPILASKOE_HAKU, KANSALAINEN_YLIOPPILASKOE_HAKU}
import fi.oph.koski.servlet.OppijaHtmlServlet

class YtrKoesuoritusServlet(implicit val application: KoskiApplication) extends OppijaHtmlServlet with RequiresKansalainen {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))
  override val unsafeAllowInlineStyles: Boolean = true

  val s3config: YtrS3Config = {
    if (Environment.usesAwsSecretsManager) YtrS3Config.fromSecretsManager else YtrS3Config.fromConfig(application.config)
  }

  private val koesuoritukset: KoesuoritusService = KoesuoritusService(s3config)

  get("/*.*")(nonce => {
    val examPaper = multiParams("splat").mkString(".")
    val hasAccess = hasAccessTo(examPaper)
    if (hasAccess && koesuoritukset.koesuoritusExists(examPaper)) {
      // TODO: Vaatiikohan YTL koesuoritukset väljemmän CSP:n? Luultavasti on ainakin tyylejä, jotka vaatisivat noncen...
      contentType = if (examPaper.endsWith(".pdf")) "application/pdf" else "text/html"
      val os = response.getOutputStream
      if (os != null) {
        koesuoritukset.writeKoesuoritus(examPaper, os)
      } else {
        logger.warn(s"Exam paper $examPaper download failed")
        haltWithStatus(KoskiErrorCategory.notFound.suoritustaEiLöydy("Koesuoritusta ei juuri nyt saatu ladattua. Yritä myöhemmin uudelleen."))
      }
    } else {
      logger.warn(s"Exam paper $examPaper not found, hasAccess: $hasAccess")
      haltWithStatus(KoskiErrorCategory.notFound.suoritustaEiLöydy())
    }
  })

  private def hasAccessTo(examPaper: String): Boolean = {
    logger.debug(s"Tarkistetaan ${if (isHuollettava) "huollettavan" else "oma"} koesuoritus access")
    getOppija.flatMap(application.ytrRepository.findByTunnisteet)
      .exists(_.examPapers.contains(examPaper))
  }

  private def getOppija: Option[HenkilönTunnisteet] =
    if (isHuollettava) {
      getHuollettavaOppija
    } else {
      mkAuditLog(session, KANSALAINEN_YLIOPPILASKOE_HAKU)
      application.henkilöRepository.findByOid(session.oid)
   }

  private def getHuollettavaOppija: Option[HenkilönTunnisteet] = {
    val oid = getStringParam("huollettava")
    if (session.isUsersHuollettava(oid)) {
      mkAuditLog(oid, KANSALAINEN_HUOLTAJA_YLIOPPILASKOE_HAKU)
      application.henkilöRepository.findByOid(oid)
    } else {
      None
    }
  }

  private def mkAuditLog(session: KoskiSpecificSession, operation: KoskiOperation): Unit = mkAuditLog(session.oid, operation)
  private def mkAuditLog(oid: String, operation: KoskiOperation): Unit = AuditLog.log(KoskiAuditLogMessage(operation, session, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oid)))

  private def isHuollettava = getOptionalStringParam("huollettava").isDefined
}
