package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

// Ensimmäinen luonnos Ahvenanmaan perusopetuksen Koski-skeemasta (TOR-2587).
// Haarautettu NuortenPerusopetus.scala:sta. Kommentit viittaavat eduuni-wikin
// väritaulukkoon: punainen tausta / yliviivaus = pudotettu manner-Suomen
// skeemasta; "TODO TOR-2587" -kommentit merkitsevät kenttiä/luokkia, joiden
// kuuluminen lopulliseen skeemaan on vielä Ahvenanmaan työryhmältä
// vahvistamatta (persikanväri wiki-taulukossa).
//
// Avoimet työryhmäkysymykset (odottavat päätöstä, tämä tiedosto ei toistaiseksi
// enää paisu ennen kuin nämä on ratkaistu):
//   - Tutkintotoimikunta ja Yritys näkyvät yhä skeemakatselimessa, koska
//     jaettu `HenkilövahvistusPaikkakunnalla.myöntäjäOrganisaatio` viittaa
//     niihin. Wiki yliviivaa molemmat. Jos halutaan aidosti pois, tarvitaan
//     oma AhvenanmaanHenkilövahvistusPaikkakunnalla jossa org-union on
//     rajatumpi (Oppilaitos | Koulutustoimija | OrganisaatioOID | Toimipiste).
//   - `sisältyyOpiskeluoikeuteen`/SisältäväOpiskeluoikeus – pakollinen
//     `Opiskeluoikeus`-traitin kontrakti. Täällä @Hidden, mutta ei varsinaisesti
//     pudotettu. Aidosti irti saaminen vaatii erillisen opiskeluoikeustraitin.
//   - Opiskeluoikeusjakson tila: oma koodisto vai jaettu
//     koskiopiskeluoikeudentila? Vaikuttaa `AhvenanmaanPerusopetuksen-
//     Opiskeluoikeusjakso.tila`-kenttään.
//   - Käyttäytymisen arviointi ("Ansvar och samarbete"): aina sanallinen vai
//     myös numeerinen? Vaikuttaa `AhvenanmaanPerusopetuksenKäyttäytymisen-
//     Arviointi`-luokan muotoon.
//   - Ahvenanmaan oppiainekoodisto: AhvenanmaanPerusopetuksenMuuOppiaine.tunniste
//     nykyään valtakunnallisen koodiston placeholder (pudotettu AI/KT/ET).
//     Lopullinen koodisto sisältänee mm. svenska, svenska som andraspråk ja
//     yhdistetyn uskonto/livsåskådning-oppiaineen.
//   - Arviointiasteikko: erillinen Ahvenanmaan koodisto (ruotsinkieliset
//     käännökset eroavat valtakunnallisista)?
//   - Koulutuskoodi/perusteenDiaarinumero: `AhvenanmaanPerusopetus` käyttää
//     nyt 201101 ja diaarinumero-kenttä jätetty avoimeksi. Ehdokas
//     ÅLR2020/9841, mutta ePerusteissa ei julkaisua.
//
// Tallennettavuus ja wiring-askeleet (tarvitaan ennen kuin tyyppi voidaan
// aidosti tallentaa Koskeen — tätä ei tehty tässä ensimmäisessä vedoksessa):
//
//   1. OpiskeluoikeudenTyyppi.ahvenanmaanperusopetus -rekisteröinti
//      Opiskeluoikeus.scala:ssa. Sivuvaikutuksena rekisteröity tyyppi tulee
//      mukaan validaattoreihin, käyttöoikeustarkistuksiin, raportointi-
//      maskeihin, tiedonsiirron defaulteihin jne.
//   2. Koodistofixturet: opiskeluoikeudentyyppi-koodistoon "ahvenanmaan-
//      perusopetus" ja suorituksentyyppi-koodistoon uudet suoritustyypit
//      (ahvenanmaanperusopetuksenoppimaara, -vuosiluokka, -oppiaine,
//      -toimintaalue). Lokaalisti mockdata/koodisto/ alla; tuotannossa
//      Koodistopalvelu.
//   3. KoskiValidator (ja muut Perusopetus*Validation-tiedostot) sisältävät
//      case _: PerusopetuksenOpiskeluoikeus -haaroja jotka EIVÄT osu tähän
//      luokkaan. Päätettävä onko manner-Suomen validointi tarkoitus uusio-
//      käyttää (laajenna haarat) vai kirjoittaa erilliset säännöt.
//   4. OpiskeluoikeusAccessChecker – uuden tyypin pääsyoikeudet rooleittain.
//   5. Frontti (editor) + `make ts-types`; mahdolliset raportointikannan/
//      luovutusskeemojen (Kela, HSL, Supa, Hakemuspalvelu, Migri,
//      AktiivisetJaPaattyneetOpinnot) per-tyyppiset lisäykset.
//
// Input-deserialisointi menee automaattisesti:
// SchemaValidatingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus]
// dispatchaa @Discriminator tyyppi -kentän perusteella, ja @KoodistoKoodiarvo
// -annotaatio tässä luokassa riittää siihen ohjaukseen. Käytännössä kuitenkin
// tallennus torjutaan tällä hetkellä askeleessa 2: koodistopalvelu ei tunne
// uutta koodiarvoa, joten validointi epäonnistuu – toimii turvaverkkona
// ennen kuin wiring on valmis.
//
// Skippaus eksplisiittisesti: jos tyyppi halutaan varmuudella ulos tallennus-
// polusta ennen wiringiä, vaihda `extends KoskeenTallennettavaOpiskeluoikeus`
// pelkkään `extends Opiskeluoikeus`:iin – skeemakatselimessa tyyppi säilyy
// näkyvillä, mutta deserialisoija ei enää pidä sitä kelvollisena tallennus-
// kohteena.

