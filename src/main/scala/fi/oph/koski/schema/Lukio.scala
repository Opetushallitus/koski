package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.koski.localization.LocalizedString.{concat, finnish}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.scalaschema.annotation.{DefaultValue, Description, MinItems, Title}

@Description("Lukion opiskeluoikeus")
case class LukionOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko koko lukiokoulutuksen oppimäärätavoitteisessa koulutuksessa tai oppiaineen oppimäärätavoitteisessa koulutuksessa.")
  alkamispäivä: Option[LocalDate] = None,
  @Description("Opiskelijan opiskeluoikeuden arvioitu päättymispäivä joko koko lukiokoulutuksen oppimäärätavoitteisessa koulutuksessa tai oppiaineen oppimäärätavoitteisessa koulutuksessa.")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä joko koko lukiokoulutuksen oppimäärätavoitteisessa koulutuksessa tai oppiaineen oppimäärätavoitteisessa koulutuksessa.")
  päättymispäivä: Option[LocalDate] = None,
  tila: LukionOpiskeluoikeudenTila,
  lisätiedot: Option[LukionOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[LukionPäätasonSuoritus],
  @KoodistoKoodiarvo("lukiokoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukiokoulutus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[LukionPäätasonSuoritus]])
}

@Description("Lukion opiskeluoikeuden lisätiedot")
case class LukionOpiskeluoikeudenLisätiedot(
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false). Lukion oppimäärä tulee suorittaa enintään neljässä vuodessa, jollei opiskelijalle perustellusta syystä myönnetä suoritusaikaan pidennystä (lukiolaki 21.8.1998/629 24 §)")
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Opiskelija on ulkomainen vaihto-opiskelija Suomessa (true/false)")
  @Title("Ulkomainen vaihto-opiskelija")
  ulkomainenVaihtoopiskelija: Boolean = false,
  @Description("Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei opiskelija opiskele aikuisten lukiokoulutuksessa alle 18-vuotiaana")
  @Title("Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa")
  alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy: Option[LocalizedString] = None,
  @Description("Yksityisopiskelija aikuisten lukiokoulutuksessa (true/false)")
  yksityisopiskelija: Boolean = false,
  erityisenKoulutustehtävänJaksot: Option[List[ErityisenKoulutustehtävänJakso]] = None,
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @Description("Tieto onko oppijalla maksuton asuntolapaikka")
  @DefaultValue(false)
  oikeusMaksuttomaanAsuntolapaikkaan: Boolean = false
) extends OpiskeluoikeudenLisätiedot


@Description("Opiskelija opiskelee erityisen koulutustehtävän mukaisesti (ib, musiikki, urheilu, kielet, luonnontieteet, jne.). Kentän puuttuminen tai null-arvo tulkitaan siten, ettei opiskelija opiskele erityisen koulutustehtävän mukaisesti.")
case class ErityisenKoulutustehtävänJakso(
  @Description("Opiskelijan erityisen koulutustehtävän mukaisen koulutuksen jakson alkupäivämäärä")
  alku: LocalDate,
  @Description("Opiskelijan erityisen koulutustehtävän mukaisen koulutuksen jakson loppupäivämäärä")
  loppu: Option[LocalDate],
  @Description("Erityinen koulutustehtävä. Koodisto.")
  @KoodistoUri("erityinenkoulutustehtava")
  @OksaUri("tmpOKSAID181", "erityinen koulutustehtävä")
  tehtävä: Koodistokoodiviite
) extends Jakso

trait LukionPäätasonSuoritus extends PäätasonSuoritus with Toimipisteellinen

@Description("Lukion oppimäärän suoritustiedot")
case class LukionOppimääränSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: LukionOppimäärä,
  @KoodistoUri("lukionoppimaara")
  @Description("Tieto siitä, suoritetaanko lukiota nuorten vai aikuisten oppimäärän mukaisesti")
  @Title("Opetussuunnitelma")
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppimääränOsasuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus with Todistus with Arvioinniton with Ryhmällinen

