package fi.oph.koski.oppija

import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.servlet.http.HttpServletRequest

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluOikeusRow}
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json.toJValue
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.schema._
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import fi.oph.koski.tiedonsiirto.TiedonsiirtoError
import fi.oph.koski.util.Timing
import org.json4s.{JArray, JValue}
import org.scalatra.GZipSupport
import rx.lang.scala.Observable

class OppijaServlet(val application: KoskiApplication)
  extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with GZipSupport with NoCache with Timing {

  put("/") {
    timed("PUT /oppija", thresholdMs = 10) {
      storeTiedonsiirtoResultInCaseOfException { withJsonBody { (oppijaJson: JValue) =>
        val validationResult: Either[HttpStatus, Oppija] = application.validator.extractAndValidateOppija(oppijaJson)(koskiSession, AccessType.write)
        val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = UpdateContext(koskiSession, application, request).putSingle(validationResult, oppijaJson)
        renderEither(result)
      }(parseErrorHandler = handleUnparseableJson)}
    }
  }

  put("/batch") {
    timed("PUT /oppija/batch", thresholdMs = 10) {
      storeTiedonsiirtoResultInCaseOfException { withJsonBody { parsedJson =>
        val putter = UpdateContext(koskiSession, application, request)

        val validationResults: List[(Either[HttpStatus, Oppija], JValue)] = application.validator.extractAndValidateBatch(parsedJson.asInstanceOf[JArray])(koskiSession, AccessType.write)

        val batchResults: List[Either[HttpStatus, HenkilönOpiskeluoikeusVersiot]] = validationResults.par.map { results =>
          putter.putSingle(results._1, results._2)
        }.toList

        response.setStatus(batchResults.map {
          case Left(status) => status.statusCode
          case _ => 200
        }.max)

        batchResults
      }(parseErrorHandler = handleUnparseableJson)}
    }
  }

  get("/") {
    query.map {
      case (henkilö, rivit) => Oppija(henkilö, rivit.map(_.toOpiskeluOikeus))
    }
  }

  get("/:oid") {
    renderEither(findByOid(params("oid"), koskiSession))
  }


  private def findByOid(oid: String, user: KoskiSession): Either[HttpStatus, Oppija] = {
    HenkilöOid.validateHenkilöOid(oid).right.flatMap { oid =>
      application.oppijaFacade.findOppija(oid)(user)
    }
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    application.tiedonsiirtoService.storeTiedonsiirtoResult(koskiSession, None, None, None, Some(TiedonsiirtoError(toJValue(Map("unparseableJson" -> request.body)), toJValue(status.errors))))
    haltWithStatus(status)
  }


  private def storeTiedonsiirtoResultInCaseOfException[T](f: => T) = {
    try {
      f
    } catch {
      case e: Exception =>
        application.tiedonsiirtoService.storeTiedonsiirtoResult(koskiSession, None, None, None, Some(TiedonsiirtoError(toJValue(Map("unparseableJson" -> request.body)), toJValue(KoskiErrorCategory.internalError().errors))))
        throw e
    }
  }

}

/**
  *  Operating context for data updates. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class UpdateContext(user: KoskiSession, application: KoskiApplication, request: HttpServletRequest) extends Logging {
  def putSingle(validationResult: Either[HttpStatus, Oppija], oppijaJsonFromRequest: JValue): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {

    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.right.flatMap(application.oppijaFacade.createOrUpdate(_)(user))

    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).warn("Opinto-oikeuden päivitys estetty: " + code + " " + errors + " for request " + logSafeDescription(request))
    }

    application.tiedonsiirtoService.storeTiedonsiirtoResult(user, result.right.toOption.map(_.henkilö), validationResult.right.toOption, Some(oppijaJsonFromRequest), result.fold(
      status => Some(TiedonsiirtoError(oppijaJsonFromRequest, toJValue(status.errors))),
      _ => None)
    )

    result
  }
}