@Description("Ahvenanmaan perusopetuksen opiskeluoikeus")
case class AhvenanmaanPerusopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Hidden
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  tila: AhvenanmaanPerusopetuksenOpiskeluoikeudenTila,
  lisätiedot: Option[AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot] = None,
  suoritukset: List[AhvenanmaanPerusopetuksenPäätasonSuoritus],
  @KoodistoKoodiarvo("ahvenanmaanperusopetus")
  tyyppi: Koodistokoodiviite =
    Koodistokoodiviite("ahvenanmaanperusopetus", koodistoUri = "opiskeluoikeudentyyppi"),
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  lähdejärjestelmäkytkentäPurettu: Option[LähdejärjestelmäkytkennänPurkaminen] = None,
) extends KoskeenTallennettavaOpiskeluoikeus {
  @Description("Oppijan oppimäärän päättymispäivä")
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) =
    this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None

  def kotiopetuksessa(päivämäärä: LocalDate): Boolean = lisätiedot match {
    case Some(l) => l.kotiopetusjaksot.toList.flatten.exists(_.contains(päivämäärä))
    case None => false
  }
}

// Lisätiedoista on pudotettu lähes kaikki manner-Suomen kentät; vain
// kotiopetusjaksot säilyy vahvistettuna. Luokka on pidetty eteenpäin
// laajennettavuuden vuoksi.
case class AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot(
  @Description("Kotiopetusjaksot huoltajan päätöksestä alkamis- ja päättymispäivineen.")
  @Tooltip("Kotiopetusjaksot huoltajan päätöksestä alkamis- ja päättymispäivineen.")
  kotiopetusjaksot: Option[List[Aikajakso]] = None,
  // TODO TOR-2587: vahvistettava kuuluuko Ahvenanmaalle; wikissä "Todennäköisesti ei".
  @Description("Opiskelu joustavassa perusopetuksessa (JOPO).")
  @OksaUri("tmpOKSAID453", "joustava perusopetus")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  joustavaPerusopetus: Option[Aikajakso] = None,
  // TODO TOR-2587: peach – vahvistettava.
  @Title("Opiskelee tavoitekokonaisuuksittain")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  tavoitekokonaisuuksittainOpiskelu: Option[List[Aikajakso]] = None,
  // TODO TOR-2587: peach – vahvistettava.
  @Description("Oppilas on vuosiluokkiin sitomattomassa opetuksessa (kyllä/ei).")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @Title("Vuosiluokkiin sitomaton opetus")
  vuosiluokkiinSitoutumatonOpetus: Option[Boolean] = None,
  // TODO TOR-2587: peach – vahvistettava.
  @Description("Perusopetukseen valmistavan opetuksen lisäopetuksen aikajaksot.")
  valmistavanLisäopetus: Option[List[Aikajakso]] = None,
) extends OpiskeluoikeudenLisätiedot

