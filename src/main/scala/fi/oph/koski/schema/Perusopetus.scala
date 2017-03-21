package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.{concat, finnish}
import fi.oph.scalaschema.annotation.{MinItems, Description, Title}

@Description("Perusopetuksen opiskeluoikeus")
case class PerusopetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int]  = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  alkamispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: PerusopetuksenOpiskeluoikeudenTila,
  lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[PerusopetuksenPäätasonSuoritus],
  @KoodistoKoodiarvo("perusopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[PerusopetuksenPäätasonSuoritus]])
}

case class PerusopetuksenOpiskeluoikeudenLisätiedot(
  @Description("""Perusopetuksen aloittamisesta lykkäys (true/false). Oppilas saa luvan  aloittaa perusopetuksen myöhemmin.""")
  @OksaUri("tmpOKSAID242", "koulunkäynnin aloittamisen lykkääminen")
  perusopetuksenAloittamistaLykätty: Boolean = false,
  @Description("""Perusopetuksen aikastaminen (true/false). Oppilas aloittaa ennen oppivelvollisuusikää.""")
  aloittanutEnnenOppivelvollisuutta: Boolean = false,
  @Description("""Pidennetty oppivelvollisuus alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että oppilaalla ei ole pidennettyä oppivelvollisuutta.""")
  @OksaUri("tmpOKSAID517", "pidennetty oppivelvollisuus")
  pidennettyOppivelvollisuus: Option[Päätösjakso] = None,
  @KoodistoUri("perusopetuksentukimuoto")
  @Description("""Oppilaan saamat laissa säädetyt tukimuodot""")
  tukimuodot: Option[List[Koodistokoodiviite]] = None,
  @Description("""Erityisen tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty.""")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  erityisenTuenPäätös: Option[ErityisenTuenPäätös] = None,
  @Description("""Tehostetun tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty.""")
  @OksaUri("tmpOKSAID511", "tehostettu tuki")
  tehostetunTuenPäätös: Option[Päätösjakso] = None,
  @Description("""Opiskelu joustavassa perusopetuksessa (JOPO) alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole joustavassa perusopetuksessa.""")
  @OksaUri("tmpOKSAID453", "joustava perusopetus")
  joustavaPerusopetus: Option[Päätösjakso] = None,
  @Description("""Opiskelu kotiopetuksessa huoltajan päätöksestä, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole kotiopetuksessa.""")
  kotiopetus: Option[Päätösjakso] = None,
  @Description("""Opiskelu ulkomailla huoltajan ilmoituksesta, alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole ulkomailla.""")
  ulkomailla: Option[Päätösjakso] = None,
  @Description("""Oppilas on vuosiluokkiin sitoutumattomassa opetuksessa (true/false)""")
  vuosiluokkiinSitoutumatonOpetus: Boolean = false
) extends OpiskeluoikeudenLisätiedot

case class ErityisenTuenPäätös(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @Description("""Oppilas opiskelee toiminta-alueittain (true/false).
                 | Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain.
                 | Tuolloin oppilaalla on aina erityisen tuen päätös.
                 | Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.
                 | huomautuksena: toiminta-alue arviointeineen on kuvattu oppiaineen suorituksessa.""")
  @Title("Opiskelee toiminta-alueittain")
  opiskeleeToimintaAlueittain: Boolean = false,
  @Description("""Suorittaako erityisoppilas koulutusta omassa erityisryhmässään vai inklusiivisesti opetuksen mukana""")
  @OksaUri("tmpOKSAID444", "opetusryhmä")
  @Title("Opiskelee erityisryhmässä")
  erityisryhmässä: Boolean
)


trait PerusopetuksenPäätasonSuoritus extends PäätasonSuoritus with Toimipisteellinen

