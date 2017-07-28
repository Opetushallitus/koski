package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.koski.localization.LocalizedString._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.scalaschema.annotation.{DefaultValue, Description, MaxItems, Title}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  @Description("Opiskelijan opiskeluoikeuden alkamisaika lukiokoulutukseen valmistavassa koulutuksessa")
  alkamispäivä: Option[LocalDate],
  @Description("Opiskelijan opiskeluoikeuden arvioitu päättymispäivä")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä")  
  päättymispäivä: Option[LocalDate],
  tila: LukionOpiskeluoikeudenTila,
  @MaxItems(1)
  suoritukset: List[LukioonValmistavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", "opiskeluoikeudentyyppi"),
  lisätiedot: Option[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[LukioonValmistavanKoulutuksenSuoritus]])
}

@Description("Lukioon valmistavan koulutus (LUVA) suoritus")
case class LukioonValmistavanKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: LukioonValmistavaKoulutus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  @Description("Lukiokoulutukseen valmistavan koulutuksen suorituskieli eli se kieli, jolla opiskelija suorittaa tutkinnon (suorituksen kieli (tutkintotasoinen tieto)).")
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Lukioon valmistavaan koulutukseen sisältyvien oppiaineiden ja niiden kurssien suoritukset")
  override val osasuoritukset: Option[List[LukioonValmistavanKoulutuksenOsasuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", koodistoUri = "suorituksentyyppi")
) extends PäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton

@Description("Lukioon valmistavan koulutuksen (LUVA) tunnistetiedot")
case class LukioonValmistavaKoulutus(
  @KoodistoKoodiarvo("999906")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999906", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = None
) extends DiaarinumerollinenKoulutus {
  def laajuus = None
}

trait LukioonValmistavanKoulutuksenOsasuoritus extends Suoritus

@Description("Lukioon valmistavan koulutuksen oppiaineen suoritustiedot LUVA-koulutuksessa")
case class LukioonValmistavanKoulutuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: LukioonValmistavanKoulutuksenOppiaine,
  @Description("Suoritetun oppiaineen tunnistetiedot. Voi olla joko paikallinen tai lukioon valmistavan koulutuksen oppiaine")
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukioonValmistavanKurssinSuoritus]],
  @KoodistoKoodiarvo("luvaoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "luvaoppiaine", koodistoUri = "suorituksentyyppi")
) extends LukioonValmistavanKoulutuksenOsasuoritus with VahvistuksetonSuoritus

@Title("Lukion oppiaineen opintojen suoritus")
@Description("Lukion oppiaineen opintojen suoritustiedot LUVA-koulutuksessa")
case class LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa(
  @Title("Oppiaine")
  koulutusmoduuli: LukionOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]],
  @KoodistoKoodiarvo("luvalukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "luvalukionoppiaine", koodistoUri = "suorituksentyyppi")
) extends LukioonValmistavanKoulutuksenOsasuoritus with VahvistuksetonSuoritus


trait LukioonValmistavanKoulutuksenOppiaine extends Koulutusmoduuli with Valinnaisuus {
  @Title("Oppiaine")
  def tunniste: KoodiViite
}

@Title("Äidinkieli ja kirjallisuus")
case class LukioonValmistavaÄidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("LVAIK")
  @KoodistoUri("oppiaineetluva")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  @KoodistoKoodiarvo("AI7")
  @KoodistoKoodiarvo("AI8")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukioonValmistavanKoulutuksenOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  override def description(text: LocalizationRepository) = concat(nimi, ", ", kieli)
}

case class MuutKielet(
  @KoodistoKoodiarvo("LVMUUTK")
  @KoodistoUri("oppiaineetluva")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukioonValmistavanKoulutuksenOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  override def description(text: LocalizationRepository) = concat(nimi, ", ", kieli)
}

case class MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(
  @KoodistoKoodiarvo("LVMALUO")
  @KoodistoKoodiarvo("LVYHKU")
  @KoodistoKoodiarvo("LVOPO")
  @KoodistoUri("oppiaineetluva")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukioonValmistavanKoulutuksenOppiaine with KoodistostaLöytyväKoulutusmoduuli

case class PaikallinenLukioonValmistavanKoulutuksenOppiaine(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukioonValmistavanKoulutuksenOppiaine with PaikallinenKoulutusmoduuli

case class LukioonValmistavanKurssinSuoritus(
  @Title("Kurssi")
  @Flatten
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi,
  tila: Koodistokoodiviite,
  @Flatten
  arviointi: Option[List[LukionKurssinArviointi]],
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("luvakurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luvakurssi", koodistoUri = "suorituksentyyppi")
) extends VahvistuksetonSuoritus

@Description("Lukioon valmistavassa koulutuksessa suoritettava lukioon valmistavan kurssin tunnistetiedot")
case class LukioonValmistavanKoulutuksenKurssi(
  @Flatten
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKursseissa],
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli

case class LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false). Lukiokoulutukseen valmistavan koulutuksen oppimäärä tulee suorittaa yhdessä vuodessa, jollei sairauden tai muun erityisen syyn vuoksi myönnetä suoritusaikaan pidennystä. (lukiolaki 21.8.1998/629 24 §)")
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Opiskelija on ulkomainen vaihto-opiskelija Suomessa (true/false)")
  ulkomainenVaihtoopiskelija: Boolean = false,
  @Description("Opintoihin liittyvien ulkomaanjaksojen tiedot")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @Description("Tieto onko oppijalla maksuton asuntolapaikka.")
  @DefaultValue(false)
  oikeusMaksuttomaanAsuntolapaikkaan: Boolean = false
) extends OpiskeluoikeudenLisätiedot
