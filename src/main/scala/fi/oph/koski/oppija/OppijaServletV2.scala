package fi.oph.koski.oppija

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.tiedonsiirto.TiedonsiirtoError
import javax.servlet.http.HttpServletRequest
import org.json4s.JValue
import org.json4s.JsonAST.{JObject, JString}

class OppijaServletV2(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  post("/") { putSingle(false) }

  put("/") { putSingle(true) }

  private def putSingle(allowUpdate: Boolean) = {
    withTracking { withJsonBody { (oppijaJson: JValue) =>
      val validationResult: Either[HttpStatus, Oppija] = application.validator.extractUpdateFieldsAndValidateOppija(oppijaJson)(session, AccessType.write)
      val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = UpdateContextV2(session, application, request).putSingle(validationResult, oppijaJson, allowUpdate)
      renderEither[HenkilönOpiskeluoikeusVersiot](result)
    }(parseErrorHandler = handleUnparseableJson)}
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    application.tiedonsiirtoService.storeTiedonsiirtoResult(session, None, None, None, Some(TiedonsiirtoError(JObject("unparseableJson" -> JString(request.body)), status.errors)))
    haltWithStatus(status)
  }

  private def withTracking[T](f: => T) = {
    if (session.isPalvelukäyttäjä) {
      application.ipService.trackIPAddress(session)
    }
    try {
      f
    } catch {
      case e: Exception =>
        application.tiedonsiirtoService.storeTiedonsiirtoResult(session, None, None, None, Some(TiedonsiirtoError(JObject("unparseableJson" -> JString(request.body)), KoskiErrorCategory.internalError().errors)))
        throw e
    }
  }
}

/**
  *  Operating context for data updates. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class UpdateContextV2(user: KoskiSpecificSession, application: KoskiApplication, request: HttpServletRequest) extends Logging {
  def putSingle(validationResult: Either[HttpStatus, Oppija], oppijaJsonFromRequest: JValue, allowUpdate: Boolean): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {

    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.flatMap(application.oppijaFacadeV2.createOrUpdate(_, allowUpdate)(user))

    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).info("Opiskeluoikeuden lisäys/päivitys estetty: " + code + " " + errors + " for request " + logSafeDescription(request))
    }

    val error = result.left.toOption.map(status => TiedonsiirtoError(oppijaJsonFromRequest, status.errors))
    application.tiedonsiirtoService
      .storeTiedonsiirtoResult(user, result.toOption.map(_.henkilö), validationResult.toOption, Some(oppijaJsonFromRequest), error)

    result
  }
}
