package fi.oph.koski.koski

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluOikeusRow}
import fi.oph.koski.henkilo.HenkiloOid
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json.toJValue
import fi.oph.koski.koskiuser._
import fi.oph.koski.log._
import fi.oph.koski.schema.{Opiskeluoikeus, Oppija, TäydellisetHenkilötiedot}
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import fi.oph.koski.tiedonsiirto.TiedonsiirtoError
import fi.oph.koski.util.Timing
import org.json4s.{JArray, JValue}
import org.scalatra.GZipSupport
import rx.lang.scala.Observable

class OppijaServlet(val application: KoskiApplication)
  extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with GZipSupport with NoCache with Timing {

  put("/") {
    timed("PUT /oppija", thresholdMs = 10) {
      withJsonBody { (oppijaJson: JValue) =>
        val validationResult: Either[HttpStatus, Oppija] = application.validator.extractAndValidateOppija(oppijaJson)(koskiUser, AccessType.write)
        val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = UpdateContext(koskiUser, application, request).putSingle(validationResult, oppijaJson)
        renderEither(result)
      }(handleUnparseableJson)
    }
  }

  put("/batch") {
    timed("PUT /oppija/batch", thresholdMs = 10) {
      withJsonBody { parsedJson =>
        val putter = UpdateContext(koskiUser, application, request)

        val validationResults: List[(Either[HttpStatus, Oppija], JValue)] = application.validator.extractAndValidateBatch(parsedJson.asInstanceOf[JArray])(koskiUser, AccessType.write)

        val batchResults: List[Either[HttpStatus, HenkilönOpiskeluoikeusVersiot]] = validationResults.par.map { results =>
          putter.putSingle(results._1, results._2)
        }.toList

        response.setStatus(batchResults.map {
          case Left(status) => status.statusCode
          case _ => 200
        }.max)

        batchResults
      }(handleUnparseableJson)
    }
  }

  get("/") {
    query.map {
      case (henkilö, rivit) => Oppija(henkilö, rivit.map(_.toOpiskeluOikeus))
    }
  }

  get("/:oid") {
    renderEither(findByOid(params("oid"), koskiUser))
  }

  get("/search") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        application.facade.findOppijat(query.toUpperCase)(koskiUser)
      case _ =>
        throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }

  private def findByOid(oid: String, user: KoskiUser): Either[HttpStatus, Oppija] = {
    HenkiloOid.validateHenkilöOid(oid).right.flatMap { oid =>
      application.facade.findOppija(oid)(user)
    }
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    application.tiedonsiirtoService.storeTiedonsiirtoResult(koskiUser, None, None, Some(TiedonsiirtoError(toJValue(Map("unparseableJson" -> request.body)), toJValue(status.errors))))
    haltWithStatus(status)
  }
}

/**
  *  Operating context for data updates. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class UpdateContext(user: KoskiUser, application: KoskiApplication, request: HttpServletRequest) extends Logging {
  def putSingle(validationResult: Either[HttpStatus, Oppija], oppijaJson: JValue): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {

    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.right.flatMap(application.facade.createOrUpdate(_)(user))

    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).warn("Opinto-oikeuden päivitys estetty: " + code + " " + errors + " for request " + logSafeDescription(request))
    }

    application.tiedonsiirtoService.storeTiedonsiirtoResult(user, result.right.toOption.map(_.henkilö), Some(oppijaJson), result.fold(
      status => Some(TiedonsiirtoError(oppijaJson, toJValue(status.errors))),
      _ => None)
    )

    result
  }
}

trait OpiskeluoikeusQueries extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with ObservableSupport with GZipSupport {
  def application: KoskiApplication

  def query: Observable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = {
    logger(koskiUser).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    application.facade.findOppijat(params.toList, koskiUser) match {
      case Right(oppijat) => oppijat
      case Left(status) => haltWithStatus(status)
    }
  }
}