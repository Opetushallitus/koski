package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.{concat, finnish}
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

@Description("Lukion opiskeluoikeus")
case class LukionOpiskeluoikeus(
  id: Option[Int],
  versionumero: Option[Int],
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  alkamispäivä: Option[LocalDate],
  päättymispäivä: Option[LocalDate],
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  @Description("Onko tavoitteena lukion koko oppimäärän vai yksittäisen oppiaineen oppimäärän suoritus")
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("lukionoppimaara")
  @KoodistoKoodiarvo("lukionoppiaineenoppimaara")
  tavoite: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", "suorituksentyyppi"),
  @MinItems(1) @MaxItems(1)
  suoritukset: List[LukionPäätasonSuoritus],
  tila: Option[LukionOpiskeluoikeudenTila],
  läsnäolotiedot: Option[YleisetLäsnäolotiedot],
  @KoodistoKoodiarvo("lukiokoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukiokoulutus", "opiskeluoikeudentyyppi"),
  lisätiedot: Option[LukionOpiskeluoikeudenLisätiedot] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä: Option[LocalDate] = None
}

case class LukionOpiskeluoikeudenLisätiedot(
  @Description("Opiskeluajan pidennetty päättymispäivä. Lukion oppimäärä tulee suorittaa enintään neljässä vuodessa, jollei opiskelijalle perustellusta syystä myönnetä suoritusaikaan pidennystä (lukiolaki 21.8.1998/629 24 §)")
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Opiskelija on vaihto-opiskelija (ulkomainen vaihto-opiskelija Suomessa)")
  ulkomainenVaihtoopiskelija: Boolean = false,
  @Description("Syy alle 18-vuotiaana aloitettuun opiskeluun aikuisten lukiokoulutuksessa. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei opiskelija opiskele aikuisten lukiokoulutuksessa alle 18-vuotiaana")
  alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy: Option[LocalizedString] = None,
  @Description("Yksityisopiskelija (aikuisten lukiokoulutuksessa)")
  yksityisopiskelija: Boolean = false,
  @Description("Opiskelija opiskelee erityisen koulutustehtävän mukaisesti (ib, musiikki, urheilu, kielet, luonnontieteet, jne.). Kentän puuttuminen tai null-arvo tulkitaan siten, ettei opiskelija opiskele erityisen koulutustehtävän mukaisesti")
  erityinenkoulutustehtävä: Option[Erityinenkoulutustehtävä] = None,
  @Description("Tieto siitä, että oppilaalla on oikeus maksuttomaan asuntolapaikkaan, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilaalla ole oikeutta maksuttomaan asuntolapaikkaan.")
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Päätösjakso] = None,
  @Description("""Tieto siitä, että oppilas on sisäoppilaismaisessa majoituksessa, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole sisäoppilasmaisessa majoituksessa.""")
  sisäoppilaitosmainenMajoitus: Option[Päätösjakso] = None,
  @Description("Tieto siitä liittyykö opintoihin ulkomaanjaksoja")
  ulkomaanjakso: Option[List[Ulkomaanjakso]] = None
)

case class Erityinenkoulutustehtävä(
  @KoodistoUri("erityinenkoulutustehtava")
  @OksaUri("tmpOKSAID181", "erityinen koulutustehtävä")
  tehtävä: Koodistokoodiviite
)

trait LukionPäätasonSuoritus extends Suoritus with Toimipisteellinen

case class LukionOppimääränSuoritus(
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  koulutusmoduuli: LukionOppimäärä,
  @KoodistoUri("lukionoppimaara")
  oppimäärä: Koodistokoodiviite,
  @KoodistoKoodiarvo("lukionoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"),
  vahvistus: Option[Henkilövahvistus] = None,
  override val osasuoritukset: Option[List[LukionOppiaineenSuoritus]]
) extends LukionPäätasonSuoritus {
  def arviointi = None
}

case class LukionOppiaineenOppimääränSuoritus(
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  koulutusmoduuli: LukionOppiaine,
  @KoodistoKoodiarvo("lukionoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("lukionoppiaineenoppimaara", koodistoUri = "suorituksentyyppi"),
  vahvistus: Option[Henkilövahvistus] = None,
  @Description("Lukion oppiaineen oppimäärän arviointi")
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]]
) extends LukionPäätasonSuoritus

