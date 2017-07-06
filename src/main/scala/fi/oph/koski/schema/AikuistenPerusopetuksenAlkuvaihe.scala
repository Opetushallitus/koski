package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString
import fi.oph.scalaschema.annotation.{Description, Title}

case class AikuistenPerusopetuksenAlkuvaiheenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AikuistenPerusopetus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenoppimaaranalkuvaihe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("aikuistenperusopetuksenoppimaaranalkuvaihe", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenPäätasonSuoritus with Suoritus with Todistus with Arvioinniton with SuoritustavallinenPerusopetuksenSuoritus

@Description("Oppiaineen suoritus osana aikuisten perusopetuksen oppimäärän alkuvaiheen suoritusta")
case class AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetuksenOppiaine,
  yksilöllistettyOppimäärä: Boolean = false,
  @Description("Tieto siitä, onko oppiaineen opetus painotettu (true/false)")
  painotettuOpetus: Boolean = false,
  tila: Koodistokoodiviite,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Title("Kurssit")
  override val osasuoritukset: Option[List[AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus]] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenalkuvaiheenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenalkuvaiheenoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with VahvistuksetonSuoritus with Yksilöllistettävä with MahdollisestiSuorituskielellinen

case class AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus(
  @Description("Aikuisten perusopetuksen alkuvaiheen kurssin tunnistetiedot")
  @Title("Kurssi")
  @Flatten
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenKurssi,
  tila: Koodistokoodiviite,
  @Flatten
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("aikuistenperusopetuksenalkuvaiheenkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "aikuistenperusopetuksenalkuvaiheenkurssi", koodistoUri = "suorituksentyyppi")
) extends VahvistuksetonSuoritus with MahdollisestiSuorituskielellinen

sealed trait AikuistenPerusopetuksenAlkuvaiheenKurssi extends Koulutusmoduuli {
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

case class PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi(
  @Flatten
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends AikuistenPerusopetuksenAlkuvaiheenKurssi with PaikallinenKoulutusmoduuli

@Title("Aikuisten perusopetuksen opetussuunnitelman 2017 mukainen kurssi")
case class ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017(
  @KoodistoUri("aikuistenperusopetuksenalkuvaiheenkurssit2017")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends AikuistenPerusopetuksenAlkuvaiheenKurssi with KoodistostaLöytyväKoulutusmoduuli