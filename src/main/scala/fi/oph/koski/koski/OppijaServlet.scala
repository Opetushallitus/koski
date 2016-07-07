package fi.oph.koski.koski

import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.henkilo.HenkiloOid
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.AuditLog.{log => auditLog}
import fi.oph.koski.log._
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{HenkilöWithOid, Oppija}
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import fi.oph.koski.util.Timing
import fi.vm.sade.security.ldap.DirectoryClient
import org.json4s.JsonAST.JArray
import org.scalatra.GZipSupport

class OppijaServlet(rekisteri: KoskiFacade, val käyttöoikeudet: KäyttöoikeusRepository, val directoryClient: DirectoryClient, val validator: KoskiValidator, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with ObservableSupport with GZipSupport with NoCache with Timing {

  put("/") {
    timed("PUT /oppija", thresholdMs = 10) {
      withJsonBody { parsedJson =>
        val validationResult: Either[HttpStatus, Oppija] = validator.extractAndValidate(parsedJson)(koskiUser, AccessType.write)
        val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = putSingle(validationResult, koskiUser)
        renderEither(result)
      }
    }
  }

  def putSingle(validationResult: Either[HttpStatus, Oppija], user: KoskiUser): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = { // <- user passed explicitly because this may be called in another thread (see batch below)
    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.right.flatMap(rekisteri.createOrUpdate(_)(user))
    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).warn("Opinto-oikeuden päivitys estetty: " + code + " " + errors + " for request " + describeRequest)
    }
    result
  }

  put("/batch") {
    timed("PUT /oppija/batch", thresholdMs = 10) {
      withJsonBody { parsedJson =>

        implicit val user = koskiUser

        val validationResults: List[Either[HttpStatus, Oppija]] = validator.extractAndValidateBatch(parsedJson.asInstanceOf[JArray])(user, AccessType.write)
        val batchResults: List[Either[HttpStatus, HenkilönOpiskeluoikeusVersiot]] = validationResults.par.map(putSingle(_, user)).toList

        response.setStatus(batchResults.map {
          case Left(status) => status.statusCode
          case _ => 200
        }.max)

        batchResults
      }
    }
  }

  get("/") {
    query
  }

  get("/:oid") {
    renderEither(findByOid(params("oid"), koskiUser))
  }

  get("/validate") {
    query.map(validateOppija)
  }

  get("/validate/:oid") {
    renderEither(
      findByOid(params("oid"), koskiUser)
        .right.flatMap(validateHistory)
        .right.map(validateOppija)
    )
  }

  get("/search") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        rekisteri.findOppijat(query.toUpperCase)(koskiUser)
      case _ =>
        throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }

  private def validateOppija(oppija: Oppija): ValidationResult = {
    val oppijaOid: Oid = oppija.henkilö.asInstanceOf[HenkilöWithOid].oid
    val validationResult = validator.validateAsJson(oppija)(koskiUser, AccessType.read)
    validationResult match {
      case Right(oppija) =>
        ValidationResult(oppijaOid, Nil)
      case Left(status) =>
        ValidationResult(oppijaOid, status.errors)
    }
  }

  private def validateHistory(oppija: Oppija): Either[HttpStatus, Oppija] = {
    HttpStatus.fold(oppija.opiskeluoikeudet.map { oikeus =>
      historyRepository.findVersion(oikeus.id.get, oikeus.versionumero.get)(koskiUser) match {
        case Right(latestVersion) =>
          HttpStatus.validate(latestVersion == oikeus) {
            KoskiErrorCategory.internalError(Json.toJValue(HistoryInconsistency(oikeus + " versiohistoria epäkonsistentti", Json.jsonDiff(oikeus, latestVersion))))
          }
        case Left(error) => error
      }
    }) match {
      case HttpStatus.ok => Right(oppija)
      case status: HttpStatus => Left(status)
    }
  }

  private def query = {
    logger(koskiUser).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    rekisteri.findOppijat(params.toList, koskiUser) match {
      case Right(oppijat) => oppijat
      case Left(status) => haltWithStatus(status)
    }
  }

  private def findByOid(oid: String, user: KoskiUser): Either[HttpStatus, Oppija] = {
    HenkiloOid.validateHenkilöOid(oid).right.flatMap { oid =>
      rekisteri.findOppija(oid)(user)
    }
  }
}

