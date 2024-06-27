package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus.{SureVapaanSivistystyönOpiskeluoikeus, _}
import fi.oph.koski.schema._
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDateTime

case class SureResponse(
  oppijaOid: String,
  aikaleima: LocalDateTime,
  opiskeluoikeus: SureOpiskeluoikeus,
)

object SureResponse {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(KoskiSchema.createSchema(classOf[SureResponse]).asInstanceOf[ClassSchema])
}

object SureOpiskeluoikeus {
  def apply(oo: KoskeenTallennettavaOpiskeluoikeus): Option[SureOpiskeluoikeus] =
    (oo match {
      case o: PerusopetuksenOpiskeluoikeus => Some(SurePerusopetuksenOpiskeluoikeus(o))
      case o: AikuistenPerusopetuksenOpiskeluoikeus => Some(SureAikuistenPerusopetuksenOpiskeluoikeus(o))
      case o: AmmatillinenOpiskeluoikeus => Some(SureAmmatillinenTutkinto(o))
      case o: TutkintokoulutukseenValmentavanOpiskeluoikeus => Some(SureTutkintokoulutukseenValmentavanOpiskeluoikeus(o))
      case o: VapaanSivistystyönOpiskeluoikeus => Some(SureVapaanSivistystyönOpiskeluoikeus(o))
      case o: DIAOpiskeluoikeus => SureDIAOpiskeluoikeus(o)
      case o: EBOpiskeluoikeus => Some(SureEBOpiskeluoikeus(o))
      case o: IBOpiskeluoikeus => SureIBOpiskeluoikeus(o)
      case o: InternationalSchoolOpiskeluoikeus => SureInternationalSchoolOpiskeluoikeus(o)
      case _ => None
    })//.filter(_.suoritukset.nonEmpty)
}