@Description("Perusopetuksen vuosiluokan suoritus. Nämä suoritukset näkyvät lukuvuositodistuksella.")
case class PerusopetuksenVuosiluokanSuoritus(
  @Description("Luokka-aste ilmaistaan perusopetuksenluokkaaste-koodistolla")
  @Title("Luokka-aste")
  koulutusmoduuli: PerusopetuksenLuokkaAste,
  @Description("Luokan tunniste, esimerkiksi 9C")
  luokka: String,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate],
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Tieto siitä kielestä, joka on oppilaan kotimaisten kielten kielikylvyn kieli.")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID439", "kielikylpy")
  kielikylpykieli: Option[Koodistokoodiviite] = None,
  jääLuokalle: Boolean = false,
  käyttäytymisenArvio: Option[PerusopetuksenKäyttäytymisenArviointi] = None,
  @Description("Vuosiluokan suoritukseen liittyvät oppiaineen suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[OppiaineenTaiToiminta_AlueenSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenvuosiluokka")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenvuosiluokka", koodistoUri = "suorituksentyyppi"),
  liitetiedot: Option[List[PerusopetuksenVuosiluokanSuorituksenLiitteet]] = None
) extends PerusopetuksenPäätasonSuoritus with Todistus with Arvioinniton

@Description("Perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella.")
case class PerusopetuksenOppimääränSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: Perusopetus,
  @KoodistoUri("perusopetuksenoppimaara")
  @Description("Tieto siitä, suoritetaanko perusopetusta nuorten vai aikuisten oppimäärän mukaisesti")
  oppimäärä: Koodistokoodiviite,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  @KoodistoUri("perusopetuksensuoritustapa")
  @Description("Tieto siitä, suoritetaanko perusopetusta normaalina koulutuksena vai erityisenä tutkintona")
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[OppiaineenTaiToiminta_AlueenSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppimaara", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenPäätasonSuoritus with Todistus with Arvioinniton

@Description("Vuosiluokan todistuksen liitteet")
case class PerusopetuksenVuosiluokanSuorituksenLiitteet(
  @Description("Liitetiedon tyyppi kooditettuna")
  @KoodistoUri("perusopetuksentodistuksenliitetieto")
  @KoodistoKoodiarvo("kayttaytyminen")
  @KoodistoKoodiarvo("tyoskentely")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus")
  kuvaus: LocalizedString
)

@Description("Perusopetuksen yksittäisen oppiaineen oppimäärän suoritus erillisenä kokonaisuutena")
case class PerusopetuksenOppiaineenOppimääränSuoritus(
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  @Title("Oppiaine")
  @Flatten
  koulutusmoduuli: PerusopetuksenOppiaine,
  yksilöllistettyOppimäärä: Boolean = false,
  @Description("Tieto siitä, onko oppiaineen opetus painotettu (true/false)")
  painotettuOpetus: Boolean = false,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  @Title("Arvosana")
  @Flatten
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  override val vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  @KoodistoUri("perusopetuksensuoritustapa")
  @Description("Tieto siitä, suoritetaanko perusopetusta normaalina koulutuksena vai erityisenä tutkintona")
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Option[Koodistokoodiviite] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppiaineenoppimaara", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenPäätasonSuoritus with OppiaineenSuoritus with Todistus with Yksilöllistettävä

sealed trait OppiaineenTaiToiminta_AlueenSuoritus extends Suoritus

@Description("Perusopetuksen oppiaineen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta")
case class PerusopetuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PerusopetuksenOppiaine,
  yksilöllistettyOppimäärä: Boolean = false,
  @Description("Tieto siitä, onko oppiaineen opetus painotettu (true/false)")
  painotettuOpetus: Boolean = false,
  tila: Koodistokoodiviite,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("perusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with OppiaineenTaiToiminta_AlueenSuoritus with VahvistuksetonSuoritus with Yksilöllistettävä

@Description("Perusopetuksen toiminta-alueen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta. Suoritukset voidaan kirjata oppiaineiden sijaan toiminta-alueittain, jos opiskelijalle on tehty erityisen tuen päätös.")
case class PerusopetuksenToiminta_AlueenSuoritus(
  @Title("Toiminta-alue")
  koulutusmoduuli: PerusopetuksenToiminta_Alue,
  tila: Koodistokoodiviite,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetuksentoimintaalue")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksentoimintaalue", koodistoUri = "suorituksentyyppi")
) extends OppiaineenTaiToiminta_AlueenSuoritus with VahvistuksetonSuoritus

trait PerusopetuksenOppiaineenArviointi extends YleissivistävänKoulutuksenArviointi

@Description("Numeerinen arviointi asteikolla 4 (hylätty) - 10 (erinomainen)")
case class NumeerinenPerusopetuksenOppiaineenArviointi(
  arvosana: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate]
) extends PerusopetuksenOppiaineenArviointi with NumeerinenYleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

@Description("Sanallisessa arvioinnissa suorituksen hyväksymisen ilmaisuun käytetään koodiarvoja S (suoritettu) ja H (hylätty). Koodiarvon lisäksi voidaan liittää sanallinen arviointi vapaana tekstinä kuvaus-kenttään.")
case class SanallinenPerusopetuksenOppiaineenArviointi(
  @KoodistoKoodiarvo("H")
  @KoodistoKoodiarvo("S")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString],
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate] = None
) extends PerusopetuksenOppiaineenArviointi with SanallinenYleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

