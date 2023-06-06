package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Unauthenticated}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT, KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.schema.Oppija
import fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppija
import fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOppija
import fi.oph.koski.util.ChainingSyntax.chainingOps

case class SuoritusjakoReadRequest(
  secret: String
)
class SuoritusjakoApiServlet(implicit application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  get("/suoritetut-tutkinnot/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    val result = application.suoritusjakoService.getSuoritetutTutkinnot(params("secret"))
      .tap(_ => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT, suoritusjakoUser, Map())))
    renderEither[SuoritetutTutkinnotOppija](result)
  }

  post("/suoritetut-tutkinnot") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ json =>
      val body = JsonSerializer.extract[SuoritusjakoReadRequest](json)
      val result = application.suoritusjakoService.getSuoritetutTutkinnot(body.secret)
        .tap(_ => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT, suoritusjakoUser, Map())))
      renderEither[SuoritetutTutkinnotOppija](result)
    })()
  }

  get("/aktiiviset-ja-paattyneet-opinnot/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    val result = application.suoritusjakoService.getAktiivisetJaPäättyneetOpinnot(params("secret"))
      .tap(_ => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, suoritusjakoUser, Map())))
    renderEither[AktiivisetJaPäättyneetOpinnotOppija](result)
  }

  post("/aktiiviset-ja-paattyneet-opinnot") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ json =>
      val body = JsonSerializer.extract[SuoritusjakoReadRequest](json)
      val result = application.suoritusjakoService.getAktiivisetJaPäättyneetOpinnot(body.secret)
        .tap(_ => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, suoritusjakoUser, Map())))
      renderEither[AktiivisetJaPäättyneetOpinnotOppija](result)
    })()
  }

  get("/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    val result = application.suoritusjakoService.get(params("secret"))
    renderEither[Oppija](result.map(_.getIgnoringWarnings))
  }

  post("/") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ json =>
      val body = JsonSerializer.extract[SuoritusjakoReadRequest](json)
      val result = application.suoritusjakoService.get(body.secret)
      renderEither[Oppija](result.map(_.getIgnoringWarnings))
    })()
  }
}
