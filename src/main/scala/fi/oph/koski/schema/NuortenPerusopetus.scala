package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString.{concat, finnish, unlocalized}
import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.scalaschema.annotation._

@Description("Perusopetuksen opiskeluoikeus")
case class PerusopetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int]  = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Description("Oppijan oppimäärän alkamispäivä")
  alkamispäivä: Option[LocalDate] = None,
  @Description("Oppijan oppimäärän päättymispäivä")
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
  @DefaultValue(false)
  perusopetuksenAloittamistaLykätty: Boolean = false,
  @Description("""Perusopetuksen aikastaminen (true/false). Oppilas aloittaa ennen oppivelvollisuusikää.""")
  @DefaultValue(false)
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
  @DefaultValue(false)
  vuosiluokkiinSitoutumatonOpetus: Boolean = false,
  @Description("""Oppilas on vaikeasti vammainen (true/false)""")
  @DefaultValue(false)
  vaikeastiVammainen: Boolean = false,
  @Description("""Oppilaalla on majoitusetu""")
  majoitusetu: Option[Päätösjakso] = None,
  @Description("""Oppilaalla on kuljetusetu""")
  kuljetusetu: Option[Päätösjakso] = None,
  @Description("""Oppilaalla on oikeus maksuttomaan asuntolapaikkaan""")
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Päätösjakso] = None


) extends OpiskeluoikeudenLisätiedot

case class ErityisenTuenPäätös(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @Description("""Oppilas opiskelee toiminta-alueittain (true/false).
Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain.
Tuolloin oppilaalla on aina erityisen tuen päätös.
Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.
huomautuksena: toiminta-alue arviointeineen on kuvattu oppiaineen suorituksessa.""")
  @Title("Opiskelee toiminta-alueittain")
  opiskeleeToimintaAlueittain: Boolean = false,
  @Description("""Suorittaako erityisoppilas koulutusta omassa erityisryhmässään vai inklusiivisesti opetuksen mukana""")
  @OksaUri("tmpOKSAID444", "opetusryhmä")
  @Title("Opiskelee erityisryhmässä")
  erityisryhmässä: Boolean
)

trait PerusopetuksenPäätasonSuoritus extends PäätasonSuoritus with Toimipisteellinen with MonikielinenSuoritus with Suorituskielellinen

@Description("Perusopetuksen vuosiluokan suoritus. Nämä suoritukset näkyvät lukuvuositodistuksella.")
case class PerusopetuksenVuosiluokanSuoritus(
  @Description("Luokka-aste ilmaistaan perusopetuksenluokkaaste-koodistolla")
  @Title("Luokka-aste")
  koulutusmoduuli: PerusopetuksenLuokkaAste,
  @Description("Luokan tunniste, esimerkiksi 9C")
  luokka: String,
  toimipiste: OrganisaatioWithOid,
  @Description("Vuosiluokan alkamispäivä")
  override val alkamispäivä: Option[LocalDate] = None,
  tila: Koodistokoodiviite,
  @Description("Varsinaisen todistuksen saantipäivämäärä.")
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä kielestä, joka on oppilaan kotimaisten kielten kielikylvyn kieli.")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID439", "kielikylpy")
  kielikylpykieli: Option[Koodistokoodiviite] = None,
  @Description("Tieto siitä, että oppilas jää luokalle.")
  @Title("Oppilas jää luokalle")
  jääLuokalle: Boolean = false,
  käyttäytymisenArvio: Option[PerusopetuksenKäyttäytymisenArviointi] = None,
  @Description("Vuosiluokan suoritukseen liittyvät oppiaineen suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[OppiaineenTaiToiminta_AlueenSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenvuosiluokka")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenvuosiluokka", koodistoUri = "suorituksentyyppi"),
  liitetiedot: Option[List[PerusopetuksenVuosiluokanSuorituksenLiite]] = None
) extends PerusopetuksenPäätasonSuoritus with Todistus with Arvioinniton

trait PerusopetuksenOppimääränSuoritus extends Suoritus with Todistus with Arvioinniton with SuoritustavallinenPerusopetuksenSuoritus {
  @Title("Koulutus")
  override def koulutusmoduuli: Perusopetus
  def suorituskieli: Koodistokoodiviite
  def muutSuorituskielet: Option[List[Koodistokoodiviite]]
  @Description("Päättötodistukseen liittyvät oppiaineen suoritukset")
  @Title("Oppiaineet")
  override def osasuoritukset: Option[List[Suoritus]] = None
  def todistuksellaNäkyvätLisätiedot: Option[LocalizedString]
  def oppiaineet = osasuoritukset.toList.flatten
}

