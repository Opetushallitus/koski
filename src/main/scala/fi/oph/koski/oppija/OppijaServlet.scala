package fi.oph.koski.oppija

import javax.servlet.http.HttpServletRequest
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.{JsonSerializer, SensitiveDataFilter}
import fi.oph.koski.koskiuser._
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.schema._
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.tiedonsiirto.TiedonsiirtoError
import fi.oph.koski.util.{Pagination, Timing, WithWarnings, XML}
import fi.oph.koski.virta.VirtaHakuehtoHetu
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.{JArray, JValue}
import org.scalatra.ContentEncodingSupport

import scala.collection.immutable

class OppijaServlet(implicit val application: KoskiApplication) extends ApiServlet with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with ContentEncodingSupport with NoCache with Timing with Pagination {

  post("/") { putSingle(false) }

  put("/") { putSingle(true) }

  private def putSingle(allowUpdate: Boolean) = {
    withTracking { withJsonBody { (oppijaJson: JValue) =>
      val validationResult: Either[HttpStatus, Oppija] = application.validator.extractAndValidateOppija(oppijaJson)(koskiSession, AccessType.write)
      val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = UpdateContext(koskiSession, application, request).putSingle(validationResult, oppijaJson, allowUpdate)
      renderEither[HenkilönOpiskeluoikeusVersiot](result)
    }(parseErrorHandler = handleUnparseableJson)}
  }

  put("/batch") {
    withTracking { withJsonBody { parsedJson =>
      val putter = UpdateContext(koskiSession, application, request)

      val validationResults: List[(Either[HttpStatus, Oppija], JValue)] = application.validator.extractAndValidateBatch(parsedJson.asInstanceOf[JArray])(koskiSession, AccessType.write)

      val batchResults: List[Either[HttpStatus, HenkilönOpiskeluoikeusVersiot]] = validationResults.par.map { results =>
        putter.putSingle(results._1, results._2, true)
      }.toList

      response.setStatus(batchResults.map {
        case Left(status) => status.statusCode
        case _ => 200
      }.max)

      batchResults
    }(parseErrorHandler = handleUnparseableJson)}
  }

  get("/") {
    val serialize = SensitiveDataFilter(koskiSession).rowSerializer
    streamResponse(query.map(serialize), koskiSession)
  }

  get("/:oid") {
    renderEither[Oppija](findByOid(params("oid"), koskiSession).flatMap(_.warningsToLeft))
  }

  get("/:oid/virta-opintotiedot-xml") {
    if (!koskiSession.hasGlobalReadAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden())
    }
    findByOid(params("oid"), koskiSession)
      .flatMap(_.warningsToLeft)
      .flatMap { oppija: Oppija =>
        oppija.henkilö match {
          case h: Henkilötiedot if h.hetu.nonEmpty =>
            val hetu = h.hetu.get
            application.virtaClient.opintotiedot(VirtaHakuehtoHetu(hetu)) match {
              case Some(x) => Right(x)
              case None => Left(KoskiErrorCategory.notFound("Tyhjä vastaus?"))
            }
          case _ =>
            Left(KoskiErrorCategory.notFound("Hetu puuttuu?"))
      }
    } match {
      case Right(xml) =>
        contentType = "text/plain"
        response.writer.print(XML.prettyPrint(xml))
      case Left(status) =>
        haltWithStatus(status)
    }
  }

  get("/oids") {
    streamResponse[String](application.opiskeluoikeusQueryRepository.oppijaOidsQuery(paginationSettings)(koskiSession), koskiSession)
  }

  private def findByOid(oid: String, user: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    HenkilöOid.validateHenkilöOid(oid).right.flatMap { oid =>
      application.oppijaFacade.findOppija(oid)(user)
    }
  }

  private def handleUnparseableJson(status: HttpStatus) = {
    application.tiedonsiirtoService.storeTiedonsiirtoResult(koskiSession, None, None, None, Some(TiedonsiirtoError(JObject("unparseableJson" -> JString(request.body)), status.errors)))
    haltWithStatus(status)
  }


  private def withTracking[T](f: => T) = {
    if (koskiSession.isPalvelukäyttäjä) {
      application.ipService.trackIPAddress(koskiSession)
    }
    try {
      f
    } catch {
      case e: Exception =>
        application.tiedonsiirtoService.storeTiedonsiirtoResult(koskiSession, None, None, None, Some(TiedonsiirtoError(JObject("unparseableJson" -> JString(request.body)), KoskiErrorCategory.internalError().errors)))
        throw e
    }
  }
}

/**
  *  Operating context for data updates. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class UpdateContext(user: KoskiSession, application: KoskiApplication, request: HttpServletRequest) extends Logging {
  def putSingle(validationResult: Either[HttpStatus, Oppija], oppijaJsonFromRequest: JValue, allowUpdate: Boolean): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {

    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.right.flatMap(application.oppijaFacade.createOrUpdate(_, allowUpdate)(user))

    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).warn("Opiskeluoikeuden lisäys/päivitys estetty: " + code + " " + errors + " for request " + logSafeDescription(request))
    }

    application.tiedonsiirtoService.storeTiedonsiirtoResult(user, result.right.toOption.map(_.henkilö), validationResult.right.toOption, Some(oppijaJsonFromRequest), result.fold(
      status => Some(TiedonsiirtoError(oppijaJsonFromRequest, status.errors)),
      _ => None)
    )

    result
  }
}

