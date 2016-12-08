package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluOikeusRow}
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.oppija.ReportingQueryFacade
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.servlet.{ApiServlet, ObservableSupport}
import org.scalatra._
import rx.lang.scala.Observable

trait OpiskeluoikeusQueries extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with ObservableSupport with GZipSupport {
  def application: KoskiApplication

  def query: Observable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = {
    logger(koskiSession).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    OpiskeluoikeusQueryFilter.parseQueryFilter(params.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
      case Right(filters) =>
        AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> params.toList.map { case (p,v) => p + "=" + v }.mkString("&"))))
        ReportingQueryFacade(application.oppijaRepository, application.opiskeluOikeusRepository, application.koodistoViitePalvelu).findOppijat(filters, koskiSession)
      case Left(status) =>
        haltWithStatus(status)
    }
  }
}




