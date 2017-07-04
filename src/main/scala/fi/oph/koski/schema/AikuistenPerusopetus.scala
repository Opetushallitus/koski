package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, Title}

@Description("Perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella.")
case class AikuistenPerusopetuksenOppimääränSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: Perusopetus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  @KoodistoUri("perusopetuksensuoritustapa")
  @Description("Tieto siitä, suoritetaanko perusopetusta normaalina koulutuksena vai erityisenä tutkintona")
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenOppiaineenSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetuksenoppimaara", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenPäätasonSuoritus with PerusopetuksenOppimääränSuoritus with Todistus with Arvioinniton

@Description("Perusopetuksen oppiaineen suoritus osana aikuisten perusopetuksen oppimäärän suoritusta")
case class AikuistenPerusopetuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PerusopetuksenOppiaine,
  yksilöllistettyOppimäärä: Boolean = false,
  @Description("Tieto siitä, onko oppiaineen opetus painotettu (true/false)")
  painotettuOpetus: Boolean = false,
  tila: Koodistokoodiviite,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with VahvistuksetonSuoritus with Yksilöllistettävä with MahdollisestiSuorituskielellinen

case class AikuistenPerusopetuksenKurssinSuoritus(
  @Description("Aikuisten perusopetuksen kurssin tunnistetiedot")
  @Title("Kurssi")
  @Flatten
  koulutusmoduuli: AikuistenPerusopetuksenKurssi,
  tila: Koodistokoodiviite,
  @Flatten
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenkurssi", koodistoUri = "suorituksentyyppi")
) extends VahvistuksetonSuoritus with MahdollisestiSuorituskielellinen

sealed trait AikuistenPerusopetuksenKurssi extends Koulutusmoduuli {
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

case class PaikallinenAikuistenPerusopetuksenKurssi(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends AikuistenPerusopetuksenKurssi with PaikallinenKoulutusmoduuli

@Title("Aikuisten perusopetuksen opetussuunnitelman 2015 mukainen kurssi")
case class ValtakunnallinenAikuistenPerusopetuksenKurssi2015(
  @KoodistoUri("aikuistenperusopetuksenkurssit2015")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends AikuistenPerusopetuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

@Title("Aikuisten perusopetuksen päättövaiheen opetussuunnitelman 2017 mukainen kurssi")
case class ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017(
  @KoodistoUri("aikuistenperusopetuksenpaattovaiheenkurssit2017")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends AikuistenPerusopetuksenKurssi with KoodistostaLöytyväKoulutusmoduuli