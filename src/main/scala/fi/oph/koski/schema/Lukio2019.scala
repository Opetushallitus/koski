package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{DefaultValue, Description, Discriminator, MinItems, Title}

trait LukionPäätasonSuoritus2019 extends LukionPäätasonSuoritus with Todistus with Arvioinniton with PuhviKokeellinen2019 with SuullisenKielitaidonKokeellinen2019

@Title("Lukion oppimäärän suoritus 2019")
@Description("Lukion oppimäärän opetussuunnitelman 2019 mukaiset suoritustiedot")
case class LukionOppimääränSuoritus2019(
  @Title("Koulutus")
  koulutusmoduuli: LukionOppimäärä,
  @KoodistoUri("lukionoppimaara")
  @Description("Tieto siitä, suoritetaanko lukiota nuorten vai aikuisten oppimäärän mukaisesti")
  @Title("Opetussuunnitelma")
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  @Description("Oppimäärän suorituksen opetuskieli/suorituskieli. Rahoituksen laskennassa käytettävä tieto.")
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Osallistuminen lukiokoulutusta täydentävän saamen/romanikielen/opiskelijan oman äidinkielen opiskeluun")
  omanÄidinkielenOpinnot: Option[OmanÄidinkielenOpinnotLaajuusOpintopisteinä] = None,
  puhviKoe: Option[PuhviKoe2019] = None,
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppimääränOsasuoritus2019]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppimaara2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara2019", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus2019 with Ryhmällinen with KoulusivistyskieliKieliaineesta with SuoritettavissaErityisenäTutkintona2019

@Title("Lukion oppiaineiden oppimäärien suoritus 2019")
@Description("Lukion oppiaineiden oppimäärien suoritustiedot 2019")
case class LukionOppiaineidenOppimäärienSuoritus2019(
  @Title("Oppiaine")
  koulutusmoduuli: LukionOppiaineidenOppimäärät2019,
  toimipiste: OrganisaatioWithOid,
  suorituskieli: Koodistokoodiviite,
  @Description("Merkitään, jos lukion oppimäärä on tullut suoritetuksi aineopintoina.")
  @DefaultValue(false)
  lukionOppimääräSuoritettu: Boolean = false,
  puhviKoe: Option[PuhviKoe2019] = None,
  suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppiaineenSuoritus2019]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppiaineidenoppimaarat2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppiaineidenoppimaarat2019", koodistoUri = "suorituksentyyppi"),
) extends LukionPäätasonSuoritus2019 {
  // TODO: Päätason suorituksesta ei voi tehdä Vahvistukseton:ta, siksi korvattu tässä. Tämä voi aiheuttaa
  // ongelmia todistusten luonnissa tms., ratkaise ne sitten. Voi olla, että tämän pitäisi päätellä arvonsa
  // osasuorituksista.
  override def vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None
}

// TODO: Mitä tämän pitäisi sisältää oppiaineiden oppimäärien suoritukset ryhmittelevässä päätason suorituksessa?
@Title("Lukion oppiaineiden oppimäärät 2019")
case class LukionOppiaineidenOppimäärät2019(
  tunniste: LukionOppiaineidenOppimäärätKoodi2019,
  laajuus: Option[Laajuus] = None,
  nimi: LocalizedString
) extends Koulutusmoduuli

@Title("Lukion oppiaineiden oppimäärät -koodi 2019")
@Description("Koodi, jota käytetään sisäisesti lukion oppiaineiden oppimäärien ryhmittelyssä 2019")
case class LukionOppiaineidenOppimäärätKoodi2019(
  @Description("Merkkijono sisällöllä \"Oppiaineiden oppimäärät\"")
  @Title("Tunniste")
  @DefaultValue("Oppiaineiden oppimäärät")
  koodiarvo: String = "Oppiaineiden oppimäärät",
  @Description("Merkkijono sisällöllä \"Oppiaineiden oppimäärät\"")
  @DefaultValue("Oppiaineiden oppimäärät")
  @Representative
  nimi: LocalizedString = LocalizedString.finnish("Oppiaineiden oppimäärät"),
  @Description("Tyhjä")
  @Title("Koodisto-URI")
  @DefaultValue(None)
  koodistoUri: Option[String] = None
) extends KoodiViite {
  override def toString = s"$koodiarvo (${nimi.get("fi")})"
  def getNimi = Some(nimi)
  def description: LocalizedString = nimi
}

trait LukionOppimääränOsasuoritus2019 extends LukionOppimääränPäätasonOsasuoritus

@Title("Muiden lukio-opintojen suoritus 2019")
@Description("Kategoria opintojaksoille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esim. lukiodiplomit tai temaattiset opinnot.")
case class MuidenLukioOpintojenSuoritus2019(
  @KoodistoKoodiarvo("lukionmuuopinto2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionmuuopinto2019", "suorituksentyyppi"),
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  koulutusmoduuli: MuutSuorituksetTaiLukiodiplomit2019,
  @MinItems(1)
  @Description("Moduulien ja paikallisten opintojaksojen suoritukset")
  @Title("Moduulit ja paikalliset opintojaksot")
  override val osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019]]
) extends LukionOppimääränOsasuoritus2019 with PreIBSuorituksenOsasuoritus with Vahvistukseton

