package fi.oph.koski.oppija

import fi.oph.koski.config.{KoskiApplication}
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.SensitiveAndRedundantDataFilter
import fi.oph.koski.koskiuser._
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.schema.KoskiSchema.{lenientDeserializationWithoutValidation}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.tiedonsiirto.TiedonsiirtoError
import fi.oph.koski.util.{Pagination, Timing, XML}
import fi.oph.koski.virta.{VirtaHakuehtoHetu, VirtaHakuehtoKansallinenOppijanumero}

import javax.servlet.http.HttpServletRequest
import org.json4s.JsonAST.{JBool, JObject, JString}
import org.json4s.{DefaultFormats, JArray, JValue}
import org.scalatra.ContentEncodingSupport

class OppijaServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with Logging
    with OpiskeluoikeusQueries
    with RequiresVirkailijaOrPalvelukäyttäjä
    with ContentEncodingSupport
    with NoCache
    with Timing
    with Pagination {

  post("/") { putSingle(false) }

  put("/") { putSingle(true) }

  private def putSingle(allowUpdate: Boolean) = {
    withTracking { withJsonBody { (oppijaJson: JValue) =>
      val cleanedJson = cleanForTesting(oppijaJson)
      val validationResult: Either[HttpStatus, Oppija] = if (skipValidation(cleanedJson)) {
        application.validator.extractOppija(cleanedJson, lenientDeserializationWithoutValidation)
      } else {
        application.validator.extractAndValidateOppija(cleanedJson)(session, AccessType.write)
      }
      val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = UpdateContext(session, application, request).putSingle(validationResult, cleanedJson, allowUpdate)
      renderEither[HenkilönOpiskeluoikeusVersiot](result)
    }(parseErrorHandler = handleUnparseableJson)}
  }

  put("/batch") {
    withTracking { withJsonBody { parsedJson =>
      val putter = UpdateContext(session, application, request)

      val validationResults: List[(Either[HttpStatus, Oppija], JValue)] = application.validator.extractAndValidateBatch(parsedJson.asInstanceOf[JArray])(session, AccessType.write)

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
    val serializer = SensitiveAndRedundantDataFilter(session).rowSerializer

    val oppijat = performOpiskeluoikeudetQueryLaajoillaHenkilötiedoilla.map(observable => observable
      .map(x => (application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(x._1), x._2))
      .map(serializer)
    )

    streamResponse(oppijat, session)
  }

  // TODO: tarkista lokeista voiko tämän poistaa
  get("/:oid") {
    renderEither[Oppija](HenkilöOid.validateHenkilöOid(params("oid")).right.flatMap { oid =>
      application.oppijaFacade.findOppija(oid, findMasterIfSlaveOid = false)(session)
    }.flatMap(_.warningsToLeft))
  }

  get("/:oid/opintotiedot-json") {
    renderEither[Oppija](HenkilöOid.validateHenkilöOid(params("oid")).right.flatMap { oid =>
      application.oppijaFacade.findOppija(oid, findMasterIfSlaveOid = true)(session)
    }.flatMap(_.warningsToLeft))
  }

  get("/:oid/virta-opintotiedot-xml") {
    if (!session.hasGlobalReadAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden())
    }
    virtaOpinnot(params("oid")) match {
      case Right(elements) =>
        contentType = "text/plain"
        response.writer.print(XML.prettyPrintNodes(elements))
      case Left(status) => haltWithStatus(status)
    }
  }

  get("/oids") {
    streamResponse[String](application.opiskeluoikeusQueryRepository.oppijaOidsQuery(paginationSettings)(session), session)
  }

  private def virtaOpinnot(oid: String) =
    application.opintopolkuHenkilöFacade.findMasterOppija(oid).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydy()).map { oppijaHenkilö =>
      val byHetu = (oppijaHenkilö.hetu.toList ++ oppijaHenkilö.vanhatHetut).sorted.map(VirtaHakuehtoHetu)
      val byOid = (oppijaHenkilö.oid :: oppijaHenkilö.linkitetytOidit).sorted.map(VirtaHakuehtoKansallinenOppijanumero)
      application.virtaClient.opintotiedotMassahaku(byHetu) ++ application.virtaClient.opintotiedotMassahaku(byOid)
    }.map(_.toList.distinct)

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
        logger.error(e)("virhe aiheutti unparseableJson merkinnän tiedonsiirtoihin")
        throw e
    }
  }

  private def skipValidation(oppijaJson: JValue) = {
    val ignoreFlagInJson = extract[Boolean]((oppijaJson \ "ignoreKoskiValidator").map {
      case pass: JBool => pass
      case _ => JBool(false)
    })

    loginEnvIsMock && ignoreFlagInJson
  }

  private def cleanForTesting(oppijaJson: JValue) = {
    val cleanForTesting = extract[Boolean]((oppijaJson \ "cleanForTesting").map {
      case pass: JBool => pass
      case _ => JBool(false)
    })

    val shouldClean = loginEnvIsMock && cleanForTesting

    if (shouldClean) {
      implicit val formats = DefaultFormats
      oppijaJson.replace(List("henkilö"), (oppijaJson \ "henkilö").removeField {
        case ("oid", _) => true
        case ("kansalaisuus", _) => true
        case ("äidinkieli", _) => true
        case ("syntymäaika", _) => true
        case ("turvakielto", _) => true
        case _ => false
      })
    } else {
      oppijaJson
    }
  }

  private def loginEnvIsMock = {
    val sec = application.config.getString("login.security")
    sec == "mock"
  }
}

/**
  *  Operating context for data updates. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class UpdateContext(user: KoskiSpecificSession, application: KoskiApplication, request: HttpServletRequest) extends Logging {
  def putSingle(validationResult: Either[HttpStatus, Oppija], oppijaJsonFromRequest: JValue, allowUpdate: Boolean): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {

    validationResult.foreach(_.tallennettavatOpiskeluoikeudet.filter(_.lähdejärjestelmänId.isDefined).flatMap(_.versionumero).foreach(_ => logger.info("Lähdejärjestelmä siirsi versionumeron")))

    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.flatMap(application.oppijaFacade.createOrUpdate(_, allowUpdate)(user))

    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).info("Opiskeluoikeuden lisäys/päivitys estetty: " + code + " " + errors + " for request " + logSafeDescription(request))
    }

    val error = result.left.toOption.map(status => TiedonsiirtoError(oppijaJsonFromRequest, status.errors))
    application.tiedonsiirtoService
      .storeTiedonsiirtoResult(user, result.toOption.map(_.henkilö), validationResult.toOption, Some(oppijaJsonFromRequest), error)

    result
  }
}

