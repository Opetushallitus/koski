package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Unauthenticated}
import fi.oph.koski.log.KoskiAuditLogMessageField.oppijaHenkiloOid
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT}
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.suoritusjako.{AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä, OppijaJakolinkillä, SuoritetutTutkinnotOppijaJakolinkillä}
import fi.oph.koski.util.ChainingSyntax.chainingOps

import java.time.LocalDate

case class SuoritusjakoReadRequest(
  secret: String
)
class SuoritusjakoApiServlet(implicit application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  get("/suoritetut-tutkinnot/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    val result = application.suoritusjakoService.getSuoritetutTutkinnot(params("secret"))
      .tap(_.map(jakolinkki => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT, suoritusjakoUser, Map(oppijaHenkiloOid -> jakolinkki.henkilö.oid)))))
    renderEither[SuoritetutTutkinnotOppijaJakolinkillä](result)
  }

  post("/suoritetut-tutkinnot") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ json =>
      val body = JsonSerializer.extract[SuoritusjakoReadRequest](json)
      val result = application.suoritusjakoService.getSuoritetutTutkinnot(body.secret)
        .tap(_.map(jakolinkki => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT, suoritusjakoUser, Map(oppijaHenkiloOid -> jakolinkki.henkilö.oid)))))
      renderEither[SuoritetutTutkinnotOppijaJakolinkillä](result)
    })()
  }

  get("/aktiiviset-ja-paattyneet-opinnot/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    val result = application.suoritusjakoService.getAktiivisetJaPäättyneetOpinnot(params("secret"))
      .tap(_.map(jakolinkki => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, suoritusjakoUser, Map(oppijaHenkiloOid -> jakolinkki.henkilö.oid)))))
    renderEither[AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä](result)
  }

  post("/aktiiviset-ja-paattyneet-opinnot") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ json =>
      val body = JsonSerializer.extract[SuoritusjakoReadRequest](json)
      val result = application.suoritusjakoService.getAktiivisetJaPäättyneetOpinnot(body.secret)
        .tap(_.map(jakolinkki => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT, suoritusjakoUser, Map(oppijaHenkiloOid -> jakolinkki.henkilö.oid)))))
      renderEither[AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä](result)
    })()
  }

  get("/:secret") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    val result = application.suoritusjakoService.get(params("secret"))
    renderEither[OppijaJakolinkillä](result.map(_.getIgnoringWarnings))
  }

  post("/") {
    contentType = "application/json"
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ json =>
      val body = JsonSerializer.extract[SuoritusjakoReadRequest](json)
      val result = application.suoritusjakoService.get(body.secret)
      renderEither[OppijaJakolinkillä](result.map(_.getIgnoringWarnings))
    })()
  }
}