@Title("Lukion oppiaineen suoritus 2019")
@Description("Lukion oppiaineen suoritustiedot 2019")
case class LukionOppiaineenSuoritus2019(
  koulutusmoduuli: LukionOppiaine2019,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suoritettuErityisenäTutkintona: Boolean = false,
  @Title("Suorituskieli, jos muu kuin opetuskieli")
  @Description("Suorituskieli, mikäli opiskelija on opiskellut yli puolet oppiaineen oppimäärän opinnoista muulla kuin koulun varsinaisella opetuskielellä.")
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien moduulien ja paikallisten opintojaksojen suoritukset")
  @Title("Moduulit ja paikalliset opintojaksot")
  override val osasuoritukset: Option[List[LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019]],
  @KoodistoKoodiarvo("lukionoppiaine2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine2019", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with Vahvistukseton with LukionOppimääränOsasuoritus2019 with MahdollisestiSuorituskielellinen with SuoritettavissaErityisenäTutkintona2019

trait LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 extends Suoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu with Vahvistukseton {
  @FlattenInUI
  def arviointi: Option[List[LukionArviointi]]
  @ComplexObject
  def tunnustettu: Option[OsaamisenTunnustaminen]
}

@Title("Lukion moduulin suoritus 2019")
@Description("Lukion moduulin suoritustiedot 2019")
case class LukionModuulinSuoritus2019(
  @Description("Lukion moduulin tunnistetiedot")
  koulutusmoduuli: LukionModuuli2019,
  arviointi: Option[List[LukionArviointi]] = None,
  // TODO: descriptionin lakiviitteet yms. teksti
  @Description("Jos moduuli on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan moduuliin, tulee moduulista antaa numeroarvosana")
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionvaltakunnallinenmoduuli2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionvaltakunnallinenmoduuli2019", koodistoUri = "suorituksentyyppi")
) extends LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 with ValtakunnallisenModuulinSuoritus with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu

trait MuutSuorituksetTaiLukiodiplomit2019 extends KoodistostaLöytyväKoulutusmoduuli {
  @KoodistoUri("lukionmuutopinnot")
  def tunniste: Koodistokoodiviite
}

@Title("Muut suoritukset 2019")
@Description("Kategoria moduuleille ja opintojaksoille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen.")
case class MuutLukionSuoritukset2019(
  @KoodistoKoodiarvo("MS")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends MuutSuorituksetTaiLukiodiplomit2019

@Title("Lukiodiplomit 2019")
@Description("Kategoria lukiodiplomeille 2019.")
case class Lukiodiplomit2019(
  @KoodistoKoodiarvo("LD")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä] = None
) extends MuutSuorituksetTaiLukiodiplomit2019

@Title("Lukion paikallisen opintojakson suoritus 2019")
@Description("Lukion paikallisen opintojakson suoritustiedot 2019")
case class LukionPaikallisenOpintojaksonSuoritus2019(
  @Title("Paikallinen opintojakso")
  @Description("Lukion paikallisen opintojakson tunnistetiedot")
  @FlattenInUI
  koulutusmoduuli: LukionPaikallinenOpintojakso2019,
  arviointi: Option[List[LukionArviointi]] = None,
  // TODO: Descriptionin lakiviitteet ja teksti
  @Description("Jos opintojakso on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia pakollisia tai vapaaehtoisia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan opintojaksoon, tulee opintojaksosta antaa numeroarvosana")
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionpaikallinenopintojakso2019")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionpaikallinenopintojakso2019", koodistoUri = "suorituksentyyppi")
) extends LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 with MahdollisestiSuorituskielellinen with MahdollisestiTunnustettu with Vahvistukseton

trait LukionModuuliTaiPaikallinenOpintojakso2019 extends Koulutusmoduuli with PreIBKurssi with Valinnaisuus {
  def laajuus: Option[LaajuusOpintopisteissä]
}

@Title("Lukion moduuli 2019")
@Description("Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot")
case class LukionModuuli2019(
  @Description("Lukion/IB-lukion valtakunnallinen moduuli")
  @KoodistoUri("moduulikoodistolops2021")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusOpintopisteissä],
  pakollinen: Boolean
) extends LukionModuuliTaiPaikallinenOpintojakso2019 with KoodistostaLöytyväKoulutusmoduuli

@Title("Lukion paikallinen opintojakso 2019")
@Description("Paikallisen lukion/IB-lukion opintojakson tunnistetiedot 2019")
case class LukionPaikallinenOpintojakso2019(
  tunniste: PaikallinenKoodi,
  @Discriminator
  laajuus: Option[LaajuusOpintopisteissä],
  kuvaus: LocalizedString,
  pakollinen: Boolean
) extends LukionModuuliTaiPaikallinenOpintojakso2019 with PaikallinenKoulutusmoduuli with StorablePreference

