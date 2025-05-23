package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.CaseClass
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate

@Title("Aikuisten perusopetuksen opiskeluoikeus")
case class SureAikuistenPerusopetuksenOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila,
  suoritukset: List[SureAikuistenPerusopetuksenSuoritus],
) extends SureOpiskeluoikeus

object SureAikuistenPerusopetuksenOpiskeluoikeus {
  def apply(oo: AikuistenPerusopetuksenOpiskeluoikeus): SureAikuistenPerusopetuksenOpiskeluoikeus =
    SureAikuistenPerusopetuksenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.flatMap(SureAikuistenPerusopetuksenSuoritus.apply),
    )
}

sealed trait SureAikuistenPerusopetuksenSuoritus extends SureSuoritus

object SureAikuistenPerusopetuksenSuoritus {
  def apply(pts: AikuistenPerusopetuksenPäätasonSuoritus): Option[SureAikuistenPerusopetuksenSuoritus] =
    pts match {
      case s: AikuistenPerusopetuksenOppimääränSuoritus =>
        Some(SureAikuistenPerusopetuksenOppimääränSuoritus(s))
      case s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus =>
        Some(SureAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(s))
      case _ =>
        None
    }
}

@Title("Aikuisten perusopetuksen oppimäärän suoritus")
case class SureAikuistenPerusopetuksenOppimääränSuoritus(
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SureVahvistus],
  koulutusmoduuli: AikuistenPerusopetus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[AikuistenPerusopetuksenOppiaineenSuoritus],
) extends SureAikuistenPerusopetuksenSuoritus with SureVahvistuksellinen

object SureAikuistenPerusopetuksenOppimääränSuoritus {
  def apply(s: AikuistenPerusopetuksenOppimääränSuoritus): SureAikuistenPerusopetuksenOppimääränSuoritus =
    SureAikuistenPerusopetuksenOppimääränSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SureVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.toList.flatten,
    )
}

@Title("Aikuisten perusopetuksen oppiaineen oppimäärän suoritus")
case class SureAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  koulutusmoduuli: AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  vahvistus: Option[SureVahvistus] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends SureAikuistenPerusopetuksenSuoritus with SureVahvistuksellinen

object SureAikuistenPerusopetuksenOppiaineenOppimääränSuoritus {
  def apply(s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus): SureAikuistenPerusopetuksenOppiaineenOppimääränSuoritus =
    SureAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
      koulutusmoduuli = s.koulutusmoduuli,
      toimipiste = s.toimipiste,
      arviointi = s.arviointi,
      vahvistus = s.vahvistus.map(v => SureVahvistus(v.päivä)),
      suoritustapa = s.suoritustapa,
      suorituskieli = s.suorituskieli,
      muutSuorituskielet = s.muutSuorituskielet,
      todistuksellaNäkyvätLisätiedot = s.todistuksellaNäkyvätLisätiedot,
      osasuoritukset = s.osasuoritukset,
      tyyppi = s.tyyppi
    )
}
