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
    val mySession = session

    val käyttöoikeudetSize = mySession.kaikkiKäyttöoikeudet.size
    logger.info(s"Käyttäjällä ${mySession.user.oid} on $käyttöoikeudetSize käyttöoikeutta")

    val sizeGroup = käyttöoikeudetSize match {
      case n if n == 0 => "0"
      case n if n == 1 => "1"
      case n if n < 5 => "<5"
      case n if n < 10 => "<10"
      case n if n < 100 => "<100"
      case n if n < 200 => "<200"
      case n if n < 500 => "<500"
      case n if n < 1000 => "<1000"
      case n if n < 5000 => "<5000"
      case n if n < 10000 => "<10000"
      case _ => ">=10000"
    }

    val result: Either[HttpStatus, KoskiOpiskeluoikeusRow] = timed(s"findByOid, käyttöoikeuksia ${sizeGroup}", thresholdMs = 0) {
      application.opiskeluoikeusRepository.findByOid(getStringParam("oid"))(mySession)
    }
    result.map(oo => KoskiAuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, mySession, Map(oppijaHenkiloOid -> oo.oppijaOid))).foreach(AuditLog.log)
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
    val result = application.opiskeluoikeusRepository.puraLähdejärjestelmäkytkentä(getStringParam("oid"), application.henkilöRepository)
    result.foreach(r => KoskiAuditLogMessage(LAHDEJARJESTELMAKYTKENNAN_PURKAMINEN, session, Map(opiskeluoikeusOid -> r.oid)))
    renderEither(result.map(r => LähdejärjestelmänPurkaminenResult(r.oid)))
  }
}

case class LähdejärjestelmänPurkaminenResult(
  oid: Opiskeluoikeus.Oid,
)
