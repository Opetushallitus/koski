package fi.oph.koski.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluOikeusRow}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.Timing
import org.scalatra._
import rx.lang.scala.Observable

class OpiskeluoikeusServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with GZipSupport with NoCache with Timing{
  get("/:id") {
    val findResult: Either[HttpStatus, OpiskeluOikeusRow] = application.facade.findOpiskeluOikeus(getIntegerParam("id"))(koskiUser)
    val result: Either[HttpStatus, Opiskeluoikeus] = findResult
      .right.map(_.toOpiskeluOikeus)
    renderEither(result)
  }

  get("/validate") {
    val context = ValidateContext(koskiUser, application.validator, application.historyRepository)
    query.flatMap { case (henkilö, opiskeluoikeudet) => Observable.from(opiskeluoikeudet) }.map(context.validateOpiskeluoikeus)
  }

  get("/validate/:id") {
    val context = ValidateContext(koskiUser, application.validator, application.historyRepository)
    val findResult: Either[HttpStatus, OpiskeluOikeusRow] = application.facade.findOpiskeluOikeus(getIntegerParam("id"))(koskiUser)
    val result: Either[HttpStatus, ValidationResult] = findResult
      .right.flatMap(context.validateHistory)
      .right.map(context.validateOpiskeluoikeus)
    renderEither(result)
  }
}