@Description("Käyttäytymisen arviointi. Koodiarvon lisäksi voidaan liittää sanallinen arviointi vapaana tekstinä kuvaus-kenttään.")
case class PerusopetuksenKäyttäytymisenArviointi(
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString] = None,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate] = None
) extends PerusopetuksenOppiaineenArviointi with SanallinenArviointi with IgnoreInAnyOfDeserialization {
  def arviointipäivä = päivä
}

object PerusopetuksenOppiaineenArviointi {
  def apply(arvosana: String, kuvaus: Option[LocalizedString] = None) = new SanallinenPerusopetuksenOppiaineenArviointi(
    arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"),
    päivä = None,
    kuvaus = kuvaus
  )
  def apply(arvosana: Int) = new NumeerinenPerusopetuksenOppiaineenArviointi(
    arvosana = Koodistokoodiviite(koodiarvo = arvosana.toString, koodistoUri = "arviointiasteikkoyleissivistava"),
    päivä = None
  )
}

@Description("Perusopetuksen toiminta-alueen tunnistetiedot")
case class PerusopetuksenToiminta_Alue(
  @KoodistoUri("perusopetuksentoimintaalue")
  tunniste: Koodistokoodiviite
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
}

@Description("Perusopetuksen tunnistetiedot")
case class Perusopetus(
 perusteenDiaarinumero: Option[String],
 @KoodistoKoodiarvo("201101")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus")
) extends Koulutus with DiaarinumerollinenKoulutus {
  override def laajuus = None
  override def isTutkinto = true
}

@Description("Perusopetuksen luokka-asteen (1-9) tunnistetiedot")
case class PerusopetuksenLuokkaAste(
 @Description("Luokka-asteen tunniste (1-9)")
 @KoodistoUri("perusopetuksenluokkaaste")
 tunniste: Koodistokoodiviite,
 perusteenDiaarinumero: Option[String]
) extends KoodistostaLöytyväKoulutusmoduuli with DiaarinumerollinenKoulutus {
  override def laajuus = None
  override def isTutkinto = false
}

object PerusopetuksenLuokkaAste {
  def apply(luokkaAste: Int): PerusopetuksenLuokkaAste = PerusopetuksenLuokkaAste(Koodistokoodiviite(luokkaAste.toString, "perusopetuksenluokkaaste"), None)
}

@Description("Perusopetuksen oppiaineen tunnistetiedot")
trait PerusopetuksenOppiaine extends Koulutusmoduuli with Valinnaisuus {
  @Title("Oppiaine")
  def tunniste: KoodiViite
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

trait PerusopetuksenKoodistostaLöytyväOppiaine extends PerusopetuksenOppiaine with YleissivistavaOppiaine

case class PerusopetuksenPaikallinenValinnainenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None,
  kuvaus: LocalizedString
) extends PerusopetuksenOppiaine with PaikallinenKoulutusmoduuli {
  def pakollinen: Boolean = false
}

case class MuuPeruskoulunOppiaine(
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
  @KoodistoKoodiarvo("MA")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenKoodistostaLöytyväOppiaine

@Description("Oppiaineena äidinkieli ja kirjallisuus")
case class PeruskoulunAidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenKoodistostaLöytyväOppiaine


@Description("Oppiaineena vieras tai toinen kotimainen kieli")
case class PeruskoulunVierasTaiToinenKotimainenKieli(
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
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenKoodistostaLöytyväOppiaine {
  override def description = concat(nimi, ", ", kieli)
}

case class LaajuusVuosiviikkotunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("3")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "3", nimi = Some(finnish("Vuosiviikkotuntia")), koodistoUri = "opintojenlaajuusyksikko")
) extends Laajuus

case class PerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[PerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class PerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso