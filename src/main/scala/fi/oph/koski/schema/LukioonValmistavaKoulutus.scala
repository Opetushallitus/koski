package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{DefaultValue, Description, MaxItems, Title}

@Description("Lukioon valmistava koulutus (LUVA)")
case class LukioonValmistavanKoulutuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  @Description("Opiskelijan opiskeluoikeuden arvioitu päättymispäivä")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä")
  päättymispäivä: Option[LocalDate],
  tila: LukionOpiskeluoikeudenTila,
  @MaxItems(1)
  suoritukset: List[LukioonValmistavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.luva.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.luva,
  lisätiedot: Option[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
}

@Description("Lukioon valmistavan koulutus (LUVA) suoritus")
case class LukioonValmistavanKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: LukioonValmistavaKoulutus,
  @KoodistoUri("lukionoppimaara")
  @Description("Tieto siitä, suoritetaanko nuorten vai aikuisten oppimäärän mukaisesti")
  @Title("Opetussuunnitelma")
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  @Description("Lukiokoulutukseen valmistavan koulutuksen suorituskieli eli se kieli, jolla opiskelija suorittaa tutkinnon (suorituksen kieli (tutkintotasoinen tieto))")
  suorituskieli: Koodistokoodiviite,
  @Description("Lukioon valmistavaan koulutukseen sisältyvien oppiaineiden ja niiden kurssien suoritukset")
  override val osasuoritukset: Option[List[LukioonValmistavanKoulutuksenOsasuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("luva")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luva", koodistoUri = "suorituksentyyppi")
) extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with Suorituskielellinen

@Description("Lukioon valmistavan koulutuksen (LUVA) tunnistetiedot")
case class LukioonValmistavaKoulutus(
  @KoodistoKoodiarvo("999906")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999906", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus {
  def laajuus = None
}

trait LukioonValmistavanKoulutuksenOsasuoritus extends Suoritus with MahdollisestiSuorituskielellinen

@Description("Lukioon valmistavan koulutuksen oppiaineen suoritustiedot LUVA-koulutuksessa")
case class LukioonValmistavanKoulutuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  @Description("Suoritetun oppiaineen tunnistetiedot. Voi olla joko paikallinen tai lukioon valmistavan koulutuksen oppiaine")
  koulutusmoduuli: LukioonValmistavanKoulutuksenOppiaine,
  @Description("Lukiokoulutuksen valmistavan koulutuksen todistukseen merkitään opiskelijan opiskelemat oppiaineet, niissä suoritettujen kurssien määrä tai merkintä aineryhmän tai oppiaineen hyväksytystä suorittamisesta (hyväksytty)")
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukioonValmistavanKurssinSuoritus]],
  @KoodistoKoodiarvo("luvaoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "luvaoppiaine", koodistoUri = "suorituksentyyppi")
) extends LukioonValmistavanKoulutuksenOsasuoritus with Vahvistukseton

@Title("Lukion oppiaineen opintojen suoritus")
@Description("Lukion oppiaineen opintojen suoritustiedot LUVA-koulutuksessa")
case class LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa(
  @Title("Oppiaine")
  koulutusmoduuli: LukionOppiaine,
  @Description("Lukiokoulutuksen valmistavan koulutuksen todistukseen merkitään opiskelijan opiskelemat oppiaineet, niissä suoritettujen kurssien määrä tai merkintä aineryhmän tai oppiaineen hyväksytystä suorittamisesta (hyväksytty)")
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]],
  @KoodistoKoodiarvo("luvalukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "luvalukionoppiaine", koodistoUri = "suorituksentyyppi")
) extends LukioonValmistavanKoulutuksenOsasuoritus with Vahvistukseton


trait LukioonValmistavanKoulutuksenOppiaine extends Koulutusmoduuli with Valinnaisuus {
  @Title("Oppiaine")
  def tunniste: KoodiViite
}

@Title("Äidinkieli ja kirjallisuus")
case class LukioonValmistavaÄidinkieliJaKirjallisuus(
  @Description("Oppiaineen tunniste")
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
) extends LukioonValmistavanKoulutuksenOppiaine with KoodistostaLöytyväKoulutusmoduuli with Äidinkieli {
  override def description = kieliaineDescription
}

case class MuutKielet(
  @Description("Oppiaineen tunniste")
  @KoodistoKoodiarvo("LVMUUTK")
  @KoodistoKoodiarvo("LVAK")
  @KoodistoKoodiarvo("LVMAI")
  @KoodistoKoodiarvo("LVPOAK")
  @KoodistoUri("oppiaineetluva")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukioonValmistavanKoulutuksenOppiaine with KoodistostaLöytyväKoulutusmoduuli with Kieliaine {
  override def description = kieliaineDescription
}

case class MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(
  @Description("Oppiaineen tunniste")
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
) extends LukioonValmistavanKoulutuksenOppiaine with PaikallinenKoulutusmoduuli with StorablePreference

case class LukioonValmistavanKurssinSuoritus(
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi,
  @Description("Kurssit arvioidaan suoritettu/hylätty-asteikolla")
  @FlattenInUI
  arviointi: Option[List[LukionKurssinArviointi]],
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("luvakurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luvakurssi", koodistoUri = "suorituksentyyppi")
) extends KurssinSuoritus with MahdollisestiSuorituskielellinen

sealed trait LukioonValmistavanKoulutuksenKurssi extends Koulutusmoduuli {
  def laajuus: Option[LaajuusKursseissa]
}

@Description("Valtakunnallisen lukioon valmistavan koulutuksen kurssin tunnistetiedot")
case class ValtakunnallinenLukioonValmistavanKoulutuksenKurssi(
  @Description("Lukioon valmistavan koulutuksen kurssi")
  @KoodistoUri("lukioonvalmistavankoulutuksenkurssit2015")
  @OksaUri("tmpOKSAID873", "kurssi")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  override val laajuus: Option[LaajuusKursseissa]
) extends LukioonValmistavanKoulutuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

@Description("Paikallisen lukioon valmistavan koulutuksen kurssin tunnistetiedot")
case class PaikallinenLukioonValmistavanKoulutuksenKurssi(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  override val laajuus: Option[LaajuusKursseissa],
  kuvaus: LocalizedString
) extends LukioonValmistavanKoulutuksenKurssi with PaikallinenKoulutusmoduuli with StorablePreference

case class LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false). Lukiokoulutukseen valmistavan koulutuksen oppimäärä tulee suorittaa yhdessä vuodessa, jollei sairauden tai muun erityisen syyn vuoksi myönnetä suoritusaikaan pidennystä. (lukiolaki 21.8.1998/629 24 §)")
  @DefaultValue(false)
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Opiskelija on ulkomainen vaihto-opiskelija Suomessa (true/false). Rahoituksen laskennassa hyödynnettävä tieto.")
  ulkomainenVaihtoopiskelija: Boolean = false,
  @Description("Opintoihin liittyvien ulkomaanjaksojen tiedot. Rahoituksen laskennassa hyödynnettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @Description("Tieto onko oppijalla maksuton asuntolapaikka. Rahoituksen laskennassa hyödynnettävä tieto.")
  @DefaultValue(false)
  @SensitiveData
  oikeusMaksuttomaanAsuntolapaikkaan: Boolean = false,
  @Description("Tieto onko oppija sisäoppilaitosmaisessa majoituksessa. Rahoituksen laskennassa hyödynnettävä tieto.")
  @SensitiveData
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None
) extends OpiskeluoikeudenLisätiedot
