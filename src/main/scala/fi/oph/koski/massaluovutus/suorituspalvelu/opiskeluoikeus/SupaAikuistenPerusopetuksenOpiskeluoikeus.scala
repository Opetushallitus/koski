package fi.oph.koski.massaluovutus.suorituspalvelu.opiskeluoikeus

import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.util.CaseClass
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate

@Title("Aikuisten perusopetuksen opiskeluoikeus")
case class SupaAikuistenPerusopetuksenOpiskeluoikeus(
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo)
  tyyppi: Koodistokoodiviite,
  oid: String,
  koulutustoimija: Option[Koulutustoimija],
  oppilaitos: Option[Oppilaitos],
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila,
  suoritukset: List[SupaAikuistenPerusopetuksenSuoritus],
) extends SupaOpiskeluoikeus

object SupaAikuistenPerusopetuksenOpiskeluoikeus {
  def apply(oo: AikuistenPerusopetuksenOpiskeluoikeus): SupaAikuistenPerusopetuksenOpiskeluoikeus =
    SupaAikuistenPerusopetuksenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      koulutustoimija = oo.koulutustoimija,
      oppilaitos = oo.oppilaitos,
      tila = oo.tila,
      suoritukset = oo.suoritukset.flatMap(SupaAikuistenPerusopetuksenSuoritus.apply),
    )
}

sealed trait SupaAikuistenPerusopetuksenSuoritus extends SupaSuoritus

object SupaAikuistenPerusopetuksenSuoritus {
  def apply(pts: AikuistenPerusopetuksenPäätasonSuoritus): Option[SupaAikuistenPerusopetuksenSuoritus] =
    pts match {
      case s: AikuistenPerusopetuksenOppimääränSuoritus =>
        Some(SupaAikuistenPerusopetuksenOppimääränSuoritus(s))
      case s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus =>
        Some(SupaAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(s))
      case _ =>
        None
    }
}

@Title("Aikuisten perusopetuksen oppimäärän suoritus")
case class SupaAikuistenPerusopetuksenOppimääränSuoritus(
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistus: Option[SupaVahvistus],
  koulutusmoduuli: AikuistenPerusopetus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: List[AikuistenPerusopetuksenOppiaineenSuoritus],
) extends SupaAikuistenPerusopetuksenSuoritus with SupaVahvistuksellinen

object SupaAikuistenPerusopetuksenOppimääränSuoritus {
  def apply(s: AikuistenPerusopetuksenOppimääränSuoritus): SupaAikuistenPerusopetuksenOppimääränSuoritus =
    SupaAikuistenPerusopetuksenOppimääränSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset.toList.flatten,
    )
}

@Title("Aikuisten perusopetuksen oppiaineen oppimäärän suoritus")
case class SupaAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset.")
  koulutusmoduuli: AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine,
  toimipiste: OrganisaatioWithOid,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  vahvistus: Option[SupaVahvistus] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends SupaAikuistenPerusopetuksenSuoritus with SupaVahvistuksellinen

object SupaAikuistenPerusopetuksenOppiaineenOppimääränSuoritus {
  def apply(s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus): SupaAikuistenPerusopetuksenOppiaineenOppimääränSuoritus =
    SupaAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(
      koulutusmoduuli = s.koulutusmoduuli,
      toimipiste = s.toimipiste,
      arviointi = s.arviointi,
      vahvistus = s.vahvistus.map(v => SupaVahvistus(v.päivä)),
      suoritustapa = s.suoritustapa,
      suorituskieli = s.suorituskieli,
      muutSuorituskielet = s.muutSuorituskielet,
      todistuksellaNäkyvätLisätiedot = s.todistuksellaNäkyvätLisätiedot,
      osasuoritukset = s.osasuoritukset,
      tyyppi = s.tyyppi
    )
}
