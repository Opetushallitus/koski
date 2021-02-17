package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http._
import fi.oph.koski.json.JsonDiff.jsonDiff
import fi.oph.koski.json.JsonSerializer.serialize
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, RequiresVirkailijaOrPalvelukäyttäjä, Rooli}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache, ObservableSupport}
import fi.oph.koski.validation.KoskiValidator
import org.json4s._
import org.scalatra._
import rx.lang.scala.Observable

class OpiskeluoikeusValidationServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ObservableSupport with ContentEncodingSupport {
  get("/", request.getRemoteHost == "127.0.0.1") {
    if (!session.hasGlobalReadAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden())
    }

    val errorsOnly = params.get("errorsOnly").map(_.toBoolean).getOrElse(false)
    val validateHistory = params.get("history").map(_.toBoolean).getOrElse(false)
    val validateHenkilö = params.get("henkilö").map(_.toBoolean).getOrElse(false)
    val extractOnly = params.get("extractOnly").map(_.toBoolean).getOrElse(false)
    // Ensure that nobody uses koskiSession implicitely
    implicit val systemUser = KoskiSpecificSession.systemUser

    val context = ValidateContext(application.validator, application.historyRepository, application.henkilöRepository)(systemUser)
    val validateRow: OpiskeluoikeusRow => ValidationResult = row => try {
      var result = if (extractOnly) {
        context.extractOpiskeluoikeus(row)
      } else {
        context.validateOpiskeluoikeus(row)
      }
      if (validateHistory) result = result + context.validateHistory(row)
      if (validateHenkilö) result = result + context.validateHenkilö(row)
      result
    } catch {
      case e: Exception => ValidationResult(row.oppijaOid, row.oid, List(ErrorDetail(KoskiErrorCategory.internalError.key, e.getMessage)))
    }

    val validationResults: Observable[ValidationResult] = validate(errorsOnly, validateRow, systemUser)
    streamResponse[ValidationResult](validationResults, systemUser)
  }

  get("/:oid") {
    if (!session.hasGlobalReadAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden())
    }
    // Ensure that nobody uses koskiSession implicitely
    implicit val systemUser = KoskiSpecificSession.systemUser
    val context = ValidateContext(application.validator, application.historyRepository, application.henkilöRepository)(systemUser)
    renderEither[ValidationResult](application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))(systemUser).map(context.validateAll))
  }

  private def validate(errorsOnly: Boolean, validateRow: OpiskeluoikeusRow => ValidationResult, systemUser: KoskiSpecificSession): Observable[ValidationResult] = {
    application.opiskeluoikeusQueryRepository.mapKaikkiOpiskeluoikeudetSivuittain(1000, systemUser) { opiskeluoikeusRows =>
      opiskeluoikeusRows.par.map(validateRow).filter(result => !(errorsOnly && result.isOk)).seq
    }
  }
}

/**
  *  Operating context for data validation. Operates outside the lecixal scope of OpiskeluoikeusServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class ValidateContext(validator: KoskiValidator, historyRepository: OpiskeluoikeusHistoryRepository, henkilöRepository: HenkilöRepository)(implicit user: KoskiSpecificSession) extends Logging {
  def validateHistory(row: OpiskeluoikeusRow): ValidationResult = {
    try {
      val opiskeluoikeus = row.toOpiskeluoikeus
      (historyRepository.findVersion(row.oid, row.versionumero)(user) match {
        case Right(latestVersion) =>
          HttpStatus.validate(latestVersion == opiskeluoikeus) {
            KoskiErrorCategory.internalError(JsonErrorMessage(HistoryInconsistency(row + " versiohistoria epäkonsistentti", jsonDiff(serialize(row), serialize(latestVersion)))))
          }
        case Left(error) => error
      }) match {
        case HttpStatus.ok => ValidationResult(row.oppijaOid, row.oid, Nil)
        case status: HttpStatus => ValidationResult(row.oppijaOid, row.oid, status.errors)
      }
    } catch {
      case e: MappingException =>
        ValidationResult(row.oppijaOid, row.oid, List(ErrorDetail("deserializationFailed", s"Opiskeluoikeuden ${row.oid} deserialisointi epäonnistui")))
    }
  }

  def extractOpiskeluoikeus(row: OpiskeluoikeusRow): ValidationResult = {
    renderValidationResult(row, validator.extractOpiskeluoikeus(row.data))
  }

  def validateOpiskeluoikeus(row: OpiskeluoikeusRow): ValidationResult = {
    renderValidationResult(row, validator.extractAndValidateOpiskeluoikeus(row.data)(user, AccessType.read))
  }

  private def renderValidationResult(row: OpiskeluoikeusRow, validationResult: Either[HttpStatus, Opiskeluoikeus]) = {
    validationResult match {
      case Right(oppija) =>
        ValidationResult(row.oppijaOid, row.oid, Nil)
      case Left(status) =>
        val result = ValidationResult(row.oppijaOid, row.oid, status.errors)
        logger.warn(s"Validation failed $result")
        result
    }
  }

  def validateHenkilö(row: OpiskeluoikeusRow): ValidationResult = {
    henkilöRepository.findByOid(row.oppijaOid) match {
      case Some(h) => ValidationResult(row.oppijaOid, row.oid, Nil)
      case None => ValidationResult(row.oppijaOid, row.oid, List(ErrorDetail("oppijaaEiLöydy", s"Oppijaa ${row.oppijaOid} ei löydy henkilöpalvelusta")))
    }
  }

  def validateAll(row: OpiskeluoikeusRow): ValidationResult = {
    validateOpiskeluoikeus(row) + validateHistory(row) + validateHenkilö(row)
  }
}

case class ValidationResult(henkilöOid: Henkilö.Oid, opiskeluoikeusOid: String, errors: List[ErrorDetail]) {
  def isOk = errors.isEmpty
  def +(other: ValidationResult) = ValidationResult(henkilöOid, opiskeluoikeusOid, errors ++ other.errors)
}

case class HistoryInconsistency(message: String, @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)) diff: JValue)
