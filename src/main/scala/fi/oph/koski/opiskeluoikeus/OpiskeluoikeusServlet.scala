package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiAuditLogMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, Logging}
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus, PäätasonSuoritus}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import fi.oph.scalaschema.extraction.ValidationError
import org.json4s.JValue

class OpiskeluoikeusServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache {
  get("/:oid") {
    val result: Either[HttpStatus, KoskiOpiskeluoikeusRow] = application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))(session)
    result.map(oo => KoskiAuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, session, Map(oppijaHenkiloOid -> oo.oppijaOid))).foreach(AuditLog.log)
    renderEither[KoskeenTallennettavaOpiskeluoikeus](result.map(_.toOpiskeluoikeusUnsafe))
  }

  post("/:oid/:versionumero/delete-paatason-suoritus") {
    withJsonBody { oppijaJson: JValue =>
      implicit val context: ExtractionContext = strictDeserialization
      val validationResult = SchemaValidatingExtractor.extract[PäätasonSuoritus](oppijaJson) match {
        case Right(t) => Right(t)
        case Left(errors: List[ValidationError]) =>
          logger.error(s"Scheman validaatiovirhe poistettaessa päätason suoritusta: $errors")
          Left(KoskiErrorCategory.badRequest.validation.jsonSchema.apply(JsonErrorMessage(errors)))
      }

      val result = validationResult.flatMap(
        application.oppijaFacade.invalidatePäätasonSuoritus(getStringParam("oid"), _, getIntegerParam("versionumero"))
      )

      if (result.isLeft) {
        logger.error(s"Validaatiovirhe poistettaessa päätason suoritusta: $result")
      }

      renderEither[HenkilönOpiskeluoikeusVersiot](result)
    }(parseErrorHandler = haltWithStatus)
  }

  delete("/:oid") {
    val result = application.oppijaFacade.invalidateOpiskeluoikeus(getStringParam("oid"))
    renderEither[HenkilönOpiskeluoikeusVersiot](result)
  }

  post("/:oid/pura-lahdejarjestelmakytkenta") {
    // TODO: Testikoodia access rightsien nopeaan testaamiseen, ei tarkoitus jäädä tähän
    renderEither {
      val ooE = application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))
      val x = ooE.exists(oo => session.hasTiedonsiirtokytkennänPurkaminenAccess(oo.oppilaitosOid, oo.koulutustoimijaOid))
      if (x) {
        Right("TODO: Ei toteutettu")
      } else {
        Left(KoskiErrorCategory.forbidden())
      }
    }
  }
}

case class LähdejärjestelmänPurkaminenResult(
  oid: Opiskeluoikeus.Oid,
)