// Opiskeluoikeuden tila.
// TODO TOR-2587: peach – selvitettävä käyttääkö Ahvenanmaa omaa koodistoa
// vai jaettua "koskiopiskeluoikeudentila"-koodistoa. Sallitut arvot wikissä:
// eronnut, peruutettu, katsotaaneronneeksi, lasna, mitatoity,
// valiaikaisestikeskeytynyt, valmistunut.
case class AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AhvenanmaanPerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiLaajaOpiskeluoikeusjakso

// ---------- Päätason suoritukset ----------

trait AhvenanmaanPerusopetuksenPäätasonSuoritus
  extends KoskeenTallennettavaPäätasonSuoritus
  with Toimipisteellinen
  with MonikielinenSuoritus
  with Suorituskielellinen

@Description("Ahvenanmaan perusopetuksen vuosiluokan suoritus. Nämä suoritukset näkyvät lukuvuositodistuksella.")
case class AhvenanmaanPerusopetuksenVuosiluokanSuoritus(
  @Title("Luokka-aste")
  koulutusmoduuli: AhvenanmaanPerusopetuksenLuokkaAste,
  @Description("Luokan tunniste, esimerkiksi 9C.")
  luokka: String,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate] = None,
  @Description("Varsinaisen todistuksen saantipäivämäärä")
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  @KoodistoUri("perusopetuksensuoritustapa")
  suoritustapa: Option[Koodistokoodiviite] = None,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  // TODO TOR-2587: peach – vahvistettava säilyykö Ahvenanmaan skeemassa.
  @Tooltip("Osallistuminen perusopetusta täydentävän oman äidinkielen opiskeluun.")
  omanÄidinkielenOpinnot: Option[AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina] = None,
  // TODO TOR-2587: peach – vahvistettava.
  @Description("Oppilaan kotimaisten kielten kielikylvyn kieli.")
  @KoodistoUri("kieli")
  @OksaUri("tmpOKSAID439", "kielikylpy")
  kielikylpykieli: Option[Koodistokoodiviite] = None,
  @Description("Tieto siitä, että oppilas jää luokalle")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @DefaultValue(false)
  @Title("Oppilas jää luokalle")
  jääLuokalle: Boolean = false,
  // TODO TOR-2587: peach – "Ansvar och samarbete"; muotoa (sanallinen vs. numeerinen) ei ole vahvistettu.
  käyttäytymisenArvio: Option[AhvenanmaanPerusopetuksenKäyttäytymisenArviointi] = None,
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ahvenanmaanperusopetuksenvuosiluokka")
  tyyppi: Koodistokoodiviite =
    Koodistokoodiviite("ahvenanmaanperusopetuksenvuosiluokka", koodistoUri = "suorituksentyyppi"),
) extends AhvenanmaanPerusopetuksenPäätasonSuoritus with Arvioinniton

@Description("Ahvenanmaan perusopetuksen koko oppimäärän suoritus. Nämä suoritukset näkyvät päättötodistuksella.")
case class AhvenanmaanPerusopetuksenOppimääränSuoritus(
  // TODO TOR-2587: peach – Ahvenanmaan koulutuskoodi / perusteenDiaarinumero varmistamatta.
  koulutusmoduuli: AhvenanmaanPerusopetus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
  // TODO TOR-2587: peach – vahvistettava.
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  // TODO TOR-2587: peach – vahvistettava.
  @Tooltip("Osallistuminen perusopetusta täydentävän oman äidinkielen opiskeluun.")
  omanÄidinkielenOpinnot: Option[AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina] = None,
  override val osasuoritukset: Option[List[AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ahvenanmaanperusopetuksenoppimaara")
  tyyppi: Koodistokoodiviite =
    Koodistokoodiviite("ahvenanmaanperusopetuksenoppimaara", koodistoUri = "suorituksentyyppi"),
) extends AhvenanmaanPerusopetuksenPäätasonSuoritus with Arvioinniton

// Aineopiskeluoikeuden suoritus (NuortenPerusopetuksenOppiaineenOppimääränSuoritus)
// on wikissä yliviivattu sekä `suoritukset`-union-rivillä että omana otsikkonaan,
// joten se on pudotettu Ahvenanmaan skeemasta kokonaan.

// ---------- Osasuoritukset ----------

sealed trait AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus
  extends Suoritus
  with MahdollisestiSuorituskielellinen

@Description("Ahvenanmaan perusopetuksen oppiaineen suoritus osana oppimäärän tai vuosiluokan suoritusta.")
case class AhvenanmaanPerusopetuksenOppiaineenSuoritus(
  koulutusmoduuli: AhvenanmaanPerusopetuksenOppiaine,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.SUORITUSJAKO_KATSELIJA))
  yksilöllistettyOppimäärä: Boolean = false,
  // TODO TOR-2587: peach – selvitetään onko tulossa Ahvenanmaalle (wiki).
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.SUORITUSJAKO_KATSELIJA))
  rajattuOppimäärä: Boolean = false,
  arviointi: Option[List[AhvenanmaanPerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ahvenanmaanperusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite =
    Koodistokoodiviite("ahvenanmaanperusopetuksenoppiaine", koodistoUri = "suorituksentyyppi"),
  suoritustapa: Option[Koodistokoodiviite] = None,
  @Title("Luokka-aste")
  @KoodistoUri("perusopetuksenluokkaaste")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.SUORITUSJAKO_KATSELIJA))
  luokkaAste: Option[Koodistokoodiviite] = None,
) extends AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus with Vahvistukseton

