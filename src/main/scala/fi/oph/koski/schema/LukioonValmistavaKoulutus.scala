package fi.oph.koski.schema

import fi.oph.koski.documentation.ExampleData.laajuusOpintopisteissä

import java.time.{LocalDate, LocalDateTime}
import fi.oph.koski.koskiuser.Rooli
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
  tila: LukionOpiskeluoikeudenTila,
  @MaxItems(1)
  suoritukset: List[LukioonValmistavanKoulutuksenSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.luva.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.luva,
  lisätiedot: Option[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot] = None,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
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
) extends KoskeenTallennettavaPäätasonSuoritus
  with Toimipisteellinen
  with Todistus
  with Arvioinniton
  with Suorituskielellinen
  with Oppimäärällinen
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

@Description("Lukioon valmistavan koulutuksen (LUVA) tunnistetiedot")
case class LukioonValmistavaKoulutus(
  @KoodistoKoodiarvo("999906")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999906", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  laajuus: Option[LaajuusOpintopisteissäTaiKursseissa] = None,
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with KoulutusmoduuliValinnainenLaajuus

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
  koulutusmoduuli: LukionOppiaine2015,
  @Description("Lukiokoulutuksen valmistavan koulutuksen todistukseen merkitään opiskelijan opiskelemat oppiaineet, niissä suoritettujen kurssien määrä tai merkintä aineryhmän tai oppiaineen hyväksytystä suorittamisesta (hyväksytty)")
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus2015]],
  @KoodistoKoodiarvo("luvalukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "luvalukionoppiaine", koodistoUri = "suorituksentyyppi")
) extends LukioonValmistavanKoulutuksenOsasuoritus with Vahvistukseton

@Title("Lukion oppiaineen opintojen suoritus 2019")
@Description("Lukion oppiaineen 2019 opintojen suoritustiedot LUVA-koulutuksessa")
case class LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019(
  @Title("Oppiaine")
  koulutusmoduuli: LukionOppiaine2019,
  @Description("Lukiokoulutuksen valmistavan koulutuksen todistukseen merkitään opiskelijan opiskelemat oppiaineet, niissä suoritettujen kurssien määrä tai merkintä aineryhmän tai oppiaineen hyväksytystä suorittamisesta (hyväksytty)")
  arviointi: Option[List[LukionOppiaineenArviointi2019]] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019]],
  @KoodistoKoodiarvo("luvalukionoppiaine2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "luvalukionoppiaine2019", koodistoUri = "suorituksentyyppi")
) extends LukioonValmistavanKoulutuksenOsasuoritus with Vahvistukseton with SuoritettavissaErityisenäTutkintona2019

trait LukioonValmistavanKoulutuksenOppiaine extends KoulutusmoduuliValinnainenLaajuus with Valinnaisuus {
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
  @KoodistoKoodiarvo("LVMFKBM")
  @KoodistoKoodiarvo("LVHIYH")
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
) extends LukioonValmistavanKoulutuksenOppiaine with PaikallinenKoulutusmoduuliKuvauksella with StorablePreference

@Title("Lukioon valmistavan kurssin tai moduulin suoritus")
case class LukioonValmistavanKurssinSuoritus(
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi,
  @Description("Kurssit arvioidaan suoritettu/hylätty-asteikolla")
  @FlattenInUI
  arviointi: Option[List[LukionArviointi]],
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("luvakurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("luvakurssi", koodistoUri = "suorituksentyyppi"),
  override val alkamispäivä: Option[LocalDate] = None
) extends KurssinSuoritus with MahdollisestiSuorituskielellinen

sealed trait LukioonValmistavanKoulutuksenKurssi extends KoulutusmoduuliValinnainenLaajuus {
  def laajuus: Option[LaajuusOpintopisteissäTaiKursseissa]
}

@Title("Valtakunnallinen lukioon valmistavan koulutuksen kurssi tai moduuli")
@Description("Valtakunnallisen lukioon valmistavan koulutuksen kurssin tai moduulin tunnistetiedot")
case class ValtakunnallinenLukioonValmistavanKoulutuksenKurssi(
  @Description("Lukioon valmistavan koulutuksen kurssi")
  @KoodistoUri("lukioonvalmistavankoulutuksenkurssit2015")
  @KoodistoUri("lukioonvalmistavankoulutuksenmoduulit2019")
  @OksaUri("tmpOKSAID873", "kurssi")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  override val laajuus: Option[LaajuusOpintopisteissäTaiKursseissa]
) extends LukioonValmistavanKoulutuksenKurssi with KoodistostaLöytyväKoulutusmoduuli

@Title("Paikallinen lukioon valmistavan koulutuksen kurssi tai moduuli")
@Description("Paikallisen lukioon valmistavan koulutuksen kurssin tunnistetiedot")
case class PaikallinenLukioonValmistavanKoulutuksenKurssi(
  @FlattenInUI
  tunniste: PaikallinenKoodi,
  override val laajuus: Option[LaajuusOpintopisteissäTaiKursseissa],
  kuvaus: LocalizedString
) extends LukioonValmistavanKoulutuksenKurssi with PaikallinenKoulutusmoduuliKuvauksella with StorablePreference

case class LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false). Lukiokoulutukseen valmistavan koulutuksen oppimäärä tulee suorittaa yhdessä vuodessa, jollei sairauden tai muun erityisen syyn vuoksi myönnetä suoritusaikaan pidennystä. (lukiolaki 21.8.1998/629 24 §)")
  @DefaultValue(false)
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Opiskelija on ulkomainen vaihto-opiskelija Suomessa (true/false). Rahoituksen laskennassa hyödynnettävä tieto.")
  @DefaultValue(false)
  ulkomainenVaihtoopiskelija: Boolean = false,
  @Description("Opintoihin liittyvien ulkomaanjaksojen tiedot. Rahoituksen laskennassa hyödynnettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @Description("Tieto onko oppijalla maksuton asuntolapaikka. Rahoituksen laskennassa hyödynnettävä tieto.")
  @DefaultValue(None)
  @RedundantData
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Boolean] = None,
  @Description("Tieto onko oppija sisäoppilaitosmaisessa majoituksessa. Rahoituksen laskennassa hyödynnettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot
  with Ulkomaanjaksollinen
  with SisäoppilaitosmainenMajoitus
  with UlkomainenVaihtoopiskelija
  with MaksuttomuusTieto
  with OikeusmaksuttomaanAsuntolapaikkaanBooleanina
