package fi.oph.koski.massaluovutus.suorituspalvelu

import fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.scalaschema.annotation.Description
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDateTime

case class SupaResponse(
  @Description("Oppijan yksilöivä tunniste. Jos oppijalla on olemassa useita yksilöiviä tunnisteita, palautetaan tässä kentässä oppijanumero eli oppijan hallitseva tunniste.")
  oppijaOid: String,
  @Description("Oppijan kaikki yksilöivät tunnisteet, joilla opiskeluoikeuksia on tallennettu Koski-tietovarantoon.")
  kaikkiOidit: Seq[String],
  aikaleima: LocalDateTime,
  opiskeluoikeudet: Seq[SupaOpiskeluoikeus],
)

object SupaResponse {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[SupaResponse]).asInstanceOf[ClassSchema])
}

object SupaOpiskeluoikeusO {
  def apply(oo: KoskeenTallennettavaOpiskeluoikeus, oppijaOid: String): Option[SupaOpiskeluoikeus] =
    oo match {
      case o: PerusopetuksenOpiskeluoikeus => Some(SupaPerusopetuksenOpiskeluoikeus(o, oppijaOid))
      case o: AikuistenPerusopetuksenOpiskeluoikeus => Some(SupaAikuistenPerusopetuksenOpiskeluoikeus(o, oppijaOid))
      case o: AmmatillinenOpiskeluoikeus => Some(SupaAmmatillinenTutkinto(o, oppijaOid))
      case o: TutkintokoulutukseenValmentavanOpiskeluoikeus => Some(SupaTutkintokoulutukseenValmentavanOpiskeluoikeus(o, oppijaOid))
      case o: VapaanSivistystyönOpiskeluoikeus => Some(SupaVapaanSivistystyönOpiskeluoikeus(o, oppijaOid))
      case o: DIAOpiskeluoikeus => SupaDIAOpiskeluoikeus(o, oppijaOid)
      case o: EBOpiskeluoikeus => Some(SupaEBOpiskeluoikeus(o, oppijaOid))
      case o: IBOpiskeluoikeus => SupaIBOpiskeluoikeus(o, oppijaOid)
      case o: InternationalSchoolOpiskeluoikeus => SupaInternationalSchoolOpiskeluoikeus(o, oppijaOid)
      case o: LukionOpiskeluoikeus => Some(SupaLukionOpiskeluoikeus(o, oppijaOid))
      case _ => None
    }
}
