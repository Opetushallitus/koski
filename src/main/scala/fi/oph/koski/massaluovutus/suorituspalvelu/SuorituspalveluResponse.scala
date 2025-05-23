package fi.oph.koski.massaluovutus.suorituspalvelu

import fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDateTime

case class SupaResponse(
  oppijaOid: String,
  kaikkiOidit: Seq[String],
  aikaleima: LocalDateTime,
  opiskeluoikeudet: Seq[SupaOpiskeluoikeus],
)

object SupaResponse {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[SupaResponse]).asInstanceOf[ClassSchema])
}

object SupaOpiskeluoikeusO {
  def apply(oo: KoskeenTallennettavaOpiskeluoikeus): Option[SupaOpiskeluoikeus] =
    oo match {
      case o: PerusopetuksenOpiskeluoikeus => Some(SupaPerusopetuksenOpiskeluoikeus(o))
      case o: AikuistenPerusopetuksenOpiskeluoikeus => Some(SupaAikuistenPerusopetuksenOpiskeluoikeus(o))
      case o: AmmatillinenOpiskeluoikeus => Some(SupaAmmatillinenTutkinto(o))
      case o: TutkintokoulutukseenValmentavanOpiskeluoikeus => Some(SupaTutkintokoulutukseenValmentavanOpiskeluoikeus(o))
      case o: VapaanSivistystyönOpiskeluoikeus => Some(SupaVapaanSivistystyönOpiskeluoikeus(o))
      case o: DIAOpiskeluoikeus => SupaDIAOpiskeluoikeus(o)
      case o: EBOpiskeluoikeus => Some(SupaEBOpiskeluoikeus(o))
      case o: IBOpiskeluoikeus => SupaIBOpiskeluoikeus(o)
      case o: InternationalSchoolOpiskeluoikeus => SupaInternationalSchoolOpiskeluoikeus(o)
      case _ => None
    }
}