@Description("Lukion/IB-lukion oppiaineen tunnistetiedot 2019")
trait LukionOppiaine2019 extends Koulutusmoduuli with Valinnaisuus with PreIBOppiaine with Diaarinumerollinen {
  def laajuus: Option[LaajuusOpintopisteissä]
  @Title("Oppiaine")
  def tunniste: KoodiViite
}

@Title("Paikallinen oppiaine 2019")
case class PaikallinenLukionOppiaine2019(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  pakollinen: Boolean = false,
  @Discriminator
  laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionOppiaine2019 with PaikallinenKoulutusmoduuli with StorablePreference

trait LukionValtakunnallinenOppiaine2019 extends LukionOppiaine2019 with YleissivistavaOppiaine

@Title("Muu valtakunnallinen oppiaine 2019")
case class LukionMuuValtakunnallinenOppiaine2019(
  @KoodistoKoodiarvo("BI")
  @KoodistoKoodiarvo("ET")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("FY")
  @KoodistoKoodiarvo("GE")
  @KoodistoKoodiarvo("HI")
  @KoodistoKoodiarvo("KE")
  @KoodistoKoodiarvo("KU")
  @KoodistoKoodiarvo("LI")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("OP")
  @KoodistoKoodiarvo("PS")
  @KoodistoKoodiarvo("TE")
  @KoodistoKoodiarvo("YH")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  override val laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2019

@Title("Uskonto 2019")
case class LukionUskonto2019(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  @Discriminator
  override val laajuus: Option[LaajuusOpintopisteissä] = None,
  uskonnonOppimäärä: Option[Koodistokoodiviite] = None
) extends LukionValtakunnallinenOppiaine2019 with Uskonto

@Title("Äidinkieli ja kirjallisuus 2019")
@Description("Oppiaineena äidinkieli ja kirjallisuus")
case class LukionÄidinkieliJaKirjallisuus2019(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  override val laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2019 with Äidinkieli {
  override def description: LocalizedString = kieliaineDescription
}

@Title("Vieras tai toinen kotimainen kieli 2019")
@Description("Oppiaineena vieras tai toinen kotimainen kieli 2019")
case class VierasTaiToinenKotimainenKieli2019(
  @KoodistoKoodiarvo("A")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  @KoodistoKoodiarvo("AOM") // TODO: rajoita kielivaihtoehdot suomi+ruotsi? Miksei tätä ole tehty myös perinteisessä 2. kotimaisessa?
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  override val laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2019 with Kieliaine {
  override def description = kieliaineDescription
}

@Title("Matematiikka 2019")
@Description("Oppiaineena matematiikka")
case class LukionMatematiikka2019(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  @Discriminator
  override val laajuus: Option[LaajuusOpintopisteissä] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine2019 with KoodistostaLöytyväKoulutusmoduuli with Oppimäärä {
  override def description = oppimäärä.description
}

trait SuoritettavissaErityisenäTutkintona2019 {
  @DefaultValue(false)
  def suoritettuErityisenäTutkintona: Boolean
}

trait PuhviKokeellinen2019 {
  @DefaultValue(None)
  @Title("Puhvi-koe")
  def puhviKoe: Option[PuhviKoe2019]
}

@Title("Puhvi-koe")
@Description("Toisen asteen puheviestintätaitojen päättökoe")
case class PuhviKoe2019(
  arviointi: LukionArviointi
)

trait SuullisenKielitaidonKokeellinen2019 {
  @DefaultValue(None)
  def suullisenKielitaidonKokeet: Option[List[SuullisenKielitaidonKoe2019]]
}

@Title("Suullisen kielitaidon koe")
case class SuullisenKielitaidonKoe2019(
  @KoodistoUri("kieli")
  kieli: Koodistokoodiviite,
  arviointi: SuullisenKielitaidonKokeenArviointi2019
)

trait SuullisenKielitaidonKokeenArviointi2019 extends ArviointiPäivämäärällä {
  @KoodistoUri("arviointiasteikkosuullisenkielitaidonkoetaitotaso")
  def taitotaso: Koodistokoodiviite
}

@Title("Numeerinen suullisen kielitaidon kokeen arviointi")
case class NumeerinenSuullisenKielitaidonKokeenArviointi2019(
  arvosana: Koodistokoodiviite,
  taitotaso: Koodistokoodiviite,
  päivä: LocalDate
) extends SuullisenKielitaidonKokeenArviointi2019 with NumeerinenYleissivistävänKoulutuksenArviointi

@Title("Sanallinen suullisen kielitaidon kokeen arviointi")
case class SanallinenSuullisenKielitaidonKokeenArviointi2019(
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  taitotaso: Koodistokoodiviite,
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends SuullisenKielitaidonKokeenArviointi2019 with SanallinenYleissivistävänKoulutuksenArviointi
