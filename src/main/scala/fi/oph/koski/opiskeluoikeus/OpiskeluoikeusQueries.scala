package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluOikeusRow}
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.oppija.ReportingQueryFacade
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid, TäydellisetHenkilötiedot}
import fi.oph.koski.servlet.{ApiServlet, ObservableSupport}
import org.scalatra._
import rx.lang.scala.Observable

trait OpiskeluoikeusQueries extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with ObservableSupport with GZipSupport {
  def application: KoskiApplication

  def query: Observable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = {
    logger(koskiSession).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    OpiskeluoikeusQueryParamParser(application.koodistoViitePalvelu).queryFilters(params.toList) match {
      case Right(filters) =>
        AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> params.toList.map { case (p,v) => p + "=" + v }.mkString("&"))))
        ReportingQueryFacade(application.oppijaRepository, application.opiskeluOikeusRepository, application.koodistoViitePalvelu).findOppijat(filters, koskiSession)
      case Left(status) =>
        haltWithStatus(status)
    }
  }
}

sealed trait OpiskeluoikeusQueryFilter

case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
case class OpiskeluoikeusAlkanutAikaisintaan(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
case class OpiskeluoikeusAlkanutViimeistään(päivä: LocalDate) extends OpiskeluoikeusQueryFilter
case class TutkinnonTila(tila: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
case class Nimihaku(hakusana: String) extends OpiskeluoikeusQueryFilter
case class OpiskeluoikeudenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
case class SuorituksenTyyppi(tyyppi: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
case class KoulutusmoduulinTunniste(tunniste: List[Koodistokoodiviite]) extends OpiskeluoikeusQueryFilter
case class Osaamisala(osaamisala: List[Koodistokoodiviite]) extends OpiskeluoikeusQueryFilter
case class Tutkintonimike(nimike: List[Koodistokoodiviite]) extends OpiskeluoikeusQueryFilter
case class OpiskeluoikeudenTila(tila: Koodistokoodiviite) extends OpiskeluoikeusQueryFilter
case class Toimipiste(toimipiste: List[OrganisaatioWithOid]) extends OpiskeluoikeusQueryFilter
case class Luokkahaku(hakusana: String) extends OpiskeluoikeusQueryFilter