trait SuoritustavallinenPerusopetuksenSuoritus extends Suoritus {
  @KoodistoUri("perusopetuksensuoritustapa")
  @Description("Tieto siitä, suoritetaanko perusopetusta normaalina koulutuksena vai erityisenä tutkintona")
  def suoritustapa: Koodistokoodiviite
}

@Description("Perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella.")
case class NuortenPerusopetuksenOppimääränSuoritus(
  koulutusmoduuli: NuortenPerusopetus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  override val osasuoritukset: Option[List[OppiaineenTaiToiminta_AlueenSuoritus]] = None,
  @Description("Vapaamuotoinen tekstikenttä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppimaara", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenPäätasonSuoritus with PerusopetuksenOppimääränSuoritus

@Description("Vuosiluokan todistuksen liitetieto")
case class PerusopetuksenVuosiluokanSuorituksenLiite(
  @Description("Liitetiedon tyyppi kooditettuna")
  @KoodistoUri("perusopetuksentodistuksenliitetieto")
  @KoodistoKoodiarvo("kayttaytyminen")
  @KoodistoKoodiarvo("tyoskentely")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus")
  kuvaus: LocalizedString
)

sealed trait OppiaineenTaiToiminta_AlueenSuoritus extends Suoritus with MahdollisestiSuorituskielellinen

@Description("Perusopetuksen oppiaineen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta")
case class PerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetuksenOppiaine,
  @Description("Jos oppilas opiskelee yhdessä yksilöllistetyn oppimäärän mukaan, myös päättöarviointi voi näissä aineissa olla sanallinen.")
  yksilöllistettyOppimäärä: Boolean = false,
  @Description("Tieto siitä, onko oppiaineen opetus painotettu (true/false). Painotettu opetus (oppiaine tai oppiainekokonaisuus, kaksikielinen opetus) tavoitteet ja arviointiperusteet ovat valtakunnallisen opetussuunnitelman perusteiden mukaiset.")
  painotettuOpetus: Boolean = false,
  tila: Koodistokoodiviite,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends OppiaineenSuoritus with OppiaineenTaiToiminta_AlueenSuoritus with VahvistuksetonSuoritus with Yksilöllistettävä with MahdollisestiSuorituskielellinen

@Description("Perusopetuksen toiminta-alueen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta. Suoritukset voidaan kirjata oppiaineiden sijaan toiminta-alueittain, jos opiskelijalle on tehty erityisen tuen päätös.")
case class PerusopetuksenToiminta_AlueenSuoritus(
  @Description("Toiminta-alueet voivat sisältää yksittäisen oppiaineen tavoitteita ja sisältöjä, jos oppilaalla on vahvuuksia jossakin yksittäisessä oppiaineessa. Opetuksen toteuttamisessa eri toiminta-alueiden sisältöjä voidaan yhdistää. Toiminta-alueesta muodostuu oppiaineen kaltaisia suorituksia.")
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
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString],
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate] = None
) extends PerusopetuksenOppiaineenArviointi with SanallinenYleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

