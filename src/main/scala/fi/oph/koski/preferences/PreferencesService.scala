package fi.oph.koski.preferences

import fi.oph.common.log.Logging
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.{KoskiDatabaseMethods, PreferenceRow, Tables}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.scalaschema.SchemaValidatingExtractor
import fi.oph.scalaschema.extraction.ValidationError
import org.json4s._

import scala.collection.immutable
import scala.reflect.runtime.universe.TypeTag

case class PreferencesService(protected val db: DB) extends Logging with KoskiDatabaseMethods {
  import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

  val prefTypes: Map[String, Class[_ <: StorablePreference]] = Map(
    "myöntäjät" -> classOf[Organisaatiohenkilö],
    "nuortenperusopetuksenpaikallinenoppiaine" -> classOf[NuortenPerusopetuksenPaikallinenOppiaine],
    "aikuistenperusopetuksenpaikallinenoppiaine" -> classOf[AikuistenPerusopetuksenPaikallinenOppiaine],
    "perusopetukseenvalmistavanopetuksenoppiaine" -> classOf[PerusopetukseenValmistavanOpetuksenOppiaine],
    "aikuistenperusopetuksenalkuvaiheenpaikallinenoppiaine" -> classOf[AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine],
    "paikallinenaikuistenperusopetuksenkurssi" -> classOf[PaikallinenAikuistenPerusopetuksenKurssi],
    "paikallinenaikuistenperusopetuksenalkuvaiheenkurssi" -> classOf[PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi],
    "paikallinenlukionkurssi" -> classOf[PaikallinenLukionKurssi2015],
    "paikallinenlukionoppiaine" -> classOf[PaikallinenLukionOppiaine2015],
    "paikallinenlukionoppiaine2019" -> classOf[PaikallinenLukionOppiaine2019],
    "paikallinenlukioonvalmistavankoulutuksenkurssi" -> classOf[PaikallinenLukioonValmistavanKoulutuksenKurssi],
    "paikallinenlukioonvalmistavankoulutuksenoppiaine" -> classOf[PaikallinenLukioonValmistavanKoulutuksenOppiaine],
    "ibkurssi" -> classOf[IBKurssi],
    "lukionpaikallinenopintojakso2019" -> classOf[LukionPaikallinenOpintojakso2019]
  )

  def put(organisaatioOid: String, koulutustoimijaOid: Option[String], `rawType`: String, key: String, value: JValue)(implicit session: KoskiSession) = {
    val `type` = migrateTypeName(rawType)

    if (!session.hasWriteAccess(organisaatioOid, koulutustoimijaOid)) throw new InvalidRequestException(KoskiErrorCategory.forbidden.organisaatio())
    prefTypes.get(`type`) match {
      case Some(klass) =>
        extract[StorablePreference](value, klass) match {
          case Right(deserialized) =>
            runDbSync(Tables.Preferences.insertOrUpdate(PreferenceRow(organisaatioOid, koulutustoimijaOid, `type`, key, value)))
            HttpStatus.ok
          case Left(errors: immutable.Seq[ValidationError]) =>
            KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors))
        }
      case None => KoskiErrorCategory.notFound("Unknown pref type " + `type`)
    }
  }

  def delete(organisaatioOid: String, koulutustoimijaOid: Option[String], `rawType`: String, key: String)(implicit session: KoskiSession): HttpStatus = {
    val `type` = migrateTypeName(rawType)

    if (!session.hasWriteAccess(organisaatioOid, koulutustoimijaOid)) throw new InvalidRequestException(KoskiErrorCategory.forbidden.organisaatio())

    prefTypes.get(`type`) match {
      case Some(klass) =>
        val koulutustoimija: String = koulutustoimijaOid.getOrElse("")
        runDbSync(Tables.Preferences.filter(r => r.organisaatioOid === organisaatioOid && r.`type` === `type` && r.key === key && r.koulutustoimijaOid.map(_ === koulutustoimija).getOrElse(true)).delete)
        HttpStatus.ok
      case None => KoskiErrorCategory.notFound("Unknown pref type " + `type`)
    }
  }

  private def extract[T : TypeTag](value: JValue, klass: Class[_ <: T]): Either[List[ValidationError], T] = {
    import fi.oph.koski.schema.KoskiSchema.deserializationContext
    SchemaValidatingExtractor.extract(value, klass).right.map(_.asInstanceOf[T])
  }

  def get(organisaatioOid: String, koulutustoimijaOid: Option[String], `rawType`: String)(implicit session: KoskiSession): Either[HttpStatus, List[StorablePreference]] = {
    val `type` = migrateTypeName(rawType)

    if (!session.hasWriteAccess(organisaatioOid, koulutustoimijaOid)) throw new InvalidRequestException(KoskiErrorCategory.forbidden.organisaatio())

    prefTypes.get(`type`) match {
      case Some(klass) =>
        val koulutustoimija: String = koulutustoimijaOid.getOrElse("")
        val jValues = runDbSync(Tables.Preferences.filter(r => r.organisaatioOid === organisaatioOid && r.`type` === `type` && r.koulutustoimijaOid.map(_ === koulutustoimija).getOrElse(true)).map(_.value).result).toList
        HttpStatus.foldEithers(jValues.map(value =>
          extract[StorablePreference](value, klass)
            .left.map((errors: List[ValidationError]) => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
        ))
      case None => Left(KoskiErrorCategory.notFound("Unknown pref type " + `type`))
    }
  }

  private def migrateTypeName(rawType: String): String = typeNameMigrations.getOrElse(rawType, rawType)

  private val typeNameMigrations: Map[String, String] = Map(
    "paikallinenlukionoppiaine2015" -> "paikallinenlukionoppiaine",
    "paikallinenlukionkurssi2015" -> "paikallinenlukionkurssi"
  )
}
