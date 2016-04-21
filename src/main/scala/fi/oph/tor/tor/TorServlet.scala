package fi.oph.tor.tor

import fi.oph.tor.db.GlobalExecutionContext
import fi.oph.tor.henkilo.HenkiloOid
import fi.oph.tor.history.OpiskeluoikeusHistoryRepository
import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.json.{Json, JsonStreamWriter}
import fi.oph.tor.log.AuditLog.{log => auditLog}
import fi.oph.tor.log._
import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.schema.{HenkilöWithOid, Oppija}
import fi.oph.tor.servlet.{ErrorHandlingServlet, InvalidRequestException, NoCache}
import fi.oph.tor.toruser.{RequiresAuthentication, TorUser, UserOrganisationsRepository}
import fi.oph.tor.util.Timing
import fi.vm.sade.security.ldap.DirectoryClient
import org.json4s.JsonAST.JArray
import org.scalatra.{FutureSupport, GZipSupport}
import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class TorServlet(rekisteri: TodennetunOsaamisenRekisteri, val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val validator: TorValidator, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ErrorHandlingServlet with Logging with RequiresAuthentication with GlobalExecutionContext with FutureSupport with GZipSupport with NoCache with Timing {

  put("/") {
    timed("PUT /oppija", thresholdMs = 10) {
      withJsonBody { parsedJson =>
        val validationResult: Either[HttpStatus, Oppija] = validator.extractAndValidate(parsedJson)(torUser)
        val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = putSingle(validationResult, torUser)
        renderEither(result)
      }
    }
  }

  def putSingle(validationResult: Either[HttpStatus, Oppija], user: TorUser): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.right.flatMap(rekisteri.createOrUpdate(_)(user))

    result.left.foreach { case HttpStatus(code, errors) =>
      logger.warn("Opinto-oikeuden päivitys estetty: " + code + " " + errors + " for request " + describeRequest)
    }
    result
  }

  put("/batch") {
    timed("PUT /oppija/batch", thresholdMs = 10) {
      withJsonBody { parsedJson =>

        implicit val user = torUser

        val validationResults: List[Either[HttpStatus, Oppija]] = validator.extractAndValidateBatch(parsedJson.asInstanceOf[JArray])
        val batchResults: List[Either[HttpStatus, HenkilönOpiskeluoikeusVersiot]] = validationResults.par.map(putSingle(_, user)).toList

        response.setStatus(batchResults.map {
          case Left(status) => status.statusCode
          case _ => 200
        }.max)

        Json.write(batchResults)
      }
    }
  }

  get("/") {
    query { oppijat =>
      JsonStreamWriter.writeJsonStream(oppijat)(this, Json.jsonFormats)
    }
  }

  get("/:oid") {
    renderEither(findByOid(params("oid"), torUser))
  }

  get("/validate") {
    query { oppijat =>
      val validationResults = oppijat.map(validateOppija)
      JsonStreamWriter.writeJsonStream(validationResults)(this, Json.jsonFormats)
    }
  }

  get("/validate/:oid") {
    renderEither(
      findByOid(params("oid"), torUser)
        .right.flatMap(validateHistory)
        .right.map(validateOppija)
    )
  }

  get("/search") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        Json.write(rekisteri.findOppijat(query.toUpperCase)(torUser))
      case _ =>
        throw new InvalidRequestException(TorErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }

  override protected def asyncTimeout = Duration.Inf

  private def validateOppija(oppija: Oppija): ValidationResult = {
    val oppijaOid: Oid = oppija.henkilö.asInstanceOf[HenkilöWithOid].oid
    val validationResult = validator.validateAsJson(oppija)(torUser)
    validationResult match {
      case Right(oppija) =>
        ValidationResult(oppijaOid, Nil)
      case Left(status) =>
        ValidationResult(oppijaOid, status.errors)
    }
  }

  private def validateHistory(oppija: Oppija): Either[HttpStatus, Oppija] = {
    HttpStatus.fold(oppija.opiskeluoikeudet.map { oikeus =>
      historyRepository.findVersion(oikeus.id.get, oikeus.versionumero.get)(torUser) match {
        case Right(latestVersion) =>
          HttpStatus.validate(latestVersion == oikeus) {
            TorErrorCategory.internalError(Json.toJValue(HistoryInconsistency(oikeus + " versiohistoria epäkonsistentti", Json.jsonDiff(oikeus, latestVersion))))
          }
        case Left(error) => error
      }
    }) match {
      case HttpStatus.ok => Right(oppija)
      case status: HttpStatus => Left(status)
    }
  }

  private def query(handleResults: Observable[Oppija] => Future[String]) = {
    logger.info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    rekisteri.findOppijat(params.toList, torUser) match {
      case Right(oppijat) => handleResults(oppijat)
      case Left(status) => renderStatus(status)
    }
  }

  private def findByOid(oid: String, user: TorUser): Either[HttpStatus, Oppija] = {
    HenkiloOid.validateHenkilöOid(oid).right.flatMap { oid =>
      rekisteri.findTorOppija(oid)(user)
    }
  }
}