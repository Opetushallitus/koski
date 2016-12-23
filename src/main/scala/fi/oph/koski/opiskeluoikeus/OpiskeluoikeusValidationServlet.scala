package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, RequiresAuthentication}
import fi.oph.koski.log.KoskiMessageField.{apply => _}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus}
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import fi.oph.koski.validation.KoskiValidator
import org.json4s._
import org.scalatra._
import rx.lang.scala.Observable

class OpiskeluoikeusValidationServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with NoCache with ObservableSupport with GZipSupport{
  get("/") {
    val errorsOnly = params.get("errorsOnly").map(_.toBoolean).getOrElse(false)
    val context = ValidateContext(koskiSession, application.validator, application.historyRepository, application.henkilöRepository)
    val validateHistory = params.get("history").map(_.toBoolean).getOrElse(false)
    val validateHenkilö = params.get("henkilö").map(_.toBoolean).getOrElse(false)
    def validate(row: OpiskeluoikeusRow): ValidationResult = {
      var result = context.validateOpiskeluoikeus(row)
      if (validateHistory) result = result + context.validateHistory(row)
      if (validateHenkilö) result = result + context.validateHenkilö(row)
      result
    }

    OpiskeluoikeusQueryFilter.parseQueryFilter(params.filterKeys(!List("errorsOnly", "history", "henkilö").contains(_)).toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
      case Right(filters) =>
        val rows: Observable[(OpiskeluoikeusRow, HenkilöRow)] = application.OpiskeluoikeusRepository.streamingQuery(filters, None, None)(koskiSession)
        rows.map(_._1).map(validate).filter(result => !(errorsOnly && result.isOk))

      case Left(status) =>
        haltWithStatus(status)
    }
  }

  get("/:id") {
    val context = ValidateContext(koskiSession, application.validator, application.historyRepository, application.henkilöRepository)
    val result: Option[OpiskeluoikeusRow] = application.OpiskeluoikeusRepository.findById(getIntegerParam("id"))(koskiSession)
    renderEither(result match {
      case Some(oo) => Right(context.validateAll(oo))
      case _ => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    })
  }
}

/**
  *  Operating context for data validation. Operates outside the lecixal scope of OpiskeluoikeusServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class ValidateContext(user: KoskiSession, validator: KoskiValidator, historyRepository: OpiskeluoikeusHistoryRepository, henkilöRepository: HenkilöRepository) {
  def validateHistory(row: OpiskeluoikeusRow): ValidationResult = {
    try {
      val opiskeluoikeus = row.toOpiskeluoikeus
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
    } catch {
      case e: MappingException =>
        ValidationResult(row.oppijaOid, row.id, List(s"Opiskeluoikeuden ${row.id} deserialisointi epäonnistui"))
    }
  }

  def validateOpiskeluoikeus(row: OpiskeluoikeusRow): ValidationResult = {
    val validationResult: Either[HttpStatus, Opiskeluoikeus] = validator.extractAndValidateOpiskeluoikeus(row.data)(user, AccessType.read)
    validationResult match {
      case Right(oppija) =>
        ValidationResult(row.oppijaOid, row.id, Nil)
      case Left(status) =>
        ValidationResult(row.oppijaOid, row.id, status.errors)
    }
  }

  def validateHenkilö(row: OpiskeluoikeusRow): ValidationResult = {
    henkilöRepository.findByOid(row.oppijaOid) match {
      case Some(h) => ValidationResult(row.oppijaOid, row.id, Nil)
      case None => ValidationResult(row.oppijaOid, row.id, List(s"Oppijaa ${row.oppijaOid} ei löydy henkilöpalvelusta"))
    }
  }

  def validateAll(row: OpiskeluoikeusRow): ValidationResult = {
    validateOpiskeluoikeus(row) + validateHistory(row) + validateHenkilö(row)
  }
}

case class ValidationResult(henkilöOid: Henkilö.Oid, opiskeluoikeusId: Int, errors: List[AnyRef]) {
  def isOk = errors.isEmpty
  def +(other: ValidationResult) = ValidationResult(henkilöOid, opiskeluoikeusId, errors ++ other.errors)
}

case class HistoryInconsistency(message: String, diff: JValue)
