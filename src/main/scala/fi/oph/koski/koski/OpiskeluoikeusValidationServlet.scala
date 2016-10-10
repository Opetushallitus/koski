package fi.oph.koski.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluOikeusRow
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.koskiuser.{AccessType, KoskiUser, RequiresAuthentication}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Henkilö, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s._
import rx.lang.scala.Observable

class OpiskeluoikeusValidationServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with OpiskeluoikeusQueries with NoCache {
  get("/") {
    val context = ValidateContext(koskiUser, application.validator, application.historyRepository)
    query
      .flatMap { case (henkilö, opiskeluoikeudet) => Observable.from(opiskeluoikeudet) }
      .map(context.validateAll)
  }

  get("/:id") {
    val context = ValidateContext(koskiUser, application.validator, application.historyRepository)
    val findResult: Either[HttpStatus, OpiskeluOikeusRow] = application.facade.findOpiskeluOikeus(getIntegerParam("id"))(koskiUser)
    val result: Either[HttpStatus, ValidationResult] = findResult
      .right.map(context.validateAll)
    renderEither(result)
  }
}

/**
  *  Operating context for data validation. Operates outside the lecixal scope of OpiskeluoikeusServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class ValidateContext(user: KoskiUser, validator: KoskiValidator, historyRepository: OpiskeluoikeusHistoryRepository) {
  def validateHistory(row: OpiskeluOikeusRow): ValidationResult = {
    (try {
      Right(row.toOpiskeluOikeus)
    } catch {
      case e: Exception => Left(KoskiErrorCategory.internalError(s"Opiskeluoikeuden ${row.id} deserialisointi epäonnistui")) // TODO: row.toOpiskeluoikeus should return Either
    }) match {
      case Right(opiskeluoikeus) =>
        (historyRepository.findVersion(row.id, row.versionumero)(user) match {
          case Right(latestVersion) =>
            HttpStatus.validate(latestVersion == opiskeluoikeus) {
              KoskiErrorCategory.internalError(toJValue(HistoryInconsistency(row + " versiohistoria epäkonsistentti", Json.jsonDiff(row, latestVersion))))
            }
          case Left(error) => error
        }) match {
          case HttpStatus.ok => ValidationResult(row.oppijaOid, row.id, Nil)
          case status: HttpStatus => ValidationResult(row.oppijaOid, row.id, status.errors)
        }
      case Left(status) =>
        ValidationResult(row.oppijaOid, row.id, status.errors)
    }

  }

  def validateOpiskeluoikeus(row: OpiskeluOikeusRow): ValidationResult = {
    val validationResult: Either[HttpStatus, Opiskeluoikeus] = validator.extractAndValidateOpiskeluoikeus(row.data)(user, AccessType.read)
    validationResult match {
      case Right(oppija) =>
        ValidationResult(row.oppijaOid, row.id, Nil)
      case Left(status) =>
        ValidationResult(row.oppijaOid, row.id, status.errors)
    }
  }

  def validateAll(row: OpiskeluOikeusRow): ValidationResult = {
    validateOpiskeluoikeus(row) + validateHistory(row)
  }
}

case class ValidationResult(henkilöOid: Henkilö.Oid, opiskeluoikeusId: Int, errors: List[AnyRef]) {
  def isOk = errors.isEmpty
  def +(other: ValidationResult) = ValidationResult(henkilöOid, opiskeluoikeusId, errors ++ other.errors)
}

case class HistoryInconsistency(message: String, diff: JValue)
