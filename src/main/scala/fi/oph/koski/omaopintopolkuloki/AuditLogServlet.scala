package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class AuditLogServlet(implicit val application: KoskiApplication) extends
  RequiresKansalainen with ApiServlet with NoCache {

  val auditLogs = new AuditLogService(application.organisaatioRepository, AuditLogDynamoDB.db)

  get("/") {
    renderEither(
      auditLogs.queryLogsFromDynamo(koskiSession.oid)
    )
  }
}
