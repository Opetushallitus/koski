package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.EditorModel
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Unauthenticated}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_SUORITUSJAKO_KATSOMINEN, KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema.Oppija
import fi.oph.koski.suoritusjako.{SuoritusjakoRequest, SuoritusjakoSecret}
import fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOppija

case class SuoritusjakoReadRequest(
  secret: String
)
class SuoritusjakoApiServlet(implicit application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  get("/suoritetut-tutkinnot/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    val result = application.suoritusjakoService.getSuoritetutTutkinnot(params("secret"))
    AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT, suoritusjakoUser, Map()))
    renderEither[SuoritetutTutkinnotOppija](result)
  }

  post("/suoritetut-tutkinnot") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ json =>
      val body = JsonSerializer.extract[SuoritusjakoReadRequest](json)
      val result = application.suoritusjakoService.getSuoritetutTutkinnot(body.secret)
      AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT, suoritusjakoUser, Map()))
      renderEither[SuoritetutTutkinnotOppija](result)
    })()
  }

  get("/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    val result = application.suoritusjakoService.get(params("secret"))
    AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN, suoritusjakoUser, Map()))
    renderEither[Oppija](result.map(_.getIgnoringWarnings))
  }

  post("/") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ json =>
      val body = JsonSerializer.extract[SuoritusjakoReadRequest](json)
      val result = application.suoritusjakoService.get(body.secret)
      AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN, suoritusjakoUser, Map()))
      renderEither[Oppija](result.map(_.getIgnoringWarnings))
    })()
  }
}