@Description("Lukion oppiaineen oppimäärän suoritustiedot")
case class LukionOppiaineenOppimääränSuoritus(
  @Title("Oppiaine")
  @Flatten
  koulutusmoduuli: LukionOppiaine,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  @Description("Lukion oppiaineen oppimäärän arviointi")
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]],
  @Description("Todistuksella näytettävä lisätieto, vapaamuotoinen tekstikenttä.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppiaineenoppimaara", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends LukionPäätasonSuoritus with Todistus with Ryhmällinen

@Description("Lukiokoulutuksen tunnistetiedot")
case class LukionOppimäärä(
 @KoodistoKoodiarvo("309902")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("309902", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String]
) extends DiaarinumerollinenKoulutus {
  override def laajuus = None
  override def isTutkinto = true
}

trait LukionOppimääränOsasuoritus extends Suoritus

@Title("Muiden lukio-opintojen suoritus")
@Description("Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot")
case class MuidenLukioOpintojenSuoritus(
  @KoodistoKoodiarvo("lukionmuuopinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionmuuopinto", "suorituksentyyppi"),
  tila: Koodistokoodiviite,
  koulutusmoduuli: MuuLukioOpinto,
  @MinItems(1)
  @Description("Kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]]
) extends LukionOppimääränOsasuoritus with VahvistuksetonSuoritus with Arvioinniton {
  override def suorituskieli = None
}

@Title("Muu lukio-opinto")
@Description("Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot")
case class MuuLukioOpinto(
  @KoodistoUri("lukionmuutopinnot")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusKursseissa] = None
) extends KoodistostaLöytyväKoulutusmoduuli

@Description("Lukion oppiaineen suoritustiedot")
case class LukionOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: LukionOppiaine,
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineeseen kuuluvien kurssien suoritukset")
  @Title("Kurssit")
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]],
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with VahvistuksetonSuoritus with LukionOppimääränOsasuoritus

@Description("Lukion kurssin suoritustiedot")
case class LukionKurssinSuoritus(
  @Description("Lukion kurssin tunnistetiedot")
  @Title("Kurssi")
  @Flatten
  koulutusmoduuli: LukionKurssi,
  tila: Koodistokoodiviite,
  @Flatten
  arviointi: Option[List[LukionKurssinArviointi]] = None,
  @Description("Jos kurssi on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot. Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia pakollisia, syventäviä tai soveltavia opintoja. Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä. Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan kurssiin, tulee kurssista antaa numeroarvosana.")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("lukionkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionkurssi", koodistoUri = "suorituksentyyppi"),
  suoritettuLukiodiplomina: Option[Boolean] = None,
  suoritettuSuullisenaKielikokeena: Option[Boolean] = None
) extends VahvistuksetonSuoritus

case class LukionOppiaineenArviointi(
  @Description("Oppiaineen suorituksen arvosana on kokonaisarvosana oppiaineelle.")
  arvosana: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate]
) extends YleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

object LukionOppiaineenArviointi {
  def apply(arvosana: String) = new LukionOppiaineenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), None)
}

trait LukionKurssinArviointi extends ArviointiPäivämäärällä

case class NumeerinenLukionKurssinArviointi(
  arvosana: Koodistokoodiviite,
  päivä: LocalDate
) extends LukionKurssinArviointi with NumeerinenYleissivistävänKoulutuksenArviointi

case class SanallinenLukionKurssinArviointi(
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString],
  päivä: LocalDate
) extends LukionKurssinArviointi with SanallinenYleissivistävänKoulutuksenArviointi

sealed trait LukionKurssi extends Koulutusmoduuli with PreIBKurssi {
  def laajuus: Option[LaajuusKursseissa]
  @KoodistoUri("lukionkurssintyyppi")
  def kurssinTyyppi: Koodistokoodiviite
}