@Description("Lukion oppimäärän tunnistetiedot")
case class LukionOppimäärä(
 @KoodistoKoodiarvo("309902")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("309902", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String]
) extends Koulutus with EPerusteistaLöytyväKoulutusmoduuli {
  override def laajuus = None
  override def isTutkinto = true
}

case class LukionOppiaineenSuoritus(
  @KoodistoKoodiarvo("lukionoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionoppiaine", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukionOppiaine,
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionOppiaineenArviointi]] = None,
  override val osasuoritukset: Option[List[LukionKurssinSuoritus]]
) extends OppiaineenSuoritus

case class LukionKurssinSuoritus(
  @KoodistoKoodiarvo("lukionkurssi")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "lukionkurssi", koodistoUri = "suorituksentyyppi"),
  koulutusmoduuli: LukionKurssi,
  suorituskieli: Option[Koodistokoodiviite],
  tila: Koodistokoodiviite,
  arviointi: Option[List[LukionKurssinArviointi]] = None,
  @Description(
    """
      |Tieto siitä, onko kurssi saatu hyväksiluvun/tunnustetun (osaamisen tunnustamisen) kautta.
      |
      |Osaamisen tunnustamisella voidaan opiskelijalle lukea hyväksi ja korvata lukion oppimäärään kuuluvia pakollisia, syventäviä tai soveltavia opintoja.
      |Opiskelijan osaamisen tunnustamisessa noudatetaan, mitä 17 ja 17 a §:ssä säädetään opiskelijan arvioinnista ja siitä päättämisestä.
      |Mikäli opinnot tai muutoin hankittu osaaminen luetaan hyväksi opetussuunnitelman perusteiden mukaan numerolla arvioitavaan kurssiin, tulee kurssista antaa numeroarvosana.
    """.stripMargin)
  tunnustettu: Option[Tunnustaminen] = None
) extends Suoritus with LukioonValmistavanKoulutuksenOsasuoritus {
  def vahvistus: Option[Vahvistus] = None
}

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

case class LukionKurssinArviointi(
  arvosana: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: LocalDate
) extends YleissivistävänKoulutuksenArviointi with ArviointiPäivämäärällä

sealed trait LukionKurssi extends Koulutusmoduuli with Valinnaisuus {
  def pakollinen: Boolean = false
  def laajuus: Option[LaajuusKursseissa]
}

@Description("Valtakunnallisen lukion kurssin tunnistetiedot")
case class ValtakunnallinenLukionKurssi(
  @Description("Lukion kurssi")
  @KoodistoUri("lukionkurssit")
  @OksaUri("tmpOKSAID873", "kurssi")
  tunniste: Koodistokoodiviite,
  override val laajuus: Option[LaajuusKursseissa]
) extends LukionKurssi with KoodistostaLöytyväKoulutusmoduuli

@Description("Paikallisen lukion kurssin tunnistetiedot")
case class PaikallinenLukionKurssi(
  tunniste: PaikallinenKoodi,
  override val laajuus: Option[LaajuusKursseissa],
  kuvaus: LocalizedString
) extends LukionKurssi with PaikallinenKoulutusmoduuli

@Description("Lukion oppiaineen tunnistetiedot")
trait LukionOppiaine extends YleissivistavaOppiaine {
  def laajuus: Option[LaajuusKursseissa]
}

case class MuuOppiaine(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionOppiaine

@Description("Oppiaineena äidinkieli ja kirjallisuus")
case class AidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusKursseissa] = None
) extends LukionOppiaine

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
) extends LukionOppiaine {
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
) extends LukionOppiaine with KoodistostaLöytyväKoulutusmoduuli {
  override def description = oppimäärä.description
}

case class LaajuusKursseissa(
  arvo: Float,
  @KoodistoKoodiarvo("4")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "4", nimi = Some(finnish("kurssia")))
) extends Laajuus

case class LukionOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[LukionOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class LukionOpiskeluoikeusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("lukionopiskeluoikeudentila")
  tila: Koodistokoodiviite
) extends Opiskeluoikeusjakso