package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.common.log.KoskiMessageField.{apply => _, _}
import fi.oph.common.log.KoskiOperation._
import fi.oph.common.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PäätasonSuoritus}
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.scalaschema.SchemaValidatingExtractor
import fi.oph.scalaschema.extraction.ValidationError
import org.json4s.JValue

class OpiskeluoikeusServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache {
  get("/:oid") {
    val result: Either[HttpStatus, OpiskeluoikeusRow] = application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))(koskiSession)
    result.map(oo => AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(oppijaHenkiloOid -> oo.oppijaOid))).foreach(AuditLog.log)
    renderEither[KoskeenTallennettavaOpiskeluoikeus](result.map(_.toOpiskeluoikeus))
  }

  post("/:oid/:versionumero/delete-paatason-suoritus") {
    withJsonBody { oppijaJson: JValue =>
      import fi.oph.koski.schema.KoskiSchema.deserializationContext

      val validationResult = SchemaValidatingExtractor.extract[PäätasonSuoritus](oppijaJson) match {
        case Right(t) => Right(t)
        case Left(errors: List[ValidationError]) => Left(KoskiErrorCategory.badRequest.validation.jsonSchema.apply(JsonErrorMessage(errors)))
      }

      val result = validationResult.flatMap(
        application.oppijaFacade.invalidatePäätasonSuoritus(getStringParam("oid"), _, getIntegerParam("versionumero"))
      )

      application.perustiedotIndexer.index.refreshIndex()
      renderEither[HenkilönOpiskeluoikeusVersiot](result)
    }(parseErrorHandler = haltWithStatus)
  }

  delete("/:oid") {
    val result = application.oppijaFacade.invalidateOpiskeluoikeus(getStringParam("oid"))
    application.perustiedotIndexer.index.refreshIndex()
    renderEither[HenkilönOpiskeluoikeusVersiot](result)
  }
}