@Description("Käyttäytymisen arviointi. Koodiarvon lisäksi voidaan liittää sanallinen arviointi vapaana tekstinä kuvaus-kenttään.")
@IgnoreInAnyOfDeserialization
case class PerusopetuksenKäyttäytymisenArviointi(
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  kuvaus: Option[LocalizedString] = None,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  @Hidden
  päivä: Option[LocalDate] = None
) extends YleissivistävänKoulutuksenArviointi with SanallinenArviointi {
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

@Description("Nuorten perusopetuksen tunnistetiedot")
case class NuortenPerusopetus(
 perusteenDiaarinumero: Option[String],
 @KoodistoKoodiarvo("201101")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus")
) extends Perusopetus

trait Perusopetus extends Koulutus with Laajuudeton with Tutkinto with PerusopetuksenDiaarinumerollinenKoulutus

@Title("Perusopetuksen luokka-aste")
@Description("Perusopetuksen luokka-asteen (1-9) tunnistetiedot")
case class PerusopetuksenLuokkaAste(
 @Description("Luokka-asteen tunniste (1-9)")
 @KoodistoUri("perusopetuksenluokkaaste")
 @Title("Luokka-aste")
 tunniste: Koodistokoodiviite,
 perusteenDiaarinumero: Option[String]
) extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton with PerusopetuksenDiaarinumerollinenKoulutus {
  override def laajuus = None
  def luokkaAste = tunniste.koodiarvo
}

trait PerusopetuksenDiaarinumerollinenKoulutus extends DiaarinumerollinenKoulutus

object PerusopetuksenLuokkaAste {
  def apply(luokkaAste: Int, diaarinumero: String): PerusopetuksenLuokkaAste = PerusopetuksenLuokkaAste(Koodistokoodiviite(luokkaAste.toString, "perusopetuksenluokkaaste"), Some(diaarinumero))
}

@Description("Perusopetuksen oppiaineen tunnistetiedot")
trait PerusopetuksenOppiaine extends Koulutusmoduuli with Valinnaisuus with Diaarinumerollinen {
  @Title("Oppiaine")
  def tunniste: KoodiViite
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

trait PerusopetuksenKoodistostaLöytyväOppiaine extends PerusopetuksenOppiaine with YleissivistavaOppiaine

@Title("Paikallinen valinnainen oppiaine")
case class PerusopetuksenPaikallinenValinnainenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None,
  kuvaus: LocalizedString,
  perusteenDiaarinumero: Option[String] = None
) extends PerusopetuksenOppiaine with PaikallinenKoulutusmoduuli {
  def pakollinen: Boolean = false
}

@Title("Muu oppiaine")
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
  @KoodistoKoodiarvo("YL")
  @KoodistoKoodiarvo("OP")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenKoodistostaLöytyväOppiaine

@Title("Äidinkieli ja kirjallisuus")
@Description("Oppiaineena äidinkieli ja kirjallisuus")
case class PeruskoulunÄidinkieliJaKirjallisuus(
  @KoodistoKoodiarvo("AI")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenKoodistostaLöytyväOppiaine with Äidinkieli


@Title("Vieras tai toinen kotimainen kieli")
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
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends PerusopetuksenKoodistostaLöytyväOppiaine with Kieliaine {
  override def description(texts: LocalizationRepository) = concat(nimi, unlocalized(", "), kieli.description)
}

case class LaajuusVuosiviikkotunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("3")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "3", nimi = Some(finnish("Vuosiviikkotuntia")), koodistoUri = "opintojenlaajuusyksikko")
) extends Laajuus

