package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http._
import fi.oph.koski.json.JsonDiff.jsonDiff
import fi.oph.koski.json.JsonSerializer.serialize
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus}
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import fi.oph.koski.util.PaginationSettings
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.validation.KoskiValidator
import org.json4s._
import org.scalatra._
import rx.Observable.{create => createObservable}
import rx.Observer
import rx.functions.{Func0, Func2}
import rx.lang.scala.Observable
import rx.observables.SyncOnSubscribe.createStateful

class OpiskeluoikeusValidationServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ObservableSupport with ContentEncodingSupport {
  get("/", request.getRemoteHost == "127.0.0.1") {
    if (!koskiSession.hasGlobalReadAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden())
    }

    val errorsOnly = params.get("errorsOnly").map(_.toBoolean).getOrElse(false)
    val validateHistory = params.get("history").map(_.toBoolean).getOrElse(false)
    val validateHenkilö = params.get("henkilö").map(_.toBoolean).getOrElse(false)
    val extractOnly = params.get("extractOnly").map(_.toBoolean).getOrElse(false)
    // Ensure that nobody uses koskiSession implicitely
    implicit val systemUser = KoskiSession.systemUser

    val context = ValidateContext(application.validator, application.historyRepository, application.henkilöRepository)(systemUser)
    val validateRow: OpiskeluoikeusRow => ValidationResult = row => {
      var result = if (extractOnly) {
        context.extractOpiskeluoikeus(row)
      } else {
        context.validateOpiskeluoikeus(row)
      }
      if (validateHistory) result = result + context.validateHistory(row)
      if (validateHenkilö) result = result + context.validateHenkilö(row)
      result
    }

    OpiskeluoikeusQueryFilter.parse(params.filterKeys(!List("errorsOnly", "history", "henkilö", "extractOnly").contains(_)).toList)(application.koodistoViitePalvelu, application.organisaatioRepository, systemUser) match {
      case Right(filters) =>
        val validationResults: Observable[ValidationResult] = validate(errorsOnly, filters, validateRow, systemUser)
        streamResponse[ValidationResult](validationResults, systemUser)

      case Left(status) =>
        haltWithStatus(status)
    }
  }

  get("/:oid") {
    if (!koskiSession.hasGlobalReadAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden())
    }
    // Ensure that nobody uses koskiSession implicitely
    implicit val systemUser = KoskiSession.systemUser
    val context = ValidateContext(application.validator, application.historyRepository, application.henkilöRepository)(systemUser)
    renderEither[ValidationResult](application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))(systemUser).map(context.validateAll))
  }

  private def validate(errorsOnly: Boolean, filters: List[OpiskeluoikeusQueryFilter], validateRow: OpiskeluoikeusRow => ValidationResult, systemUser: KoskiSession): Observable[ValidationResult] = {
    import rx.lang.scala.JavaConverters._
    def validateRows(page: Int): (Seq[ValidationResult], Int) = {
      val opiskeluoikeusRows = application.opiskeluoikeusQueryRepository.opiskeluoikeusQuerySync(filters, Some(Ascending("id")), Some(PaginationSettings(page, 1000)))(systemUser).map(_._1)
      (opiskeluoikeusRows.par.map(validateRow).toList, page)
    }

    createObservable(createStateful[(Seq[ValidationResult], Int), Seq[ValidationResult]](
      (() => validateRows(0)): Func0[_ <: (Seq[ValidationResult], Int)],
      ((state, observer) => {
        val (validationResults, page) = state
        if (validationResults.isEmpty) {
          observer.onCompleted()
          (Nil, 0)
        } else {
          observer.onNext(validationResults.filter(result => !(errorsOnly && result.isOk)))
          validateRows(page + 1)
        }
      }): Func2[_ >: (Seq[ValidationResult], Int), _ >: Observer[_ >: Seq[ValidationResult]], _ <: (Seq[ValidationResult], Int)]
    )).asScala.flatMap(Observable.from(_))
  }
}

/**
  *  Operating context for data validation. Operates outside the lecixal scope of OpiskeluoikeusServlet to ensure that none of the
  *  Scalatra threadlocals are used. This must be done because in batch mode, we are running in several threads.
  */
case class ValidateContext(validator: KoskiValidator, historyRepository: OpiskeluoikeusHistoryRepository, henkilöRepository: HenkilöRepository)(implicit user: KoskiSession) {
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
        ValidationResult(row.oppijaOid, row.oid, status.errors)
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

case class HistoryInconsistency(message: String, @SensitiveData diff: JValue)
