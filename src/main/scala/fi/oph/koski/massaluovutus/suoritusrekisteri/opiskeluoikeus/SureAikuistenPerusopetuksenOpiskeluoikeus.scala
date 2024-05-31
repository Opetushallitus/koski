package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureOpiskeluoikeus
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Description

import java.time.LocalDate

case class SureAikuistenPerusopetuksenOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila,
  suoritukset: List[SureAikuistenPerusopetuksenSuoritus],
) extends SureOpiskeluoikeus

object SureAikuistenPerusopetuksenOpiskeluoikeus {
  def apply(oo: AikuistenPerusopetuksenOpiskeluoikeus): SureAikuistenPerusopetuksenOpiskeluoikeus =
    SureAikuistenPerusopetuksenOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.get,
      oppilaitos = oo.oppilaitos,
      koulutustoimija = oo.koulutustoimija,
      tila = oo.tila,
      suoritukset = oo.suoritukset.flatMap(SureAikuistenPerusopetuksenSuoritus.apply)
    )
}

sealed trait SureAikuistenPerusopetuksenSuoritus extends SurePäätasonSuoritus

object SureAikuistenPerusopetuksenSuoritus {
  def apply(pts: AikuistenPerusopetuksenPäätasonSuoritus): Option[SureAikuistenPerusopetuksenSuoritus] =
    pts match {
      case s: AikuistenPerusopetuksenOppimääränSuoritus =>
        Some(SureAikuistenPerusopetuksenOppimääränSuoritus(s))
      case s: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus =>
        Some(SureAikusitenPerusopetuksenOppiaineenOppimääränSuoritus(s))
      case _ =>
        None
    }
}

case class SureAikuistenPerusopetuksenOppimääränSuoritus(
  tyyppi: Koodistokoodiviite,
  alkamispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
  koulutusmoduuli: AikuistenPerusopetus,
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[AikuistenPerusopetuksenOppiaineenSuoritus]],
) extends SureAikuistenPerusopetuksenSuoritus

object SureAikuistenPerusopetuksenOppimääränSuoritus {
  def apply(s: AikuistenPerusopetuksenOppimääränSuoritus): SureAikuistenPerusopetuksenOppimääränSuoritus =
    SureAikuistenPerusopetuksenOppimääränSuoritus(
      tyyppi = s.tyyppi,
      alkamispäivä = s.alkamispäivä,
      vahvistuspäivä = s.vahvistus.map(_.päivä),
      koulutusmoduuli = s.koulutusmoduuli,
      suorituskieli = s.suorituskieli,
      osasuoritukset = s.osasuoritukset,
    )
}

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
