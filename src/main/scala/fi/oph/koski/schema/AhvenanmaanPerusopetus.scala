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
// ┌─────────────────────────────────────────────────────────────────────────┐
// │ Avoimet kysymykset asiantuntijoille — priorisoitu blast radiuksen mukaan │
// └─────────────────────────────────────────────────────────────────────────┘
//
// ── Rakennekysymykset (ratkaise ensin — vaikuttavat luokkahierarkiaan) ───
//
//  1. Aineopiskeluoikeus (NuortenPerusopetuksenOppiaineenOppimääränSuoritus)
//     — yliviivattu wikissä; tällä hetkellä pudotettu. Tuleeko myöhemmin?
//  2. Koodistot — omat vai jaetut?
//     a) (ratkaistu: jaettu koskiopiskeluoikeudentila, ei peruutettu-tilaa)
//     b) oppiainekoodisto: oma tarvitaan koska AI puuttuu (vain
//        svenska / svenska som andraspråk) ja uskonto+livsåskådning
//        yhdistetty yhdeksi. Koodiston sisältö?
//     c) arviointiasteikkokoodisto: oma (ruotsinkieliset käännökset
//        eroavat valtakunnallisista)?
//  3. (ratkaistu: diaarinumero ÅLR2020/9841, koulutuskoodi 201101,
//     ePerusteet-validointia ei käytetä koska ops ei ole julkisena)
//
// ── Peach-kenttien vahvistus (jokaiselle: kuuluuko skeemaan?) ───────────
//
//  4. (ratkaistu: samat 5 toiminta-aluetta kuin manner-Suomessa, mutta
//     omassa ahvenanmaanperusopetuksentoimintaalue-koodistossa)
//  5. (ratkaistu: omanÄidinkielenOpinnot, luokkaAste ja
//     vuosiluokkiinSitoutumatonOpetus pudotettu)
//  6. (ratkaistu: joustavaPerusopetus, valmistavanLisäopetus,
//     tavoitekokonaisuuksittainOpiskelu, kielikylpykieli ja rajattuOppimäärä
//     pudotettu; yksilöllistettyOppimäärä → mukautettuOppimäärä)
//  7. (ratkaistu: vuosiluokkiinSitoutumatonOpetus pudotettu)
//  8. (ratkaistu: laajuudet vvt pidetään, ei pakollinen)
//
// ── Vastuu ja yhteistyö ("Ansvar och samarbete") ─────────────────────────
//
//  9. (ratkaistu: sanallinen, vain arvo G; käyttäytymisenArvio →
//     vastuuJaYhteistyöArvio)
// 10. (ratkaistu: kuvaus-kenttä pudotettu)
//
// ── Sanallinen arviointi ─────────────────────────────────────────────────
//
// 11. Mitkä arvoista G/D/U ovat käytössä? Kaikki vai osa? (kuvaus-kenttä
//     pudotettu)
//
// ── Jaetut tyypit (rajatumpi haarautus?) ────────────────────────────────
//
// 12. HenkilövahvistusPaikkakunnalla.myöntäjäOrganisaatio sisältää nyt
//     Tutkintotoimikunnan ja Yrityksen joita wiki ei halua. Tehdäänkö
//     Ahvenanmaalle oma vahvistustyyppi (rajattu org-union)?
// 13. sisältyyOpiskeluoikeuteen — pidetäänkö @Hidden vai pudotetaan
//     kokonaan (vaatii oman opiskeluoikeus-traitin)?
//
// ── Kosmeettinen ────────────────────────────────────────────────────────
//
// 14. Oppiaine-luokan nimi: jääkö MuuOppiaine vai Oppiaine /
//     AhvenanmaanOppiaine?
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
// Tuotannon kirjoitussuojaus:
// Tuotannossa tallennus estetään features.disabledPäätasonSuoritusLuokat
// -konfiguraatiolla:
//   features.disabledPäätasonSuoritusLuokat = [
//     "AhvenanmaanPerusopetuksenVuosiluokanSuoritus",
//     "AhvenanmaanPerusopetuksenOppimääränSuoritus"
//   ]
// Kun Ahvenanmaa on valmis tuotantoon, poistetaan nämä tuotantokonfiguraatiosta.

@Description("Ahvenanmaan perusopetuksen opiskeluoikeus")
case class AhvenanmaanPerusopetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
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
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None

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
) extends OpiskeluoikeudenLisätiedot

// Käytetään valtakunnallista koskiopiskeluoikeudentila-koodistoa, mutta
// peruutettu-tilaa ei sallita.
case class AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AhvenanmaanPerusopetuksenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("eronnut")
  @KoodistoKoodiarvo("katsotaaneronneeksi")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("valiaikaisestikeskeytynyt")
  @KoodistoKoodiarvo("valmistunut")
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso

// ---------- Päätason suoritukset ----------

