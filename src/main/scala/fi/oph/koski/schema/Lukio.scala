package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.{concat, finnish}
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

@Description("Lukion opiskeluoikeus")
case class LukionOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  alkamispäivä: Option[LocalDate] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
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
  @Description("Opiskelija opiskelee erityisen koulutustehtävän mukaisesti (ib, musiikki, urheilu, kielet, luonnontieteet, jne.). Kentän puuttuminen tai null-arvo tulkitaan siten, ettei opiskelija opiskele erityisen koulutustehtävän mukaisesti")
  erityisenKoulutustehtävänJaksot: Option[List[ErityisenKoulutustehtävänJakso]] = None,
  @Description("Opintoihin liittyvien ulkomaanjaksojen tiedot")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None
) extends OpiskeluoikeudenLisätiedot

case class ErityisenKoulutustehtävänJakso(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: LocalDate,
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @KoodistoUri("erityinenkoulutustehtava")
  @OksaUri("tmpOKSAID181", "erityinen koulutustehtävä")
  tehtävä: Koodistokoodiviite
) extends Jakso

trait LukionPäätasonSuoritus extends PäätasonSuoritus with Toimipisteellinen

case class LukionOppimääränSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: LukionOppimäärä,
  @KoodistoUri("lukionoppimaara")
  @Description("Tieto siitä, suoritetaanko lukiota nuorten vai aikuisten oppimäärän mukaisesti")
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[LukionOppimääränOsasuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi")
) extends LukionPäätasonSuoritus with Todistus with Arvioinniton

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
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("lukionoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends LukionPäätasonSuoritus with Todistus

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

@Title("Muu lukion-opinto")
@Description("Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot")
case class MuuLukioOpinto(
  @KoodistoUri("lukionmuutopinnot")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusKursseissa] = None
) extends KoodistostaLöytyväKoulutusmoduuli

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
) extends OppiaineenSuoritus with VahvistuksetonSuoritus with LukionOppimääränOsasuoritus with LukioonValmistavanKoulutuksenOsasuoritus

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

@Description("Valtakunnallisen lukion kurssin tunnistetiedot")
case class ValtakunnallinenLukionKurssi(
  @Description("Lukion kurssi")
  @KoodistoUri("lukionkurssit")
  @KoodistoUri("lukionkurssitops2004aikuiset")
  @KoodistoUri("lukionkurssitops2003nuoret")
  @OksaUri("tmpOKSAID873", "kurssi")
  @Title("Nimi")
  tunniste: Koodistokoodiviite,
  override val laajuus: Option[LaajuusKursseissa],
  @KoodistoKoodiarvo("pakollinen")
  @KoodistoKoodiarvo("syventava")
  kurssinTyyppi: Koodistokoodiviite
) extends LukionKurssi with KoodistostaLöytyväKoulutusmoduuli

@Description("Paikallisen lukion kurssin tunnistetiedot")
case class PaikallinenLukionKurssi(
  @Flatten
  tunniste: PaikallinenKoodi,
  override val laajuus: Option[LaajuusKursseissa],
  kuvaus: LocalizedString,
  @KoodistoKoodiarvo("syventava")
  @KoodistoKoodiarvo("soveltava")
  kurssinTyyppi: Koodistokoodiviite
) extends LukionKurssi with PaikallinenKoulutusmoduuli

@Description("Lukion oppiaineen tunnistetiedot")
trait LukionOppiaine extends Koulutusmoduuli with Valinnaisuus with PreIBOppiaine {
  def laajuus: Option[LaajuusKursseissa]
  @Title("Oppiaine")
  def tunniste: KoodiViite
}

case class PaikallinenLukionOppiaine(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  pakollinen: Boolean = true,
  laajuus: Option[LaajuusKursseissa] = None
) extends LukionOppiaine with PaikallinenKoulutusmoduuli

trait LukionValtakunnallinenOppiaine extends LukionOppiaine with YleissivistavaOppiaine

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
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionValtakunnallinenOppiaine

@Description("Oppiaineena äidinkieli ja kirjallisuus")
case class AidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
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
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionValtakunnallinenOppiaine {
  override def description = concat(nimi, ", ", kieli)
}

@Description("Oppiaineena matematiikka")
case class LukionMatematiikka(
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "MA", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Onko kyseessä laaja vai lyhyt oppimäärä")
  @KoodistoUri("oppiainematematiikka")
  oppimäärä: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionValtakunnallinenOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  override def description = oppimäärä.description
}

case class LaajuusKursseissa(
  arvo: Float,
  @KoodistoKoodiarvo("4")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "4", nimi = Some(finnish("kurssia")))
) extends Laajuus

case class LukionOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[LukionOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class LukionOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso