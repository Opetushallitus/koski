package fi.oph.koski.preferences

import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.{KoskiDatabaseMethods, PreferenceRow, Tables}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s._

case class PreferencesService(protected val db: DB) extends Logging with KoskiDatabaseMethods {

  import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

  val prefTypes: Map[String, Class[_]] = Map(
    "myöntäjät" -> classOf[Organisaatiohenkilö],
    "perusopetuksenpaikallinenvalinnainenoppiaine" -> classOf[PerusopetuksenPaikallinenValinnainenOppiaine],
    "perusopetukseenvalmistavanopetuksenoppiaine" -> classOf[PerusopetukseenValmistavanOpetuksenOppiaine],
    "aikuistenperusopetuksenalkuvaiheenpaikallinenoppiaine" -> classOf[AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine]
  )


  def put(organisaatioOid: String, `type`: String, key: String, value: JValue)(implicit session: KoskiSession) = {
    if (!session.hasWriteAccess(organisaatioOid)) throw new InvalidRequestException(KoskiErrorCategory.forbidden.organisaatio())

    prefTypes.get(`type`) match {
      case Some(klass) =>
        extract(value, klass) match {
          case Right(deserialized) =>
            runDbSync(Tables.Preferences.insertOrUpdate(PreferenceRow(organisaatioOid, `type`, key, value)))
            HttpStatus.ok
          case Left(errors) =>
            KoskiErrorCategory.badRequest.validation.jsonSchema(errors)
        }
      case None => KoskiErrorCategory.notFound("Unknown pref type " + `type`)
    }
  }

  def delete(organisaatioOid: String, `type`: String, key: String)(implicit session: KoskiSession): HttpStatus = {
    if (!session.hasWriteAccess(organisaatioOid)) throw new InvalidRequestException(KoskiErrorCategory.forbidden.organisaatio())

    prefTypes.get(`type`) match {
      case Some(klass) =>
        runDbSync(Tables.Preferences.filter(r => r.organisaatioOid === organisaatioOid && r.`type` === `type` && r.key === key).delete)
        HttpStatus.ok
      case None => KoskiErrorCategory.notFound("Unknown pref type " + `type`)
    }
  }

  private def extract(value: JValue, klass: Class[_]) = SchemaValidatingExtractor.extract(value, klass)(KoskiSchema.deserializationContext)

  def get(organisaatioOid: String, `type`: String)(implicit session: KoskiSession): Either[HttpStatus, List[AnyRef]] = {
    if (!session.hasWriteAccess(organisaatioOid)) throw new InvalidRequestException(KoskiErrorCategory.forbidden.organisaatio())

    prefTypes.get(`type`) match {
      case Some(klass) =>
        val jValues = runDbSync(Tables.Preferences.filter(r => r.organisaatioOid === organisaatioOid && r.`type` === `type`).map(_.value).result).toList
        val extractionResults = jValues.map(value => extract(value, klass).left.map(error => KoskiErrorCategory.badRequest.validation.jsonSchema(error)))
        HttpStatus.foldEithers[AnyRef](extractionResults)
      case None => Left(KoskiErrorCategory.notFound("Unknown pref type " + `type`))
    }
  }
}