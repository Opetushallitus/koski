package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiCookieAndBasicAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.log.KoskiOperation.TODISTUKSEN_LUONTI
import fi.oph.koski.log.KoskiAuditLogMessageField
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.ChainingSyntax._

class TodistusApiServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with TodistusServlet
    with NoCache
    with KoskiCookieAndBasicAuthenticationSupport
{
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireKansalainenOrOphPääkäyttäjä
  }

  get("/status/:id") {
    renderEither(
      getIdRequest
        .flatMap(service.currentStatus)
    )
  }

  get("/status/:lang/:opiskeluoikeusOid") {
    renderEither(
      getTodistusGenerateRequest
        .flatMap(service.checkStatus)
    )
  }

  get("/generate/:lang/:opiskeluoikeusOid") {
    renderEither(
      getTodistusGenerateRequest
        .flatMap(service.checkAccessAndInitiateGenerating)
        .tap(todistusJob => mkAuditLog(
          operation = TODISTUKSEN_LUONTI,
          extraFields = Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid -> todistusJob.oppijaOid,
            KoskiAuditLogMessageField.opiskeluoikeusOid -> todistusJob.opiskeluoikeusOid,
            KoskiAuditLogMessageField.todistusId -> todistusJob.id
          )
        ))
    )
  }

}

case class TodistusGenerateRequest(
  opiskeluoikeusOid: String,
  language: String,
) {
  def toPathParams = s"${language}/${opiskeluoikeusOid}"
}

case class TodistusIdRequest(
  id: String
)