@Description("Valtakunnallisen lukion/IB-lukion kurssin tunnistetiedot")
case class ValtakunnallinenLukionKurssi(
  @Description("Lukion/IB-lukion kurssi")
  @KoodistoUri("lukionkurssit")
  @KoodistoUri("lukionkurssitops2004aikuiset")
  @KoodistoUri("lukionkurssitops2003nuoret")
  @OksaUri("tmpOKSAID873", "kurssi")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  override val laajuus: Option[LaajuusKursseissa],
  @Description("Valtakunnallisen kurssin tyyppi voi olla joko pakollinen tai syventävä.")
  @KoodistoKoodiarvo("pakollinen")
  @KoodistoKoodiarvo("syventava")
  kurssinTyyppi: Koodistokoodiviite
) extends LukionKurssi with KoodistostaLöytyväKoulutusmoduuli

@Description("Paikallisen lukion/IB-lukion kurssin tunnistetiedot")
case class PaikallinenLukionKurssi(
  @Flatten
  tunniste: PaikallinenKoodi,
  override val laajuus: Option[LaajuusKursseissa],
  kuvaus: LocalizedString,
  @Description("Paikallisen kurssin tyyppi voi olla joko syventävä tai soveltava.")
  @KoodistoKoodiarvo("syventava")
  @KoodistoKoodiarvo("soveltava")
  kurssinTyyppi: Koodistokoodiviite
) extends LukionKurssi with PaikallinenKoulutusmoduuli

@Description("Lukion/IB-lukion oppiaineen tunnistetiedot")
trait LukionOppiaine extends Koulutusmoduuli with Valinnaisuus with PreIBOppiaine with Diaarinumerollinen {
  def laajuus: Option[LaajuusKursseissa]
  @Title("Oppiaine")
  def tunniste: KoodiViite
}

@Title("Paikallinen oppiaine")
case class PaikallinenLukionOppiaine(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  pakollinen: Boolean = true,
  laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionOppiaine with PaikallinenKoulutusmoduuli

trait LukionValtakunnallinenOppiaine extends LukionOppiaine with YleissivistavaOppiaine

@Title("Muu valtakunnallinen oppiaine")
case class LukionMuuValtakunnallinenOppiaine(
  @KoodistoKoodiarvo("HI")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("BI")
  @KoodistoKoodiarvo("PS")
  @KoodistoKoodiarvo("KT")
  @KoodistoKoodiarvo("KO")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("KE")
  @KoodistoKoodiarvo("YH")
  @KoodistoKoodiarvo("TE")
  @KoodistoKoodiarvo("KS")
  @KoodistoKoodiarvo("FY")
  @KoodistoKoodiarvo("GE")
  @KoodistoKoodiarvo("LI")
  @KoodistoKoodiarvo("KU")
  @KoodistoKoodiarvo("OP")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine

@Title("Äidinkieli ja kirjallisuus")
@Description("Oppiaineena äidinkieli ja kirjallisuus")
case class LukionÄidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine

@Description("Oppiaineena vieras tai toinen kotimainen kieli")
case class VierasTaiToinenKotimainenKieli(
  @KoodistoKoodiarvo("A1")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine {
  override def description(text: LocalizationRepository) = concat(nimi, ", ", kieli)
}

@Title("Matematiikka")
@Description("Oppiaineena matematiikka")
case class LukionMatematiikka(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None,
  perusteenDiaarinumero: Option[String] = None
) extends LukionValtakunnallinenOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  override def description(text: LocalizationRepository) = oppimäärä.description
}

case class LaajuusKursseissa(
  arvo: Float,
  @KoodistoKoodiarvo("4")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "4", nimi = Some(finnish("kurssia")))
) extends Laajuus

@Description("Ks. tarkemmin lukion ja IB-tutkinnon opiskeluoikeuden tilat: https://confluence.csc.fi/display/OPHPALV/KOSKI+opiskeluoikeuden+tilojen+selitteet+koulutusmuodoittain#KOSKIopiskeluoikeudentilojenselitteetkoulutusmuodoittain-LukioIBkoulutus")
case class LukionOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[LukionOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class LukionOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso
