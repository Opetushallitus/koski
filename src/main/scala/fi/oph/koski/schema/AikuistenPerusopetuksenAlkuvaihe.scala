package fi.oph.koski.schema

import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.scalaschema.annotation.{Description, Title}
import LocalizedString._

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
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenOppiaine,
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

@Description("Perusopetuksen oppiaineen tunnistetiedot")
sealed trait AikuistenPerusopetuksenAlkuvaiheenOppiaine extends Koulutusmoduuli {
  @Title("Oppiaine")
  def tunniste: KoodiViite
  def laajuus = None
}

trait AikuistenPerusopetuksenAlkuvaiheenKoodistostaLöytyväOppiaine extends AikuistenPerusopetuksenAlkuvaiheenOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  @Description("Oppiaine")
  @KoodistoUri("aikuistenperusopetuksenalkuvaiheenoppiaineet")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
}

@Title("Paikallinen oppiaine")
case class AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString
) extends AikuistenPerusopetuksenAlkuvaiheenOppiaine with PaikallinenKoulutusmoduuli

@Title("Muu oppiaine")
case class MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine(
  @KoodistoKoodiarvo("MA")
  @KoodistoKoodiarvo("YH")
  @KoodistoKoodiarvo("YL")
  @KoodistoKoodiarvo("TE")
  @KoodistoKoodiarvo("OP")
  tunniste: Koodistokoodiviite
) extends AikuistenPerusopetuksenAlkuvaiheenKoodistostaLöytyväOppiaine

@Title("Äidinkieli ja kirjallisuus")
case class AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "aikuistenperusopetuksenalkuvaiheenoppiaineet"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite
) extends AikuistenPerusopetuksenAlkuvaiheenKoodistostaLöytyväOppiaine with Äidinkieli

@Title("Vieras tai toinen kotimainen kieli")
@Description("Oppiaineena vieras tai toinen kotimainen kieli")
case class AikuistenPerusopetuksenAlkuvaiheenVierasKieli(
  @KoodistoKoodiarvo("A1")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("A1", koodistoUri = "aikuistenperusopetuksenalkuvaiheenoppiaineet"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite
) extends AikuistenPerusopetuksenAlkuvaiheenKoodistostaLöytyväOppiaine with Kieliaine {
  override def description(texts: LocalizationRepository) = concat(nimi, unlocalized(", "), kieli.description)
}

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