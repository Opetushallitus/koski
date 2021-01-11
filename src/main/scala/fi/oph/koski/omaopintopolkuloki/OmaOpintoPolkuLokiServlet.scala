package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OmaOpintoPolkuLokiServlet(implicit val application: KoskiApplication) extends
  RequiresKansalainen with ApiServlet with NoCache {

  val auditLogs = new AuditLogService(application.organisaatioRepository, AuditLogDynamoDB.db)

  get("/auditlogs") {
    renderEither(
      auditLogs.queryLogsFromDynamo(koskiSession.oid)
    )
  }

  get("/whoami") {
    application.opintopolkuHenkilöFacade.findOppijaByOid(koskiSession.oid).map(h =>
      OmaOpintopolkuLokiHenkiloTiedot(h.hetu, h.etunimet, h.kutsumanimi, h.sukunimi)
    )
  }
}

case class OmaOpintopolkuLokiHenkiloTiedot(
  hetu: Option[String],
  etunimet: String,
  kutsumanimi: String,
  sukunimi: String
)
