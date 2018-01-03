package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString.{concat, finnish, unlocalized}
import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.koski.schema.annotation._
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.scalaschema.annotation._

@Description("Perusopetuksen opiskeluoikeus")
case class PerusopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int]  = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Hidden
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  @Description("Oppijan oppimäärän päättymispäivä")
  päättymispäivä: Option[LocalDate] = None,
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila,
  lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[PerusopetuksenPäätasonSuoritus],
  @KoodistoKoodiarvo("perusopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
}

case class PerusopetuksenOpiskeluoikeudenLisätiedot(
  @Description("Perusopetuksen aloittamisesta lykkäys (true/false). Oppilas saanut luvan aloittaa perusopetuksen myöhemmin.")
  @Tooltip("Perusopetuksen aloittamisesta lykkäys. Oppilas saanut luvan aloittaa perusopetuksen myöhemmin.")
  @SensitiveData
  @OksaUri("tmpOKSAID242", "koulunkäynnin aloittamisen lykkääminen")
  @DefaultValue(false)
  perusopetuksenAloittamistaLykätty: Boolean = false,
  @Description("Perusopetuksen aloituksen aikaistaminen (true/false). Oppilas aloittanut perusopetuksen ennen oppivelvollisuusikää.")
  @Tooltip("Perusopetuksen aloitusta aikaistettu, eli oppilas aloittanut peruskoulun ennen oppivelvollisuusikää.")
  @DefaultValue(false)
  aloittanutEnnenOppivelvollisuutta: Boolean = false,
  @Description("Pidennetty oppivelvollisuus alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että oppilaalla ei ole pidennettyä oppivelvollisuutta. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen pidennetyn oppivelvollisuuden alkamis- ja päättymispäivät. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  @OksaUri("tmpOKSAID517", "pidennetty oppivelvollisuus")
  pidennettyOppivelvollisuus: Option[Päätösjakso] = None,
  @KoodistoUri("perusopetuksentukimuoto")
  @Description("Oppilaan saamat laissa säädetyt tukimuodot.")
  @Tooltip("Oppilaan saamat laissa säädetyt tukimuodot. Voi olla useita.")
  @SensitiveData
  tukimuodot: Option[List[Koodistokoodiviite]] = None,
  @Description("Erityisen tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen erityisen tuen päätöksen alkamis- ja päättymispäivät. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  erityisenTuenPäätös: Option[ErityisenTuenPäätös] = None,
  @Description("Tehostetun tuen päätös alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että päätöstä ei ole tehty. Rahoituksen laskennassa käytettävä tieto.")
  @Description("Mahdollisen tehostetun tuen päätös päätöksen alkamis- ja päättymispäivät. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  @OksaUri("tmpOKSAID511", "tehostettu tuki")
  tehostetunTuenPäätös: Option[Päätösjakso] = None,
  @Description("Opiskelu joustavassa perusopetuksessa (JOPO) alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole joustavassa perusopetuksessa. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen joustavan perusopetuksen (JOPO) alkamis- ja päättymispäivät. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  @OksaUri("tmpOKSAID453", "joustava perusopetus")
  joustavaPerusopetus: Option[Päätösjakso] = None,
  @Description("Tieto opiskelusta kotiopetuksessa huoltajan päätöksestä alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole kotiopetuksessa. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto mahdollisesta opiskelusta kotiopetuksessa huoltajan päätöksestä alkamis- ja päättymispäivineen. Rahoituksen laskennassa käytettävä tieto.")
  kotiopetus: Option[Päätösjakso] = None,
  @Description("Tieto opiskelusta ulkomailla huoltajan ilmoituksesta alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, ettei oppilas ole ulkomailla. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto opiskelusta ulkomailla huoltajan ilmoituksesta alkamis- ja päättymispäivineen. Rahoituksen laskennassa käytettävä tieto.")
  ulkomailla: Option[Päätösjakso] = None,
  @Description("Oppilas on vuosiluokkiin sitomattomassa opetuksessa (kyllä/ei).")
  @Tooltip("Onko oppilas vuosiluokkiin sitomattomassa opetuksessa.")
  @SensitiveData
  @DefaultValue(false)
  @Title("Vuosiluokkiin sitomaton opetus")
  vuosiluokkiinSitoutumatonOpetus: Boolean = false,
  @Description("Onko oppija vammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija vammainen (alku- ja loppupäivämäärät). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  vammainen: Option[List[Aikajakso]] = None,
  @Description("Onko oppija vaikeasti vammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija vaikeasti vammainen (alku- ja loppupäivämäärät). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  vaikeastiVammainen: Option[List[Aikajakso]] = None,
  @Description("Oppilaalla on majoitusetu (alku- ja loppupäivämäärä). Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, jos oppilaalla on majoitusetu (alku- ja loppupäivämäärät). Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  majoitusetu: Option[Päätösjakso] = None,
  @Description("Oppilaalla on kuljetusetu (alku- ja loppupäivämäärät).")
  @Tooltip("Tieto siitä, jos oppilaalla on kuljetusetu (alku- ja loppupäivämäärät).")
  @SensitiveData
  kuljetusetu: Option[Päätösjakso] = None,
  @Description("Oppilaalla on oikeus maksuttomaan asuntolapaikkaan (alku- ja loppupäivämäärät).")
  @Tooltip("Tieto siitä, jos oppilaalla on oikeus maksuttomaan asuntolapaikkaan (alku- ja loppupäivämäärät).")
  @SensitiveData
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Päätösjakso] = None,
  @Description("Sisäoppilaitosmuotoinen majoitus, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen sisäoppilaitosmuotoisen majoituksen aloituspäivä ja loppupäivä. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
  @Description("Oppija on koulukotikorotuksen piirissä, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, jos oppija on koulukotikorotuksen piirissä (aloituspäivä ja loppupäivä). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  koulukoti: Option[List[Aikajakso]] = None
) extends OpiskeluoikeudenLisätiedot

@Description("Oppivelvollisen erityisen tuen päätöstiedot")
case class ErityisenTuenPäätös(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @Description("""Oppilas opiskelee toiminta-alueittain (true/false).
Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain.
Tuolloin oppilaalla on aina erityisen tuen päätös.
Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.
Huom: toiminta-alue arviointeineen on kuvattu oppiaineen suorituksessa.""")
  @Tooltip("Opiskeleeko oppilas toiminta-alueittain? Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain. Tuolloin oppilaalla on aina erityisen tuen päätös. Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.")
  @Title("Opiskelee toiminta-alueittain")
  opiskeleeToimintaAlueittain: Boolean = false,
  @Description("Suorittaako erityisoppilas koulutusta omassa erityisryhmässään vai inklusiivisesti opetuksen mukana.")
  @Tooltip("Suorittaako erityisoppilas koulutusta omassa erityisryhmässään vai inklusiivisesti opetuksen mukana.")
  @OksaUri("tmpOKSAID444", "opetusryhmä")
  @Title("Opiskelee erityisryhmässä")
  erityisryhmässä: Boolean
)

trait PerusopetuksenPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with MonikielinenSuoritus with Suorituskielellinen

@Description("Perusopetuksen vuosiluokan suoritus. Nämä suoritukset näkyvät lukuvuositodistuksella")
case class PerusopetuksenVuosiluokanSuoritus(
  @Description("Luokka-aste ilmaistaan perusopetuksenluokkaaste-koodistolla.")
  @Tooltip("Vuosiluokkasuorituksen luokka-aste ja vuosiluokkasuorituksen opetussuunnitelman perusteen diaarinumero.")
  @Title("Luokka-aste")
  koulutusmoduuli: PerusopetuksenLuokkaAste,
  @Description("Luokan tunniste, esimerkiksi 9C.")
  @Tooltip("Luokan tunniste, esimerkiksi 9C.")
  luokka: String,
  toimipiste: OrganisaatioWithOid,
  @Description("Vuosiluokan alkamispäivä")
  @Tooltip("Vuosiluokan alkamispäivä")
  override val alkamispäivä: Option[LocalDate] = None,
  @Description("Varsinaisen todistuksen saantipäivämäärä")
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Description("Oppilaan kotimaisten kielten kielikylvyn kieli.")
  @Tooltip("Oppilaan kotimaisten kielten kielikylvyn kieli.")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID439", "kielikylpy")
  kielikylpykieli: Option[Koodistokoodiviite] = None,
  @Description("Tieto siitä, että oppilas jää luokalle")
  @SensitiveData
  @DefaultValue(false)
  @Title("Oppilas jää luokalle")
  jääLuokalle: Boolean = false,
  käyttäytymisenArvio: Option[PerusopetuksenKäyttäytymisenArviointi] = None,
  @Description("Vuosiluokan suoritukseen liittyvät oppiaineen suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[OppiaineenTaiToiminta_AlueenSuoritus]] = None,
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi, jos oppilaan oppitunneista lukuvuoden aikana vähintään 25 % on opetettu muulla kuin koulun opetuskielellä.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenvuosiluokka")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenvuosiluokka", koodistoUri = "suorituksentyyppi"),
  @Tooltip("Perusopetuksen vuosiluokkatodistuksen liitetieto (liitteenä annettu arvio käyttäytymisestä tai työskentelystä).")
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
  @Description("Tieto siitä, suoritetaanko perusopetusta normaalina koulutuksena vai erityisenä tutkintona.")
  @Tooltip("Tieto siitä, suoritetaanko perusopetusta normaalina koulutuksena vai erityisenä tutkintona.")
  def suoritustapa: Koodistokoodiviite
}

@Description("Perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella")
case class NuortenPerusopetuksenOppimääränSuoritus(
  @Tooltip("Suoritettava koulutus ja koulutuksen opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: NuortenPerusopetus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  override val osasuoritukset: Option[List[OppiaineenTaiToiminta_AlueenSuoritus]] = None,
  @Description("Vapaamuotoinen tekstikenttä")
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi merkintä siitä, että oppilas on opiskellut tähdellä (*) merkityt oppiaineet yksilöllistetyn oppimäärän mukaan.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetuksenoppimaara", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenPäätasonSuoritus with PerusopetuksenOppimääränSuoritus

@Description("Vuosiluokan todistuksen liitetieto")
case class PerusopetuksenVuosiluokanSuorituksenLiite(
  @Description("Liitetiedon tyyppi kooditettuna.")
  @Tooltip("Liitetiedon tyyppi, eli onko kyseessä käyttäytymisen vai työskentelyn arviointi.")
  @KoodistoUri("perusopetuksentodistuksenliitetieto")
  @KoodistoKoodiarvo("kayttaytyminen")
  @KoodistoKoodiarvo("tyoskentely")
  tunniste: Koodistokoodiviite,
  @Description("Liitetiedon kuvaus.")
  @Tooltip("Liitetiedon kuvaus, eli sanallinen käyttäytymisen tai työskentelyn arviointi.")
  kuvaus: LocalizedString
)

trait Toiminta_AlueenSuoritus extends Suoritus

sealed trait OppiaineenTaiToiminta_AlueenSuoritus extends Suoritus with MahdollisestiSuorituskielellinen

@Description("Perusopetuksen oppiaineen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta")
case class NuortenPerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: PerusopetuksenOppiaine,
  @Description("Jos oppilas opiskelee yhdessä yksilöllistetyn oppimäärän mukaan, myös päättöarviointi voi näissä aineissa olla sanallinen.")
  @Tooltip("Onko oppilas opiskellut oppiaineessa yksilöllisen oppimäärän. Jos oppilas opiskelee yhdessä yksilöllistetyn oppimäärän mukaan, myös päättöarviointi voi näissä aineissa olla sanallinen.")
  yksilöllistettyOppimäärä: Boolean = false,
  @Description("Tieto siitä, onko oppiaineen opetus painotettu (true/false). Painotetun opetuksen (oppiaine tai oppiainekokonaisuus, kaksikielinen opetus) tavoitteet ja arviointiperusteet ovat valtakunnallisen opetussuunnitelman perusteiden mukaiset.")
  @Tooltip("Onko oppilas ollut oppiaineessa painotetussa opetuksessa. Painotetun opetuksen (oppiaine tai oppiainekokonaisuus, kaksikielinen opetus) tavoitteet ja arviointiperusteet ovat valtakunnallisen opetussuunnitelman perusteiden mukaiset.")
  painotettuOpetus: Boolean = false,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends PerusopetuksenOppiaineenSuoritus with OppiaineenTaiToiminta_AlueenSuoritus with Vahvistukseton with Yksilöllistettävä with MahdollisestiSuorituskielellinen

trait PerusopetuksenOppiaineenSuoritus extends OppiaineenSuoritus with PakollisenTaiValinnaisenSuoritus {
  override def salliDuplikaatit = !koulutusmoduuli.pakollinen
}

@Description("Perusopetuksen toiminta-alueen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta. Suoritukset voidaan kirjata oppiaineiden sijaan toiminta-alueittain, jos opiskelijalle on tehty erityisen tuen päätös")
case class PerusopetuksenToiminta_AlueenSuoritus(
  @Description("Toiminta-alueet voivat sisältää yksittäisen oppiaineen tavoitteita ja sisältöjä, jos oppilaalla on vahvuuksia jossakin yksittäisessä oppiaineessa. Opetuksen toteuttamisessa eri toiminta-alueiden sisältöjä voidaan yhdistää. Toiminta-alueesta muodostuu oppiaineen kaltaisia suorituksia")
  @Title("Toiminta-alue")
  koulutusmoduuli: PerusopetuksenToiminta_Alue,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetuksentoimintaalue")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksentoimintaalue", koodistoUri = "suorituksentyyppi")
) extends OppiaineenTaiToiminta_AlueenSuoritus with Vahvistukseton with Toiminta_AlueenSuoritus

trait PerusopetuksenOppiaineenArviointi extends YleissivistävänKoulutuksenArviointi

@Description("Numeerinen arviointi asteikolla 4 (hylätty) - 10 (erinomainen)")
case class NumeerinenPerusopetuksenOppiaineenArviointi(
  arvosana: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD.")
  päivä: Option[LocalDate]
) extends PerusopetuksenOppiaineenArviointi with NumeerinenYleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

@Description("Sanallisessa arvioinnissa suorituksen hyväksymisen ilmaisuun käytetään koodiarvoja S (suoritettu) ja H (hylätty). Koodiarvon lisäksi voidaan liittää sanallinen arviointi vapaana tekstinä kuvaus-kenttään")
case class SanallinenPerusopetuksenOppiaineenArviointi(
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  @SensitiveData
  @Tooltip("Oppiaineen sanallinen arviointi.")
  kuvaus: Option[LocalizedString],
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD")
  päivä: Option[LocalDate] = None
) extends PerusopetuksenOppiaineenArviointi with SanallinenYleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

@Description("Käyttäytymisen arviointi. Koodiarvon lisäksi voidaan liittää sanallinen arviointi vapaana tekstinä kuvaus-kenttään")
@IgnoreInAnyOfDeserialization
case class PerusopetuksenKäyttäytymisenArviointi(
  @Tooltip("Käyttäytymisen arvosana.")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  @SensitiveData
  @Tooltip("Käyttäytymisen sanallinen arviointi.")
  kuvaus: Option[LocalizedString] = None,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD.")
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
  @Description("Toiminta-alueen tunniste")
  @KoodistoUri("perusopetuksentoimintaalue")
  tunniste: Koodistokoodiviite
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
}

@Description("Nuorten perusopetuksen tunnistetiedot")
case class NuortenPerusopetus(
 perusteenDiaarinumero: Option[String],
 @KoodistoKoodiarvo("201101")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus"),
 koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Perusopetus

trait Perusopetus extends Koulutus with Laajuudeton with Tutkinto with PerusopetuksenDiaarinumerollinenKoulutus

@Title("Perusopetuksen luokka-aste")
@Description("Perusopetuksen luokka-asteen (1-9) tunnistetiedot")
case class PerusopetuksenLuokkaAste(
 @Description("Luokka-asteen tunniste (1-9)")
 @KoodistoUri("perusopetuksenluokkaaste")
 @Title("Luokka-aste")
 tunniste: Koodistokoodiviite,
 perusteenDiaarinumero: Option[String],
 koulutustyyppi: Option[Koodistokoodiviite] = None
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
  @Description("Oppiaineen tunnistetiedot")
  @Title("Oppiaine")
  def tunniste: KoodiViite
  @Tooltip("Oppiaineen laajuus vuosiviikkotunteina.")
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

trait PerusopetuksenKoodistostaLöytyväOppiaine extends PerusopetuksenOppiaine with YleissivistavaOppiaine

@Title("Paikallinen valinnainen oppiaine")
case class PerusopetuksenPaikallinenValinnainenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None,
  @Tooltip("Paikallisen oppiaineen vapaamuotoinen kuvaus.")
  kuvaus: LocalizedString,
  perusteenDiaarinumero: Option[String] = None
) extends PerusopetuksenOppiaine with PaikallinenKoulutusmoduuli with StorablePreference {
  def pakollinen: Boolean = false
}

@Title("Muu oppiaine")
case class MuuPeruskoulunOppiaine(
  @KoodistoKoodiarvo("HI")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("BI")
  @KoodistoKoodiarvo("PS")
  @KoodistoKoodiarvo("KT")
  @KoodistoKoodiarvo("ET")
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
case class NuortenPerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[NuortenPerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class NuortenPerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso

case class PakollisetOppiaineet(koodistoViitePalvelu: KoodistoViitePalvelu) {
  lazy val toimintaAlueidenSuoritukset = {
    (1 to 5).map(n => PerusopetuksenToiminta_Alue(koodi("perusopetuksentoimintaalue", n.toString))).map(ta => PerusopetuksenToiminta_AlueenSuoritus(ta)).toList
  }
  private def koodi(koodisto: String, arvo: String) = koodistoViitePalvelu.validateRequired(koodisto, arvo)
  private def aine(koodiarvo: String) = koodi("koskioppiaineetyleissivistava", koodiarvo)
  private def nuortenSuoritus(aine: PerusopetuksenOppiaine) = NuortenPerusopetuksenOppiaineenSuoritus(koulutusmoduuli = aine)
  private def aikuistenSuoritus(aine: PerusopetuksenOppiaine) = AikuistenPerusopetuksenOppiaineenSuoritus(koulutusmoduuli = aine)
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
      case _ => Nil
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
