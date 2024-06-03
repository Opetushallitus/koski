package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.scalaschema.annotation.Discriminator

import java.time.LocalDateTime

case class SureResponse(
  oppijaOid: String,
  aikaleima: LocalDateTime,
  opiskeluoikeus: SureOpiskeluoikeus,
)

trait SureOpiskeluoikeus {
  @KoodistoUri("opiskeluoikeudentyyppi")
  @Discriminator
  def tyyppi: Koodistokoodiviite
  def oid: String
  def tila: OpiskeluoikeudenTila
  def suoritukset: List[SurePäätasonSuoritus]
}

object SureOpiskeluoikeus {
  def apply(oo: KoskeenTallennettavaOpiskeluoikeus): Option[SureOpiskeluoikeus] =
    (oo match {
      case o: PerusopetuksenOpiskeluoikeus => Some(SurePerusopetuksenOpiskeluoikeus(o))
      case o: AikuistenPerusopetuksenOpiskeluoikeus => Some(SureAikuistenPerusopetuksenOpiskeluoikeus(o))
      case o: AmmatillinenOpiskeluoikeus => Some(SureAmmatillinenTutkinto(o))
      case o: TutkintokoulutukseenValmentavanOpiskeluoikeus => Some(SureTuvaOpiskeluoikeus(o))
      case _ => None
    }).filter(_.suoritukset.nonEmpty)
}