trait AhvenanmaanPerusopetuksenPäätasonSuoritus
  extends KoskeenTallennettavaPäätasonSuoritus
  with Toimipisteellinen
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
  @Description("Tieto siitä, että oppilas jää luokalle")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @DefaultValue(false)
  @Title("Oppilas jää luokalle")
  jääLuokalle: Boolean = false,
  vastuuJaYhteistyöArvio: Option[AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi] = None,
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
  // Suora @KoodistoUri koska ei extendaa SuoritustavallinenPerusopetuksenSuoritus-traitia.
  // Jos skeema pysyy lähellä manner-Suomea, harkitse traitin käyttöä tämän tilalla.
  @KoodistoUri("perusopetuksensuoritustapa")
  suoritustapa: Koodistokoodiviite,
  suorituskieli: Koodistokoodiviite,
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
  @DefaultValue(false)
  mukautettuOppimäärä: Boolean = false,
  arviointi: Option[List[AhvenanmaanPerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ahvenanmaanperusopetuksenoppiaine")
  tyyppi: Koodistokoodiviite =
    Koodistokoodiviite("ahvenanmaanperusopetuksenoppiaine", koodistoUri = "suorituksentyyppi"),
  // Suora @KoodistoUri koska ei extendaa SuoritustapanaMahdollisestiErityinenTutkinto-traitia.
  // Jos skeema pysyy lähellä manner-Suomea, harkitse traitin käyttöä tämän tilalla.
  @KoodistoUri("perusopetuksensuoritustapa")
  @KoodistoKoodiarvo("erityinentutkinto")
  suoritustapa: Option[Koodistokoodiviite] = None,
) extends AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus with Vahvistukseton

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
  extends Arviointi

@Description("Numeerinen arviointi asteikolla 4 (underkänd) - 10 (utmärkt)")
case class NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi(
  @KoodistoUri("ahvenanmaanarviointiasteikkoyleissivistava")
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  arvosana: Koodistokoodiviite,
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD.")
  päivä: Option[LocalDate]
) extends AhvenanmaanPerusopetuksenOppiaineenArviointi with KoodistostaLöytyväArviointi {
  def arviointipäivä = päivä
  def arvioitsijat = None
  def hyväksytty = arvosana.koodiarvo != "4"
}

@Description("Sanallinen arviointi; koodiarvot G (godkänd), D (deltagit), U (underkänd).")
case class SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi(
  @KoodistoUri("ahvenanmaanarviointiasteikkoyleissivistava")
  @KoodistoKoodiarvo("G")
  @KoodistoKoodiarvo("D")
  @KoodistoKoodiarvo("U")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("G", "ahvenanmaanarviointiasteikkoyleissivistava"),
  @Description("Päivämäärä, jolloin arviointi on annettu. Muoto YYYY-MM-DD.")
  päivä: Option[LocalDate] = None
) extends AhvenanmaanPerusopetuksenOppiaineenArviointi with KoodistostaLöytyväArviointi {
  def arviointipäivä = päivä
  def arvioitsijat = None
  def hyväksytty = arvosana.koodiarvo != "U"
}

// "Ansvar och samarbete" – Ahvenanmaan vastine käyttäytymisen arvioinnille.
@Description("Vastuu ja yhteistyö (Ansvar och samarbete) -arviointi. Sallittu arvo G (godkänd).")
@IgnoreInAnyOfDeserialization
case class AhvenanmaanPerusopetuksenVastuuJaYhteistyöArviointi(
  @KoodistoUri("ahvenanmaanarviointiasteikkoyleissivistava")
  @KoodistoKoodiarvo("G")
  arvosana: Koodistokoodiviite = Koodistokoodiviite("G", "ahvenanmaanarviointiasteikkoyleissivistava"),
  @Hidden
  päivä: Option[LocalDate] = None
) extends KoodistostaLöytyväArviointi {
  def arviointipäivä = päivä
  def arvioitsijat = None
  def hyväksytty = true
}

// ---------- Koulutusmoduulit ----------

// Diaarinumero on ÅLR2020/9841 mutta ops ei ole julkisena ePerusteissa,
// joten ePerusteet-validointia ei voi käyttää.
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
  // Diaarinumero ÅLR2020/9841; ei ePerusteissa.
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
  @KoodistoUri("ahvenanmaanperusopetuksentoimintaalue")
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

@Title("Oppiaine")
case class AhvenanmaanPerusopetuksenMuuOppiaine(
  @KoodistoUri("ahvenanmaankoskioppiaineetyleissivistava")
  @KoodistoKoodiarvo("SV")
  @KoodistoKoodiarvo("SVA")
  @KoodistoKoodiarvo("MA")
  @KoodistoKoodiarvo("OM")
  @KoodistoKoodiarvo("BI")
  @KoodistoKoodiarvo("GE")
  @KoodistoKoodiarvo("FYKE")
  @KoodistoKoodiarvo("FY")
  @KoodistoKoodiarvo("KE")
  @KoodistoKoodiarvo("TE")
  @KoodistoKoodiarvo("RELI")
  @KoodistoKoodiarvo("HI")
  @KoodistoKoodiarvo("MU")
  @KoodistoKoodiarvo("SA")
  @KoodistoKoodiarvo("KU")
  @KoodistoKoodiarvo("KS")
  @KoodistoKoodiarvo("TX")
  @KoodistoKoodiarvo("TN")
  @KoodistoKoodiarvo("ID")
  @KoodistoKoodiarvo("HEKO")
  @KoodistoKoodiarvo("EH")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean = true,
  perusteenDiaarinumero: Option[String] = None,
  override val laajuus: Option[LaajuusVuosiviikkotunneissa] = None,
  kuvaus: Option[LocalizedString] = None
) extends AhvenanmaanPerusopetuksenOppiaine
  with KoodistostaLöytyväKoulutusmoduuli

// Ahvenanmaalla on vain "vieras kieli" (ei toista kotimaista).
case class AhvenanmaanPerusopetuksenVierasKieli(
  @KoodistoUri("ahvenanmaankoskioppiaineetyleissivistava")
  @KoodistoKoodiarvo("A1")
  @KoodistoKoodiarvo("A2")
  @KoodistoKoodiarvo("B1")
  @KoodistoKoodiarvo("B2")
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