// TODO TOR-2587: peach – koko toiminta-alueen suoritus; wikissä "Selvitettävä ovatko
// samat toiminta-alueet ja samoilla nimillä kuin manner-Suomessa".
@Description("Ahvenanmaan perusopetuksen toiminta-alueen suoritus osana oppimäärän tai vuosiluokan suoritusta.")
@SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
case class AhvenanmaanPerusopetuksenToimintaAlueenSuoritus(
  @Title("Toiminta-alue")
  koulutusmoduuli: AhvenanmaanPerusopetuksenToimintaAlue,
  arviointi: Option[List[AhvenanmaanPerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ahvenanmaanperusopetuksentoimintaalue")
  tyyppi: Koodistokoodiviite =
    Koodistokoodiviite("ahvenanmaanperusopetuksentoimintaalue", koodistoUri = "suorituksentyyppi"),
) extends AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus with Vahvistukseton

// ---------- Arvioinnit ----------

sealed trait AhvenanmaanPerusopetuksenOppiaineenArviointi
  extends YleissivistävänKoulutuksenArviointi

// TODO TOR-2587: peach – mahdollinen Ahvenanmaan oma arviointiasteikkokoodisto
// (ruotsinkieliset käännökset eroavat valtakunnallisista).
@Description("Numeerinen arviointi asteikolla 4 (hylätty) - 10 (erinomainen)")
case class NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi(
  arvosana: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD.")
  päivä: Option[LocalDate]
) extends AhvenanmaanPerusopetuksenOppiaineenArviointi with NumeerinenYleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

// TODO TOR-2587: peach – selvitettävä mitkä arvoista S/H/O ovat Ahvenanmaalla käytössä.
@Description("Sanallinen arviointi; koodiarvot S (suoritettu), H (hylätty), O (osallistunut).")
case class SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi(
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  kuvaus: Option[LocalizedString],
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD.")
  päivä: Option[LocalDate] = None
) extends AhvenanmaanPerusopetuksenOppiaineenArviointi with SanallinenYleissivistävänKoulutuksenArviointi {
  def arviointipäivä = päivä
}

// "Ansvar och samarbete" – Ahvenanmaan vastine käyttäytymisen arvioinnille.
// TODO TOR-2587: peach – muoto (sanallinen / numeerinen) vahvistamatta;
// wikissä "Ahvenanmaa ei haluaisi kuvaus-kenttää, mutta kysytään".
@Description("Käyttäytymisen (Ansvar och samarbete) arviointi.")
@IgnoreInAnyOfDeserialization
case class AhvenanmaanPerusopetuksenKäyttäytymisenArviointi(
  arvosana: Koodistokoodiviite = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
  // TODO TOR-2587: peach – Ahvenanmaa ei haluaisi kuvaus-kenttää.
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  kuvaus: Option[LocalizedString] = None,
  @Hidden
  päivä: Option[LocalDate] = None
) extends YleissivistävänKoulutuksenArviointi with SanallinenArviointi {
  def arviointipäivä = päivä
}

// TODO TOR-2587: peach – koko luokka; vahvistettava onko käytössä Ahvenanmaalla.
case class AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina(
  arvosana: Koodistokoodiviite,
  arviointipäivä: Option[LocalDate] = None,
  @KoodistoUri("kieli")
  kieli: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
)

// ---------- Koulutusmoduulit ----------

// TODO TOR-2587: peach – koulutuskoodi (nyt 201101 manner-Suomen mukainen) ja
// perusteenDiaarinumero (ehdokas ÅLR2020/9841; ePerusteissa ei julkaisua).
@Description("Ahvenanmaan perusopetuksen tunnistetiedot")
case class AhvenanmaanPerusopetus(
  perusteenDiaarinumero: Option[String],
  @KoodistoKoodiarvo("201101")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("201101", koodistoUri = "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Perusopetus

@Title("Ahvenanmaan perusopetuksen luokka-aste")
@Description("Ahvenanmaan perusopetuksen luokka-asteen (1-9) tunnistetiedot")
case class AhvenanmaanPerusopetuksenLuokkaAste(
  @KoodistoUri("perusopetuksenluokkaaste")
  @Title("Luokka-aste")
  tunniste: Koodistokoodiviite,
  // TODO TOR-2587: peach – perusteenDiaarinumero vahvistamatta (ehdokas ÅLR2020/9841).
  perusteenDiaarinumero: Option[String],
  // TODO TOR-2587: peach – koulutustyyppi vahvistamatta.
  koulutustyyppi: Option[Koodistokoodiviite] = None,
) extends KoodistostaLöytyväKoulutusmoduuli with Laajuudeton with PerusopetuksenDiaarinumerollinenKoulutus {
  override def laajuus = None
  def luokkaAste = tunniste.koodiarvo
}

// TODO TOR-2587: peach – vahvistettava käyttävätkö Ahvenanmaalla laajuuksia.
@Description("Ahvenanmaan perusopetuksen toiminta-alueen tunnistetiedot")
case class AhvenanmaanPerusopetuksenToimintaAlue(
  @KoodistoUri("perusopetuksentoimintaalue")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None
) extends KoodistostaLöytyväKoulutusmoduuli with KoulutusmoduuliValinnainenLaajuus

// ---------- Oppiaineet ----------

trait AhvenanmaanPerusopetuksenOppiaine
  extends PerusopetuksenOppiaine
  with KoulutusmoduuliValinnainenLaajuus {
  @Tooltip("Oppiaineen laajuus vuosiviikkotunteina.")
  // TODO TOR-2587: peach – vahvistettava käytetäänkö laajuuksia.
  def laajuus: Option[LaajuusVuosiviikkotunneissa]
}

// Valtakunnalliset KT/ET/AI-oppiaineet on pudotettu: Ahvenanmaalla uskonto ja
// livsåskådning on yksi yhteinen oppiaine, ja äidinkielenä on pelkästään
// svenska / svenska som andraspråk (ei AI-rakennetta).
// TODO TOR-2587: peach – Ahvenanmaan oma oppiainekoodisto puuttuu vielä;
// @KoodistoKoodiarvo-listaus päivitetään kun koodisto on määritelty. Alla
// nykyinen valtakunnallinen lista toimii placeholderina, josta on pudotettu
// AI, KT ja ET.
@Title("Oppiaine")
case class AhvenanmaanPerusopetuksenMuuOppiaine(
  @KoodistoKoodiarvo("HI")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("BI")
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
  @KoodistoKoodiarvo("PS")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends AhvenanmaanPerusopetuksenOppiaine
  with YleissivistavaOppiaine
  with KoodistostaLöytyväKoulutusmoduuli

// Ahvenanmaalla on vain "vieras kieli" (ei toista kotimaista).
// TODO TOR-2587: nimi/koodisto voi muuttua kun Ahvenanmaan oppiainekoodisto valmistuu.
case class AhvenanmaanPerusopetuksenVierasKieli(
  @KoodistoKoodiarvo("A1")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
  @KoodistoKoodiarvo("B3")
  @KoodistoKoodiarvo("AOM")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  kieli: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends AhvenanmaanPerusopetuksenOppiaine
  with Kieliaine
  with KoodistostaLöytyväKoulutusmoduuli

case class AhvenanmaanPerusopetuksenPaikallinenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusVuosiviikkotunneissa] = None,
  kuvaus: LocalizedString,
  perusteenDiaarinumero: Option[String] = None,
  @DefaultValue(false)
  pakollinen: Boolean = false
) extends AhvenanmaanPerusopetuksenOppiaine with PerusopetuksenPaikallinenOppiaine
