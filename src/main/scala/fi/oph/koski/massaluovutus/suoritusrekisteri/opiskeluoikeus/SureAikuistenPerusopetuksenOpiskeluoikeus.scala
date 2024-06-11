package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object SureAikuistenPerusopetuksenOpiskeluoikeus {
  def apply(oo: AikuistenPerusopetuksenOpiskeluoikeus): SureOpiskeluoikeus =
    SureOpiskeluoikeus(
      oo,
      suoritukset = oo.suoritukset.flatMap(SureAikuistenPerusopetuksenSuoritus.apply)
    )
}

sealed trait SureAikuistenPerusopetuksenSuoritus extends SurePäätasonSuoritus

object SureAikuistenPerusopetuksenSuoritus {
  def apply(pts: AikuistenPerusopetuksenPäätasonSuoritus): Option[SurePäätasonSuoritus] =
    pts match {
      case s: AikuistenPerusopetuksenOppimääränSuoritus =>
        Some(SureDefaultPäätasonSuoritus(s))
      case s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus =>
        Some(SureAikusitenPerusopetuksenOppiaineenOppimääränSuoritus(s))
      case _ =>
        None
    }
}

@Title("Aikuisten perusopetuksen oppiaineen oppimäärän suoritus")
case class SureAikusitenPerusopetuksenOppiaineenOppimääränSuoritus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  koulutusmoduuli: AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends SureAikuistenPerusopetuksenSuoritus

object SureAikusitenPerusopetuksenOppiaineenOppimääränSuoritus {
  def apply(s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus): SureAikusitenPerusopetuksenOppiaineenOppimääränSuoritus =
    SureAikusitenPerusopetuksenOppiaineenOppimääränSuoritus(
      koulutusmoduuli = s.koulutusmoduuli,
      toimipiste = s.toimipiste,
      arviointi = s.arviointi,
      vahvistus = s.vahvistus,
      suoritustapa = s.suoritustapa,
      suorituskieli = s.suorituskieli,
      muutSuorituskielet = s.muutSuorituskielet,
      todistuksellaNäkyvätLisätiedot = s.todistuksellaNäkyvätLisätiedot,
      osasuoritukset = s.osasuoritukset,
      tyyppi = s.tyyppi
    )
}
