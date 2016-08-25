package fi.oph.koski.koski

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluOikeusRow}
import fi.oph.koski.henkilo.HenkiloOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json.toJValue
import fi.oph.koski.koskiuser._
import fi.oph.koski.log._
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{HenkilöWithOid, Oppija, TäydellisetHenkilötiedot}
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import fi.oph.koski.tiedonsiirto.TiedonsiirtoError
import fi.oph.koski.util.Timing
import org.json4s.{JArray, JValue}
import org.scalatra.GZipSupport
import rx.lang.scala.Observable

class OppijaServlet(val application: KoskiApplication)
  extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with ObservableSupport with GZipSupport with NoCache with Timing {

  put("/") {
    timed("PUT /oppija", thresholdMs = 10) {
      withJsonBody { parsedJson =>
        val validationResult: Either[HttpStatus, Oppija] = application.validator.extractAndValidate(parsedJson)(koskiUser, AccessType.write)
        val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = UpdateContext(koskiUser, application.facade, request).putSingle(validationResult)

        storeTiedonsiirtoResult(Some(parsedJson), result.fold(status => Some(TiedonsiirtoError(parsedJson, toJValue(status.errors))), _ => None))
        renderEither(result)

      }(handleUnparseableJson)
    }
  }

  put("/batch") {
    timed("PUT /oppija/batch", thresholdMs = 10) {
      withJsonBody { parsedJson =>
        val putter = UpdateContext(koskiUser, application.facade, request)

        val validationResults: List[Either[HttpStatus, Oppija]] = application.validator.extractAndValidateBatch(parsedJson.asInstanceOf[JArray])(koskiUser, AccessType.write)
        val batchResults: List[Either[HttpStatus, HenkilönOpiskeluoikeusVersiot]] = validationResults.par.map(putter.putSingle).toList

        response.setStatus(batchResults.map {
          case Left(status) => status.statusCode
          case _ => 200
        }.max)

        batchResults
      }(handleUnparseableJson)
    }
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    storeTiedonsiirtoResult(None, Some(TiedonsiirtoError(toJValue(Map("unparseableJson" -> request.body)), toJValue(status.errors))))
    haltWithStatus(status)
  }

  private def storeTiedonsiirtoResult(data: Option[JValue], error: Option[TiedonsiirtoError]) = {
    val oppija = data.map(_ \ "henkilö")
    val oppilaitos = data.map(_ \ "opiskeluoikeudet" \ "oppilaitos")
    koskiUser.juuriOrganisaatio.foreach(org => application.tiedonsiirtoRepository.create(koskiUser.oid, org.oid, oppija, oppilaitos, error))
  }

  get("/") {
    query.map {
      case (henkilö, rivit) => Oppija(henkilö, rivit.map(_.toOpiskeluOikeus))
    }
  }

  get("/:oid") {
    renderEither(findByOid(params("oid"), koskiUser))
  }

  get("/validate") {
    val context = ValidateContext(koskiUser, application.validator)
    query.map(context.validateOppija)
  }

  get("/validate/:oid") {
    renderEither(
      findByOid(params("oid"), koskiUser)
        .right.flatMap(validateHistory)
        .right.map(ValidateContext(koskiUser, application.validator).validateOppija)
    )
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



  private def validateHistory(oppija: Oppija): Either[HttpStatus, Oppija] = {
    HttpStatus.fold(oppija.opiskeluoikeudet.map { oikeus =>
      application.historyRepository.findVersion(oikeus.id.get, oikeus.versionumero.get)(koskiUser) match {
        case Right(latestVersion) =>
          HttpStatus.validate(latestVersion == oikeus) {
            KoskiErrorCategory.internalError(toJValue(HistoryInconsistency(oikeus + " versiohistoria epäkonsistentti", Json.jsonDiff(oikeus, latestVersion))))
          }
        case Left(error) => error
      }
    }) match {
      case HttpStatus.ok => Right(oppija)
      case status: HttpStatus => Left(status)
    }
  }

  private def query: Observable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = {
    logger(koskiUser).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    application.facade.findOppijat(params.toList, koskiUser) match {
      case Right(oppijat) => oppijat
      case Left(status) => haltWithStatus(status)
    }
  }

  private def findByOid(oid: String, user: KoskiUser): Either[HttpStatus, Oppija] = {
    HenkiloOid.validateHenkilöOid(oid).right.flatMap { oid =>
      application.facade.findOppija(oid)(user)
    }
  }
}

/**
  *  Operating context for data updates. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class UpdateContext(user: KoskiUser, facade: KoskiFacade, request: HttpServletRequest) extends Logging {
  def putSingle(validationResult: Either[HttpStatus, Oppija]): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.right.flatMap(facade.createOrUpdate(_)(user))
    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).warn("Opinto-oikeuden päivitys estetty: " + code + " " + errors + " for request " + logSafeDescription(request))
    }
    result
  }
}
/**
  *  Operating context for data validation. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class ValidateContext(user: KoskiUser, validator: KoskiValidator) {
  def validateOppija(oppija: Oppija): ValidationResult = {
    val oppijaOid: Oid = oppija.henkilö.asInstanceOf[HenkilöWithOid].oid
    val validationResult: Either[HttpStatus, Oppija] = validator.validateAsJson(oppija)(user, AccessType.read)
    toValidationResult(oppijaOid, validationResult)
  }

  private def toValidationResult(oppijaOid: String, validationResult: Either[HttpStatus, Oppija]) = validationResult match {
    case Right(oppija) =>
      ValidationResult(oppijaOid, Nil)
    case Left(status) =>
      ValidationResult(oppijaOid, status.errors)
  }

  def validateOppija(oppija: (TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])): ValidationResult = oppija match {
    case (henkilö, rivit) =>
      val oppijaOid: Oid = henkilö.asInstanceOf[HenkilöWithOid].oid
      val fullJson = toJValue(Map(
        "henkilö" -> henkilö,
        "opiskeluoikeudet" -> rivit.map(_.data)
      ))
      val validationResult = validator.extractAndValidate(fullJson)(user, AccessType.read)
      toValidationResult(oppijaOid, validationResult)
  }
}