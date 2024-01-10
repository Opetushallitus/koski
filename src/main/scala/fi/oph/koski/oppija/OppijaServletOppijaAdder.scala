package fi.oph.koski.oppija

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.lenientDeserializationWithoutValidation
import fi.oph.koski.schema.Oppija
import fi.oph.koski.tiedonsiirto.TiedonsiirtoError
import org.json4s.JsonAST.JBool
import org.json4s.{DefaultFormats, JValue}

class OppijaServletOppijaAdder(application: KoskiApplication) {
  def add(
    session: KoskiSpecificSession,
    oppijaJson: JValue,
    allowUpdate: Boolean,
    requestDescription: => String
  ): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val cleanedJson = cleanForTesting(oppijaJson)
    val validationResult: Either[HttpStatus, Oppija] = if (application.validationContext.validoiOpiskeluoikeudet && !skipValidation(oppijaJson)) {
      application.validator.extractUpdateFieldsAndValidateOppija(cleanedJson)(session, AccessType.write)
    } else {
      application.validator.extractOppija(cleanedJson, lenientDeserializationWithoutValidation)
    }
    UpdateContext(session, application).putSingle(validationResult, cleanedJson, allowUpdate, requestDescription)
  }

  private def cleanForTesting(oppijaJson: JValue): JValue =
    onlyOnMockEnvironment(oppijaJson) {
      val cleanForTesting = extract[Boolean]((oppijaJson \ "cleanForTesting").map {
        case pass: JBool => pass
        case _ => JBool(false)
      })

      if (cleanForTesting) {
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

  private def skipValidation(oppijaJson: JValue): Boolean =
    onlyOnMockEnvironment(false) {
      extract[Boolean]((oppijaJson \ "ignoreKoskiValidator").map {
        case pass: JBool => pass
        case _ => JBool(false)
      })
    }

  private def onlyOnMockEnvironment[T](default: T)(f: => T): T =
    if (Environment.isMockEnvironment(application.config)) f else default
}

/**
 *  Operating context for data updates. Operates outside the lecixal scope of OppijaServlet to ensure that none of the
 *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
 */
case class UpdateContext(user: KoskiSpecificSession, application: KoskiApplication) extends Logging {
  def putSingle(validationResult: Either[HttpStatus, Oppija], oppijaJsonFromRequest: JValue, allowUpdate: Boolean, getRequestDescription: => String): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {

    validationResult.foreach(_.tallennettavatOpiskeluoikeudet.filter(_.lähdejärjestelmänId.isDefined).flatMap(_.versionumero).foreach(_ => logger.info("Lähdejärjestelmä siirsi versionumeron")))

    val result: Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = validationResult.flatMap(application.oppijaFacade.createOrUpdate(_, allowUpdate)(user))

    result.left.foreach { case HttpStatus(code, errors) =>
      logger(user).info("Opiskeluoikeuden lisäys/päivitys estetty: " + code + " " + errors + " for request " + getRequestDescription)
    }

    val error = result.left.toOption.map(status => TiedonsiirtoError(oppijaJsonFromRequest, status.errors))
    application.tiedonsiirtoService
      .storeTiedonsiirtoResult(user, result.toOption.map(_.henkilö), validationResult.toOption, Some(oppijaJsonFromRequest), error)

    result
  }
}

