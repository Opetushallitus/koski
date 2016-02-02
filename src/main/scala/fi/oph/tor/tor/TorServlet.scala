package fi.oph.tor.tor

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.tor.db.GlobalExecutionContext
import fi.oph.tor.henkilo.HenkiloOid
import fi.oph.tor.history.OpiskeluoikeusHistoryRepository
import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.json.{Json, JsonStreamWriter}
import fi.oph.tor.log.Logging
import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.schema.{Henkilö, HenkilöWithOid, TorOppija}
import fi.oph.tor.servlet.{ErrorHandlingServlet, InvalidRequestException, NoCache}
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient
import org.scalatra.{FutureSupport, GZipSupport}
import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class TorServlet(rekisteri: TodennetunOsaamisenRekisteri, val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val validator: TorValidator, val historyRepository: OpiskeluoikeusHistoryRepository)
  extends ErrorHandlingServlet with Logging with RequiresAuthentication with GlobalExecutionContext with FutureSupport with GZipSupport with NoCache {

  put("/") {
    withJsonBody { parsedJson =>
      val validationResult: Either[HttpStatus, TorOppija] = validator.extractAndValidate(parsedJson)
      val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.right.flatMap (rekisteri.createOrUpdate _)

      result.left.foreach { case HttpStatus(code, errors) =>
        logger.warn("Opinto-oikeuden päivitys estetty: " + code + " " + errors + " for request " + describeRequest)
      }
      renderEither(result)
    }
  }

  get("/") {
    query { oppijat =>
      JsonStreamWriter.writeJsonStream(oppijat)(this, Json.jsonFormats)
    }
  }

  get("/:oid") {
    renderEither(findByOid)
  }

  get("/validate") {
    query { oppijat =>
      val validationResults = oppijat.map(validateOppija)
      JsonStreamWriter.writeJsonStream(validationResults)(this, Json.jsonFormats)
    }
  }

  get("/validate/:oid") {
    renderEither(
      findByOid
        .right.flatMap(validateHistory)
        .right.map(validateOppija)
    )
  }

  get("/search") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        Json.write(rekisteri.findOppijat(query.toUpperCase))
      case _ =>
        throw new InvalidRequestException(TorErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }

  override protected def asyncTimeout = Duration.Inf

  private def validateOppija(oppija: TorOppija): ValidationResult = {
    val oppijaOid: Oid = oppija.henkilö.asInstanceOf[HenkilöWithOid].oid
    val validationResult = validator.validateAsJson(oppija)
    validationResult match {
      case Right(oppija) =>
        ValidationResult(oppijaOid, Nil)
      case Left(status) =>
        ValidationResult(oppijaOid, status.errors)
    }
  }

  private def validateHistory(oppija: TorOppija): Either[HttpStatus, TorOppija] = {
    HttpStatus.fold(oppija.opiskeluoikeudet.map { oikeus =>
      historyRepository.findVersion(oikeus.id.get, oikeus.versionumero.get) match {
        case Right(latestVersion) =>
          HttpStatus.validate(latestVersion == oikeus) {
            TorErrorCategory.internalError("Opinto-oikeuden " + oikeus.id + " versiohistoria epäkonsistentti")
          }
        case Left(error) => error
      }
    }) match {
      case HttpStatus.ok => Right(oppija)
      case status => Left(status)
    }
  }

  private def query(handleResults: Observable[TorOppija] => Future[String]) = {
    logger.info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    val queryFilters: List[Either[HttpStatus, QueryFilter]] = params.toList.map {
      case (p, v) if p == "opiskeluoikeusPäättynytAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytAikaisintaan(_))
      case (p, v) if p == "opiskeluoikeusPäättynytViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytViimeistään(_))
      case ("tutkinnonTila", v) => Right(TutkinnonTila(v))
      case (p, _) => Left(TorErrorCategory.badRequest.queryParam.unknown("Unsupported query parameter: " + p))
    }

    queryFilters.partition(_.isLeft) match {
      case (Nil, queries) =>
        val filters = queries.map(_.right.get)
        val oppijat: Observable[TorOppija] = rekisteri.findOppijat(filters)
        handleResults(oppijat)
      case (errors, _) =>
        renderStatus(HttpStatus.fold(errors.map(_.left.get)))
    }
  }

  private def findByOid: Either[HttpStatus, TorOppija] = {
    HenkiloOid.validateHenkilöOid(params("oid")).right.flatMap { oid =>
      rekisteri.findTorOppija(oid)
    }
  }

  private def dateParam(q: (String, String)): Either[HttpStatus, LocalDate] = q match {
    case (p, v) => try {
      Right(LocalDate.parse(v))
    } catch {
      case e: DateTimeParseException => Left(TorErrorCategory.badRequest.format.pvm("Invalid date parameter: " + p + "=" + v))
    }
  }
}

trait QueryFilter

case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends QueryFilter
case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends QueryFilter
case class TutkinnonTila(tila: String) extends QueryFilter
case class ValidationResult(oid: Henkilö.Oid, errors: List[AnyRef])