@Description("Ks. tarkemmin perusopetuksen opiskeluoikeuden tilat: [confluence](https://confluence.csc.fi/display/OPHPALV/KOSKI+opiskeluoikeuden+tilojen+selitteet+koulutusmuodoittain#KOSKIopiskeluoikeudentilojenselitteetkoulutusmuodoittain-Perusopetus)")
case class PerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[PerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class PerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso

case class PakollisetOppiaineet(koodistoViitePalvelu: KoodistoViitePalvelu) {
  lazy val toimintaAlueidenSuoritukset = {
    (1 to 5).map(n => PerusopetuksenToiminta_Alue(koodi("perusopetuksentoimintaalue", n.toString))).map(ta => PerusopetuksenToiminta_AlueenSuoritus(ta, tila = kesken)).toList
  }
  private def koodi(koodisto: String, arvo: String) = koodistoViitePalvelu.validateRequired(koodisto, arvo)
  private def kesken = koodi("suorituksentila", "KESKEN")
  private def aine(koodiarvo: String) = koodi("koskioppiaineetyleissivistava", koodiarvo)
  private def nuortenSuoritus(aine: PerusopetuksenOppiaine) = PerusopetuksenOppiaineenSuoritus(koulutusmoduuli = aine, tila = kesken)
  private def aikuistenSuoritus(aine: PerusopetuksenOppiaine) = AikuistenPerusopetuksenOppiaineenSuoritus(koulutusmoduuli = aine, tila = kesken)
  def päättötodistuksenSuoritukset(suorituksenTyyppi: String, toimintaAlueittain: Boolean) = {
    suorituksenTyyppi match {
      case "perusopetuksenoppimaara" =>
    if (toimintaAlueittain) {
      toimintaAlueidenSuoritukset
    } else {
          päättötodistuksenOppiaineet.map(nuortenSuoritus)
        }
      case "aikuistenperusopetuksenoppimaara" =>
        päättötodistuksenOppiaineet.map(aikuistenSuoritus)
    }
  }
  def pakollistenOppiaineidenTaiToimintaAlueidenSuoritukset(luokkaAste: Int, toimintaAlueittain: Boolean) = {
    if (toimintaAlueittain) {
      toimintaAlueidenSuoritukset
    } else if (luokkaAste >= 1 && luokkaAste <= 2) {
      luokkaAsteiden1_2Oppiaineet.map(nuortenSuoritus)
    } else if (luokkaAste >= 3 && luokkaAste <= 6) {
      luokkaAsteiden3_6Oppiaineet.map(nuortenSuoritus)
    } else if (luokkaAste <= 9) {
      päättötodistuksenSuoritukset("perusopetuksenoppimaara", toimintaAlueittain)
    } else {
      throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Tuntematon luokka-aste: " + luokkaAste))
    }
  }

  private lazy val luokkaAsteiden3_6Oppiaineet = List(
        PeruskoulunÄidinkieliJaKirjallisuus(tunniste = aine("AI"), kieli = koodi("oppiaineaidinkielijakirjallisuus", "AI1")),
        PeruskoulunVierasTaiToinenKotimainenKieli(tunniste = aine("A1"), kieli = koodi("kielivalikoima", "EN")),
        MuuPeruskoulunOppiaine(aine("MA")),
    MuuPeruskoulunOppiaine(aine("YL")),
        MuuPeruskoulunOppiaine(aine("KT")),
        MuuPeruskoulunOppiaine(aine("HI")),
        MuuPeruskoulunOppiaine(aine("YH")),
        MuuPeruskoulunOppiaine(aine("MU")),
        MuuPeruskoulunOppiaine(aine("KU")),
        MuuPeruskoulunOppiaine(aine("KS")),
        MuuPeruskoulunOppiaine(aine("LI")),
        MuuPeruskoulunOppiaine(aine("OP"))
  )

  private lazy val päättötodistuksenOppiaineet = List(
        PeruskoulunÄidinkieliJaKirjallisuus(tunniste = aine("AI"), kieli = koodi("oppiaineaidinkielijakirjallisuus", "AI1")),
    PeruskoulunVierasTaiToinenKotimainenKieli(tunniste = aine("A1"), kieli = koodi("kielivalikoima", "EN")),
    PeruskoulunVierasTaiToinenKotimainenKieli(tunniste = aine("B1"), kieli = koodi("kielivalikoima", "SV")),
        MuuPeruskoulunOppiaine(aine("MA")),
    MuuPeruskoulunOppiaine(aine("BI")),
    MuuPeruskoulunOppiaine(aine("GE")),
    MuuPeruskoulunOppiaine(aine("FY")),
    MuuPeruskoulunOppiaine(aine("KE")),
    MuuPeruskoulunOppiaine(aine("TE")),
        MuuPeruskoulunOppiaine(aine("KT")),
    MuuPeruskoulunOppiaine(aine("HI")),
    MuuPeruskoulunOppiaine(aine("YH")),
        MuuPeruskoulunOppiaine(aine("MU")),
        MuuPeruskoulunOppiaine(aine("KU")),
        MuuPeruskoulunOppiaine(aine("KS")),
        MuuPeruskoulunOppiaine(aine("LI")),
    MuuPeruskoulunOppiaine(aine("KO")),
        MuuPeruskoulunOppiaine(aine("OP"))
  )

  private lazy val luokkaAsteiden1_2Oppiaineet = List(
        PeruskoulunÄidinkieliJaKirjallisuus(tunniste = aine("AI"), kieli = koodi("oppiaineaidinkielijakirjallisuus", "AI1")),
        MuuPeruskoulunOppiaine(aine("MA")),
        MuuPeruskoulunOppiaine(aine("YL")),
        MuuPeruskoulunOppiaine(aine("KT")),
        MuuPeruskoulunOppiaine(aine("MU")),
        MuuPeruskoulunOppiaine(aine("KU")),
        MuuPeruskoulunOppiaine(aine("KS")),
        MuuPeruskoulunOppiaine(aine("LI")),
        MuuPeruskoulunOppiaine(aine("OP"))
  )
    }
