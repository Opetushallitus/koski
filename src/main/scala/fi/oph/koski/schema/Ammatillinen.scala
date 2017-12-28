package fi.oph.koski.schema

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.localization.{LocalizationRepository, LocalizedString}
import fi.oph.koski.localization.LocalizedString._
import fi.oph.scalaschema.annotation._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.annotation._
import mojave._

@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class AmmatillinenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  @Description("Opiskelijan opiskeluoikeuden arvioitu päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa")
  arvioituPäättymispäivä: Option[LocalDate] = None,
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa")
  päättymispäivä: Option[LocalDate] = None,
  tila: AmmatillinenOpiskeluoikeudenTila,
  suoritukset: List[AmmatillinenPäätasonSuoritus],
  lisätiedot: Option[AmmatillisenOpiskeluoikeudenLisätiedot] = None,
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinenkoulutus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
}

sealed trait AmmatillinenPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Koulutussopimuksellinen with Suorituskielellinen

trait Työssäoppimisjaksollinen {
  @Description("Tutkinnon suoritukseen kuuluvien työssäoppimisjaksojen tiedot (aika, paikka, työtehtävät, laajuus)")
  def työssäoppimisjaksot: Option[List[Työssäoppimisjakso]]
}

trait Koulutussopimuksellinen extends Työssäoppimisjaksollinen {
  @Description("Tutkinnon suoritukseen kuuluvien koulutusjaksojen tiedot (aika, paikka, työtehtävät, laajuus)")
  def koulutussopimukset: Option[List[Koulutussopimusjakso]]
}

@Description("Ammatillisen opiskeluoikeuden lisätiedot (mm. rahoituksessa käytettävät)")
case class AmmatillisenOpiskeluoikeudenLisätiedot(
  @Description("Onko opiskelijalla oikeus maksuttomaan asuntolapaikkaan (true / false)")
  @Tooltip("Valitse valintaruutu, jos opiskelijalla on oikeus maksuttomaan asuntolapaikkaan.")
  oikeusMaksuttomaanAsuntolapaikkaan: Boolean = false,
  @Description("Koulutuksen tarjoajan majoitus, huoneeseen muuttopäivä ja lähtöpäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Koulutuksen järjestäjän tarjoama majoitus. Huoneeseen muuttopäivä ja lähtöpäivä. Voit lisätä useita majoitusjaksoja 'lisää uusi'-painikkeella. Rahoituksen laskennassa käytettävä tieto.")
  majoitus: Option[List[Aikajakso]] = None,
  @Description("Sisäoppilaitosmuotoinen majoitus, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Sisäoppilaitosmuotoisen majoituksen aloituspäivä ja loppupäivä. Voit lisätä useita sisäoppilaitosmuotoisia majoitusjaksoja 'lisää uusi'-painikkeella. Rahoituksen laskennassa käytettävä tieto")
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
  @Description("Vaativan erityisen tuen yhteydessä järjestettävä majoitus. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Vaativan erityisen tuen yhteydessä järjestettävä majoitus (aloitus- ja loppupäivä). Voit lisätä useita vaativan erityisen tuen yhteydessä järjestettäviä majoitusjaksoja 'lisää uusi'-painikkeella. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[Aikajakso]] = None,
  @SensitiveData
  @Description("Tieto siitä että oppija on erityisopetuksessa, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä että oppija on erityisopetuksessa. Merkitään erityisopetuksen alku- ja loppupäivä. Voit lisätä useita erityisen tuen jaksoja 'lisää uusi'-painikkeella. Rahoituksen laskennassa käytettävä tieto.")
  erityinenTuki: Option[List[Aikajakso]] = None,
  @Description("Tieto siitä että oppija on vaativan erityisen tuen erityisen tehtävän erityisen tuen piirissä (aloituspäivä ja loppupäivä). Lista alku-loppu päivämääräpareja. Oppilaitoksen opetusluvassa tulee olla myönnetty vaativan erityisen tuen tehtävä. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä että oppija on vaativan erityisen tuen erityisen tehtävän erityisen tuen piirissä (aloituspäivä ja loppupäivä). Voit lisätä useita vaativan erityisen tuen erityisen tehtävän jaksoja 'lisää uusi'-painikkeella. Oppilaitoksen opetusluvassa tulee olla myönnetty vaativan erityisen tuen tehtävä. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  vaativanErityisenTuenErityinenTehtävä: Option[List[Aikajakso]] = None,
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @SensitiveData
  hojks: Option[Hojks],
  @Description("Onko oppija vaikeasti vammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija vaikeasti vammainen (alku- ja loppupäivä). Voit lisätä useita 'Vaikeasti vammainen'-jaksoja 'lisää uusi'-painikkeella. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData
  vaikeastiVammainen: Option[List[Aikajakso]] = None,
  @Description("Onko oppija vammainen ja hänellä on avustaja. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Onko oppija vammainen ja hänellä on avustaja (alku- ja loppupäivä). Voit lisätä useita 'Vammainen ja avustaja'-jaksoja 'lisää uusi'-painikkeella. Rahoituksen laskennassa käytettävä tieto")
  @SensitiveData
  vammainenJaAvustaja: Option[List[Aikajakso]] = None,
  @Description("Kyseessä on osa-aikainen opiskelu. Lista alku-loppu päivämääräpareja. Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole osa-aikainen opiskelu. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Osa-aikaisuusjaksojen tiedot (jakson alku- ja loppupäivät sekä osa-aikaisuus prosenttilukuna). Voit lisätä useita osa-aikaisuusjaksoja 'lisää uusi'-painikkeella. Rahoituksen laskennassa käytettävä tieto")
  @Title("Osa-aikaisuusjaksot")
  osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]] = None,
  @Description("Opiskeluvalmiuksia tukevat opinnot (Laki ammatillisesta koulutuksesta 531/2017 63 §) välitetään jaksoina (alku- ja loppupäivämäärä) kun kyseessä on päätoiminen opiskelu. Jaksojen alku- ja loppupäivämäärätietojen lisäksi välitetään opintojen vapaamuotoinen kuvaus.")
  @Tooltip("Opiskeluvalmiuksia tukevat opinnot (Laki ammatillisesta koulutuksesta 531/2017 63 §) tallennetaan jaksoina (alku- ja loppupäivämäärä) kun kyseessä on päätoiminen opiskelu. Jaksojen alku- ja loppupäivämäärätietojen lisäksi tallennetaan opintojen vapaamuotoinen kuvaus. Voit lisätä useita opiskeluvalmiuksia tukevien opintojen jaksoja 'lisää uusi'-painikkeella.")
  @SensitiveData
  opiskeluvalmiuksiaTukevatOpinnot: Option[List[OpiskeluvalmiuksiaTukevienOpintojenJakso]] = None,
  @Description("Kyseessä on henkilöstökoulutus (kyllä/ei). Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole henkilöstökoulutus. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Valitse valintaruutu, jos kyseessä on henkilöstökoulutus.")
  @DefaultValue(false)
  henkilöstökoulutus: Boolean = false,
  @Description("Kyseessä on vankilaopetus. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Tieto vankilaopetusjaksoista (alku- ja loppupäivämäärä). Voit lisätä useita vankilaopetusjaksoja 'lisää uusi'-painikkeella. Rahoituksen laskennassa käytettävä tieto")
  @SensitiveData
  vankilaopetuksessa: Option[List[Aikajakso]] = None,
  @Description("Onko kyseessä koulutusvientikoulutus (kyllä/ei). Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole koulutusvientikoulutus.")
  @Tooltip("Valitse valintaruutu, jos kyseessä on koulutusvientikoulutus.")
  @DefaultValue(false)
  koulutusvienti: Boolean = false
) extends OpiskeluoikeudenLisätiedot

@Description("Aikajakson pituus (alku- ja loppupäivämäärä)")
case class Aikajakso (
  alku: LocalDate,
  loppu: Option[LocalDate]
) extends Jakso

@Title("Osa-aikaisuusjakso")
@Description("Osa-aikaisuusjakson kesto ja suuruus")
case class OsaAikaisuusJakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Osa-aikaisuuden suuruus prosentteina. Yksi täysipäiväinen opiskelupäivä viikossa = 20.")
  @Tooltip("Osa-aikaisuuden suuruus prosentteina. Yksi täysipäiväinen opiskelupäivä viikossa = 20.")
  @MinValueExclusive(0)
  @MaxValueExclusive(100)
  @UnitOfMeasure("%")
  @Title("Osa-aikaisuus")
  osaAikaisuus: Int
) extends Jakso

case class OpiskeluvalmiuksiaTukevienOpintojenJakso(
  alku: LocalDate,
  loppu: LocalDate,
  @Description("Opiskeluvalmiuksia tukevien opintojen vapaamuotoinen kuvaus.")
  @Tooltip("Opiskeluvalmiuksia tukevien opintojen vapaamuotoinen kuvaus.")
  kuvaus: LocalizedString
)

@Description("Ks. tarkemmin ammatillisen opiskeluoikeuden tilat: [confluence](https://confluence.csc.fi/pages/viewpage.action?pageId=71953716)")
case class AmmatillinenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AmmatillinenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

@Description("Sisältää myös tiedon opintojen rahoituksesta jaksoittain")
case class AmmatillinenOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("eronnut")
  @KoodistoKoodiarvo("katsotaaneronneeksi")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("peruutettu")
  @KoodistoKoodiarvo("valiaikaisestikeskeytynyt")
  @KoodistoKoodiarvo("valmistunut")
  @KoodistoKoodiarvo("loma")
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  override val opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiOpiskeluoikeusjakso

@Description("Suoritettavan näyttötutkintoon valmistavan koulutuksen tiedot")
case class NäyttötutkintoonValmistavanKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus = NäyttötutkintoonValmistavaKoulutus(),
  @Description("Tässä kentässä kuvataan sen tutkinnon tiedot, johon valmistava koulutus tähtää")
  tutkinto: AmmatillinenTutkintoKoulutus,
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  override val osaamisala: Option[List[Osaamisalajakso]] = None,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate],
  @Description("Suorituksen päättymispäivä. Muoto YYYY-MM-DD")
  päättymispäivä: Option[LocalDate],
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään rakennetta osaamisenHankkimistapa tämän sijaan.")
  järjestämismuodot: Option[List[Järjestämismuotojakso]] = None,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("nayttotutkintoonvalmistavakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nayttotutkintoonvalmistavakoulutus", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillinenPäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with Ryhmällinen with Tutkintonimikkeellinen with Osaamisalallinen

@Description("Näyttötutkintoon valmistavan koulutuksen tunnistetiedot")
case class NäyttötutkintoonValmistavaKoulutus(
  @KoodistoKoodiarvo("999904")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999904", "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Koulutus with Laajuudeton

@Description("Suoritettavan ammatillisen tutkinnon tiedot")
case class AmmatillisenTutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  suoritustapa: Koodistokoodiviite,
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  override val osaamisala: Option[List[Osaamisalajakso]] = None,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Koulutuksen järjestämismuoto. Oppilaitosmuotoinen tai - oppisopimuskoulutus. Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään rakennetta osaamisenHankkimistapa tämän sijaan.")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuodot: Option[List[Järjestämismuotojakso]] = None,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkinto", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillisenTutkinnonOsittainenTaiKokoSuoritus with Todistus

@ReadFlattened
case class Osaamisalajakso(
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  osaamisala: Koodistokoodiviite,
  alku: Option[LocalDate] = None,
  loppu: Option[LocalDate] = None
)

@Description("Oppija suorittaa yhtä tai useampaa tutkinnon osaa, eikä koko tutkintoa. Mikäli opiskelija suorittaa toista osaamisalaa tai tutkintonimikettä erillisessä opiskeluoikeudessa, välitään tieto tällöin tämän rakenteen kautta")
@Title("Ammatillisen tutkinnon osa/osia")
case class AmmatillisenTutkinnonOsittainenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  suoritustapa: Koodistokoodiviite,
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Description("Onko kyse uuden tutkintonimikkeen suorituksesta, liittyen aiemmin suoritettuun tutkintoon")
  @DefaultValue(false)
  toinenTutkintonimike: Boolean = false,
  override val osaamisala: Option[List[Osaamisalajakso]] = None,
  @Description("Onko kyse uuden osaamisalan suorituksesta, liittyen aiemmin suoritettuun tutkintoon")
  @DefaultValue(false)
  toinenOsaamisala: Boolean = false,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Koulutuksen järjestämismuoto. Oppilaitosmuotoinen tai - oppisopimuskoulutus. Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään rakennetta osaamisenHankkimistapa tämän sijaan.")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuodot: Option[List[Järjestämismuotojakso]] = None,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  @Description("Kun kyseessä on toinen osaamisala tai tutkintonimike, viittaus aiempaan suoritukseen välitetään tässä")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkintoosittainen", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillisenTutkinnonOsittainenTaiKokoSuoritus

trait AmmatillisenTutkinnonOsittainenTaiKokoSuoritus extends  AmmatillinenPäätasonSuoritus with Toimipisteellinen with Arvioinniton with Ryhmällinen with Tutkintonimikkeellinen with Osaamisalallinen {
  def koulutusmoduuli: AmmatillinenTutkintoKoulutus
  @Description("Tutkinnon suoritustapa (näyttö / ops / reformi). Ammatillisen perustutkinnon voi suorittaa joko opetussuunnitelmaperusteisesti tai näyttönä. Ammatillisen reformin (531/2017) mukaiset suoritukset välitetään suoritustavalla reformi. ")
  @OksaUri("tmpOKSAID141", "ammatillisen koulutuksen järjestämistapa")
  @KoodistoUri("ammatillisentutkinnonsuoritustapa")
  @ReadOnly("Suoritustapaa ei tyypillisesti vaihdeta suorituksen luonnin jälkeen")
  def suoritustapa: Koodistokoodiviite
}

trait AmmatillisenTutkinnonOsanSuoritus extends Suoritus with MahdollisestiSuorituskielellinen with MahdollisestiToimipisteellinen {
  @Description("Suoritettavan tutkinnon osan tunnistetiedot")
  @Title("Tutkinnon osa")
  @Discriminator
  def koulutusmoduuli: AmmatillisenTutkinnonOsa
  @Description("Tutkinto, jonka rakenteeseen tutkinnon osa liittyy. Käytetään vain tapauksissa, joissa tutkinnon osa on poimittu toisesta tutkinnosta")
  def tutkinto: Option[AmmatillinenTutkintoKoulutus]
  @KoodistoUri("ammatillisentutkinnonosanryhma")
  def tutkinnonOsanRyhmä: Option[Koodistokoodiviite]
  def toimipiste: Option[OrganisaatioWithOid]
  def arviointi: Option[List[AmmatillinenArviointi]]
  @Description("Tutkinnon osalta ei vaadita vahvistusta, mikäli se sisältyy ammatillisen tutkinnon suoritukseen (jolla puolestaan on VALMIS-tilassa oltava vahvistus)")
  def vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä]
  def alkamispäivä: Option[LocalDate]
  @ComplexObject
  def tunnustettu: Option[OsaamisenTunnustaminen]
  @Description("Suoritukseen liittyvän näytön tiedot")
  @ComplexObject
  def näyttö: Option[Näyttö]
  @ComplexObject
  def lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]
  def suorituskieli: Option[Koodistokoodiviite]
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  def tyyppi: Koodistokoodiviite
  def toimipisteellä(toimipiste: OrganisaatioWithOid): AmmatillisenTutkinnonOsanSuoritus = lens[AmmatillisenTutkinnonOsanSuoritus].field[Option[OrganisaatioWithOid]]("toimipiste").set(this)(Some(toimipiste))

  override def ryhmittelytekijä: Option[String] = tutkinnonOsanRyhmä.map(_.toString)
}

@Description("Ammatilliseen tutkintoon liittyvän yhteisen tutkinnonosan suoritus")
@Title("Yhteisen tutkinnon osan suoritus")
case class YhteisenAmmatillisenTutkinnonOsanSuoritus(
  koulutusmoduuli: YhteinenTutkinnonOsa,
  tutkinto: Option[AmmatillinenTutkintoKoulutus] = None,
  @Description("Tieto siitä mihin tutkinnon osan ryhmään osan suoritus (Ammatilliset tutkinnon osat, Yhteiset tutkinnon osat, Vapaavalintaiset tutkinnon osat, Tutkintoa yksilöllisesti laajentavat tutkinnon osat) kuuluu")
  @KoodistoKoodiarvo("2") // Yhteiset tutkinnon osat
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite] = None,
  toimipiste: Option[OrganisaatioWithOid],
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  näyttö: Option[Näyttö] = None,
  @Title("Osa-alueet")
  override val osasuoritukset: Option[List[YhteisenTutkinnonOsanOsaAlueenSuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends AmmatillisenTutkinnonOsanSuoritus with MahdollisestiToimipisteellinen

@Description("Ammatilliseen tutkintoon liittyvän, muun kuin yhteisen tutkinnonosan suoritus")
@Title("Muun tutkinnon osan suoritus")
case class MuunAmmatillisenTutkinnonOsanSuoritus(
  koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa,
  tutkinto: Option[AmmatillinenTutkintoKoulutus] = None,
  @Description("Tieto siitä mihin tutkinnon osan ryhmään osan suoritus (Ammatilliset tutkinnon osat, Yhteiset tutkinnon osat, Vapaavalintaiset tutkinnon osat, Tutkintoa yksilöllisesti laajentavat tutkinnon osat) kuuluu")
  @KoodistoKoodiarvo("1") // Ammatilliset tutkinnon osat
  @KoodistoKoodiarvo("3") // Vapaavalintaiset tutkinnon osat
  @KoodistoKoodiarvo("4") // Tutkintoa yksilöllisesti laajentavat tutkinnon osat
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite] = None,
  toimipiste: Option[OrganisaatioWithOid],
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  näyttö: Option[Näyttö] = None,
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends AmmatillisenTutkinnonOsanSuoritus with MahdollisestiToimipisteellinen

case class Järjestämismuotojakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Koulutuksen järjestämismuoto")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuoto: Järjestämismuoto
) extends Jakso

case class OsaamisenHankkimistapajakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Osaamisen hankkimistapa")
  osaamisenHankkimistapa: OsaamisenHankkimistapa
) extends Jakso

trait Oppimisjakso extends Jakso {
  def alku: LocalDate
  def loppu: Option[LocalDate]
  @Description("Työssäoppimispaikan nimi")
  def työssäoppimispaikka: Option[LocalizedString]
  @KoodistoUri("kunta")
  @Description("Kunta, jossa työssäoppiminen on tapahtunut")
  def paikkakunta: Koodistokoodiviite
  @Description("Maa, jossa työssäoppiminen on tapahtunut")
  @KoodistoUri("maatjavaltiot2")
  def maa: Koodistokoodiviite
  @Description("Työtehtävien kuvaus")
  def työtehtävät: Option[LocalizedString]
}

case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[LocalizedString],
  paikkakunta: Koodistokoodiviite,
  maa: Koodistokoodiviite,
  työtehtävät: Option[LocalizedString],
  laajuus: LaajuusOsaamispisteissä
) extends Oppimisjakso

case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[LocalizedString],
  paikkakunta: Koodistokoodiviite,
  maa: Koodistokoodiviite,
  työtehtävät: Option[LocalizedString]
) extends Oppimisjakso

@Title("Ammatillinen tutkintokoulutus")
@Description("Ammatillisen tutkinnon tunnistetiedot. Ammatillisille koulutuksille on ePerusteet")
case class AmmatillinenTutkintoKoulutus(
 tunniste: Koodistokoodiviite,
 perusteenDiaarinumero: Option[String],
 koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Laajuudeton with Tutkinto

sealed trait AmmatillisenTutkinnonOsa extends Koulutusmoduuli with LaajuuttaEiValidoida {
  def laajuus: Option[LaajuusOsaamispisteissä]
  def pakollinen: Boolean
}

object AmmatillisenTutkinnonOsa {
  val yhteisetTutkinnonOsat = List("101053", "101054", "101055", "101056").map(Koodistokoodiviite(_, "tutkinnonosat"))
}

trait ValtakunnallinenTutkinnonOsa extends AmmatillisenTutkinnonOsa with KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus with ValmaKoulutuksenOsa with TelmaKoulutuksenOsa with NäyttötutkintoonValmistavanKoulutuksenOsa {
  @Description("Tutkinnon osan kansallinen koodi")
  @KoodistoUri("tutkinnonosat")
  def tunniste: Koodistokoodiviite
}

trait MuuKuinYhteinenTutkinnonOsa extends AmmatillisenTutkinnonOsa

@Description("Yhteisen tutkinnon osan tunnistetiedot")
case class YhteinenTutkinnonOsa(
  @KoodistoKoodiarvo("101053")
  @KoodistoKoodiarvo("101054")
  @KoodistoKoodiarvo("101055")
  @KoodistoKoodiarvo("101056")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends ValtakunnallinenTutkinnonOsa

@Description("Opetussuunnitelmaan kuuluvan tutkinnon osan tunnistetiedot")
case class MuuValtakunnallinenTutkinnonOsa(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends ValtakunnallinenTutkinnonOsa with MuuKuinYhteinenTutkinnonOsa

@Description("Paikallisen tutkinnon osan tunnistetiedot")
case class PaikallinenTutkinnonOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsa with PaikallinenKoulutusmoduuli with Valinnaisuus with MuuKuinYhteinenTutkinnonOsa

@Title("Ammatillisen tutkinnon osaa pienempi kokonaisuus")
@Description("Muiden kuin yhteisten tutkinnon osien osasuoritukset")
case class AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
  @Title("Kokonaisuus")
  koulutusmoduuli: AmmatillisenTutkinnonOsaaPienempiKokonaisuus,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ammatillisentutkinnonosaapienempikokonaisuus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosaapienempikokonaisuus", "suorituksentyyppi")
) extends Suoritus with Vahvistukseton with MahdollisestiSuorituskielellinen

@Title("Yhteisen tutkinnon osan osa-alueen suoritus")
@Description("Yhteisen tutkinnon osan osa-alueen suorituksen tiedot")
case class YhteisenTutkinnonOsanOsaAlueenSuoritus(
  @Title("Osa-alue")
  @Description("Ammatillisen tutkinnon osan osa-alueen (vieras tai toinen kotimainen kieli, äidinkieli, paikallinen tutkinnon osan osa-alue, valtakunnallinen tutkinnon osan osa-alue) tunnistetiedot")
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  @Description("Jos osa-alue on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosanosaalue", "suorituksentyyppi")
) extends Suoritus with Vahvistukseton with MahdollisestiSuorituskielellinen with PakollisenTaiValinnaisenSuoritus

@Description("Ammatillisen tutkinnon osaa pienemmän kokonaisuuden tunnistetiedot")
case class AmmatillisenTutkinnonOsaaPienempiKokonaisuus(
  tunniste: PaikallinenKoodi,
  @Description("Opintokokonaisuuden kuvaus")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends PaikallinenKoulutusmoduuli with LaajuuttaEiValidoida

trait AmmatillisenTutkinnonOsanOsaAlue extends Koulutusmoduuli with LaajuuttaEiValidoida with Valinnaisuus

@Description("Paikallisen tutkinnon osan osa-alueen tunnistetiedot")
@Title("Paikallinen tutkinnon osan osa-alue")
case class PaikallinenAmmatillisenTutkinnonOsanOsaAlue(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan osa-alueen kuvaus")
  kuvaus: LocalizedString,
  @Description("Onko pakollinen tutkinnossa (true/false)")
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends AmmatillisenTutkinnonOsanOsaAlue with PaikallinenKoulutusmoduuli

@Description("Valtakunnallisen tutkinnon osan osa-alueen tunnistetiedot")
@Title("Valtakunnallinen tutkinnon osan osa-alue")
case class ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(
  @Description("Valtakunnallisen tutkinnon osan osa-alueen tunniste")
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  @Description("Onko pakollinen tutkinnossa (true/false)")
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue with KoodistostaLöytyväKoulutusmoduuli

@Title("Vieras tai toinen kotimainen kieli")
case class AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(
  @KoodistoKoodiarvo("VK")
  @KoodistoKoodiarvo("TK1")
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  @Discriminator
  kieli: Koodistokoodiviite,
  @Description("Onko pakollinen tutkinnossa (true/false)")
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue with KoodistostaLöytyväKoulutusmoduuli with Kieliaine {
  override def description(text: LocalizationRepository) = concat(nimi, ", ", kieli)
}

@Title("Äidinkieli")
case class AmmatillisenTutkinnonÄidinkieli(
  @KoodistoKoodiarvo("AI")
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("oppiaineaidinkielijakirjallisuus")
  @Discriminator
  kieli: Koodistokoodiviite,
  @Description("Onko pakollinen tutkinnossa (true/false)")
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue with KoodistostaLöytyväKoulutusmoduuli with Äidinkieli {
  override def description(text: LocalizationRepository) = concat(nimi, ", ", kieli)
}

@Description("Suoritukseen liittyvät lisätiedot, kuten mukautettu arviointi tai poikkeus arvioinnissa")
case class AmmatillisenTutkinnonOsanLisätieto(
  @Description("Lisätiedon tyyppi kooditettuna")
  @KoodistoUri("ammatillisentutkinnonosanlisatieto")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus siinä muodossa, kuin se näytetään todistuksella")
  kuvaus: LocalizedString
)

@Description("Näytön kuvaus")
case class Näyttö(
  @Description("Vapaamuotoinen kuvaus suoritetusta näytöstä")
  @MultiLineString(5)
  kuvaus: Option[LocalizedString],
  suorituspaikka: Option[NäytönSuorituspaikka],
  @Description("Näyttötilaisuuden ajankohta")
  suoritusaika: Option[NäytönSuoritusaika],
  @Description("Onko näyttö suoritettu työssäoppimisen yhteydessä (true/false)")
  @DefaultValue(false)
  @OnlyWhen("../../../suoritustapa/koodiarvo", "ops")
  työssäoppimisenYhteydessä: Boolean = false,
  @Description("Näytön arvioinnin lisätiedot")
  @FlattenInUI
  arviointi: Option[NäytönArviointi],
  @Description("Halutaanko näytöstä erillinen todistus. Puuttuva arvo tulkitaan siten, että halukkuutta ei tiedetä")
  haluaaTodistuksen: Option[Boolean] = None
)

@Description("Ammatillisen näytön suorituspaikka")
case class NäytönSuorituspaikka(
  @Description("Suorituspaikan tyyppi 1-numeroisella koodilla")
  @KoodistoUri("ammatillisennaytonsuorituspaikka")
  tunniste: Koodistokoodiviite,
  @Description("Vapaamuotoinen suorituspaikan kuvaus")
  kuvaus: LocalizedString
)

@Description("Näyttötilaisuuden ajankohta")
case class NäytönSuoritusaika(
  @Description("Näyttötilaisuuden alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: LocalDate,
  @Description("Näyttötilaisuuden päättymispäivämäärä. Muoto YYYY-MM-DD")
  loppu: LocalDate
)

@Description("Näytön arvioinnin lisätiedot")
case class NäytönArviointi (
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  arvioitsijat: Option[List[NäytönArvioitsija]] = None,
  @Tabular
  arviointikohteet: Option[List[NäytönArviointikohde]],
  @KoodistoUri("ammatillisennaytonarvioinnistapaattaneet")
  @Description("Arvioinnista päättäneet tahot, ilmaistuna 1-numeroisella koodilla")
  @MinItems(1)
  arvioinnistaPäättäneet: List[Koodistokoodiviite],
  @KoodistoUri("ammatillisennaytonarviointikeskusteluunosallistuneet")
  @Description("Arviointikeskusteluun osallistuneet tahot, ilmaistuna 1-numeroisella koodilla")
  @MinItems(1)
  arviointikeskusteluunOsallistuneet: List[Koodistokoodiviite],
  @Description("Jos näyttö on hylätty, kuvataan hylkäyksen perusteet tänne")
  hylkäyksenPeruste: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi

@Description("Näytön eri arviointikohteiden (Työprosessin hallinta jne) arvosanat")
case class NäytönArviointikohde(
  @Description("Arviointikohteen tunniste")
  @KoodistoUri("ammatillisennaytonarviointikohde")
  @Title("Arviointikohde")
  tunniste: Koodistokoodiviite,
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  @KoodistoUri("arviointiasteikkoammatillinen15")
  arvosana: Koodistokoodiviite
)

case class NäytönArvioitsija(
  @Representative
  nimi: String,
  @Description("Onko suorittanut näyttötutkintomestarikoulutuksen (true/false). Puuttuva arvo tulkitaan siten, että koulutuksen suorittamisesta ei ole tietoa")
  @Title("Näyttötutkintomestari")
  ntm: Option[Boolean]
) extends SuorituksenArvioitsija

@Description("Oppisopimuksen tiedot")
case class Oppisopimus(
  @FlattenInUI
  työnantaja: Yritys
)

trait Järjestämismuoto {
  @Discriminator
  def tunniste: Koodistokoodiviite
}

@Description("Järjestämismuoto ilman lisätietoja")
case class JärjestämismuotoIlmanLisätietoja(
  @Description("Koulutuksen järjestämismuodon tunniste")
  @KoodistoUri("jarjestamismuoto")
  @Representative
  tunniste: Koodistokoodiviite
) extends Järjestämismuoto

@Description("Koulutuksen järjestäminen oppisopimuskoulutuksena. Sisältää oppisopimuksen lisätiedot")
case class OppisopimuksellinenJärjestämismuoto(
  @Description("Koulutuksen järjestämismuodon tunniste")
  @KoodistoUri("jarjestamismuoto")
  @KoodistoKoodiarvo("20")
  tunniste: Koodistokoodiviite,
  @Discriminator
  @FlattenInUI
  oppisopimus: Oppisopimus
) extends Järjestämismuoto

trait OsaamisenHankkimistapa {
  @Discriminator
  def tunniste: Koodistokoodiviite
}

case class OsaamisenHankkimistapaIlmanLisätietoja(
  @Description("Koulutuksen järjestämismuodon tunniste")
  @KoodistoUri("osaamisenhankkimistapa")
  @Representative
  tunniste: Koodistokoodiviite
) extends OsaamisenHankkimistapa

case class OppisopimuksellinenOsaamisenHankkimistapa(
  @Description("Koulutuksen järjestämismuodon tunniste")
  @KoodistoUri("osaamisenhankkimistapa")
  @KoodistoKoodiarvo("oppisopimus")
  tunniste: Koodistokoodiviite,
  @Discriminator
  @FlattenInUI
  oppisopimus: Oppisopimus
) extends OsaamisenHankkimistapa

@Description("Jos kyseessä erityisopiskelija, jolle on tehty henkilökohtainen opetuksen järjestämistä koskeva suunnitelma (HOJKS), täytetään tämä tieto. Käytetään vain mikäli HOJKS-päätös on tehty ennen vuotta 2018. 2018 lähtien tieto välitetään erityinenTuki-rakenteen kautta. Rahoituksen laskennassa hyödynnettävä tieto.")
@Tooltip("Jos kyseessä erityisopiskelija, jolle on tehty henkilökohtainen opetuksen järjestämistä koskeva suunnitelma (HOJKS), täytetään tämä tieto. Käytetään vain mikäli HOJKS-päätös on tehty ennen vuotta 2018. 2018 lähtien tieto tallennetaan 'Erityinen tuki'-kenttään. Rahoituksen laskennassa hyödynnettävä tieto.")
@OksaUri("tmpOKSAID228", "erityisopiskelija")
case class Hojks(
  @Description("Tieto kertoo sen, suorittaako erityisopiskelija koulutusta omassa erityisryhmässään vai inklusiivisesti opetuksen mukana (erityisopiskelijan opetusryhmä-tieto, vain jos HOJKS-opiskelija).")
  @Description("Tieto kertoo sen, suorittaako erityisopiskelija koulutusta omassa erityisryhmässään vai inklusiivisesti opetuksen mukana. Valitse pudotusvalikosta oikea vaihtoehto.")
  @KoodistoUri("opetusryhma")
  opetusryhmä: Koodistokoodiviite,
  @Description("Alkamispäivämäärä. Muoto YYYY-MM-DD")
  @Tooltip("HOJKS:n voimassaolon alkamispäivämäärä.")
  alku: Option[LocalDate] = None,
  @Description("HOJKS:n voimassaolon loppupäivämäärä.")
  loppu: Option[LocalDate] = None
)

case class LaajuusOsaamispisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("6")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("6", Some(finnish("Osaamispistettä")), "opintojenlaajuusyksikko")
) extends Laajuus

@Description("Suoritettavan näyttötutkintoon valmistavan koulutuksen osan tiedot")
case class NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  @Description("Näyttötutkintoon valmistavan koulutuksen osan tunnistetiedot")
  koulutusmoduuli: NäyttötutkintoonValmistavanKoulutuksenOsa,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("nayttotutkintoonvalmistavankoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nayttotutkintoonvalmistavankoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends Vahvistukseton with MahdollisestiSuorituskielellinen with Arvioinniton

trait NäyttötutkintoonValmistavanKoulutuksenOsa extends Koulutusmoduuli with LaajuuttaEiValidoida

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
case class PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli with NäyttötutkintoonValmistavanKoulutuksenOsa with Laajuudeton

trait ValmentavaSuoritus extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with Suorituskielellinen {
  override def osasuoritukset: Option[List[ValmentavanKoulutuksenOsanSuoritus]] = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
@Title("VALMA-koulutuksen suoritus")
case class ValmaKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: ValmaKoulutus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[ValmaKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends ValmentavaSuoritus with AmmatillinenPäätasonSuoritus with Ryhmällinen

@Description("Suoritettavan VALMA-koulutuksen osan / osien tiedot")
@Title("VALMA-koulutuksen osan suoritus")
case class ValmaKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  @Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
  koulutusmoduuli: ValmaKoulutuksenOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  @KoodistoKoodiarvo("valmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valmakoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends ValmentavanKoulutuksenOsanSuoritus with MahdollisestiSuorituskielellinen

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen (VALMA) tunnistetiedot")
@Title("Valma-koulutus")
case class ValmaKoulutus(
  @KoodistoKoodiarvo("999901")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999901", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  laajuus: Option[LaajuusOsaamispisteissä] = None,
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with LaajuuttaEiValidoida

trait ValmaKoulutuksenOsa extends Koulutusmoduuli with LaajuuttaEiValidoida

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
@Title("Paikallinen Valma-koulutuksen osa")
case class PaikallinenValmaKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli with Valinnaisuus with ValmaKoulutuksenOsa

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
@Title("TELMA-koulutuksen suoritus")
case class TelmaKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: TelmaKoulutus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen osasuoritukset")
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[TelmaKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telma", koodistoUri = "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends ValmentavaSuoritus with AmmatillinenPäätasonSuoritus with Ryhmällinen

@Title("TELMA-koulutuksen osan suoritus")
@Description("Suoritettavan TELMA-koulutuksen osan tiedot")
case class TelmaKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  @Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen (TELMA) osan tunnistetiedot")
  koulutusmoduuli: TelmaKoulutuksenOsa,
  arviointi: Option[List[AmmatillinenArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telmakoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends ValmentavanKoulutuksenOsanSuoritus with MahdollisestiSuorituskielellinen

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen (TELMA) tunnistetiedot")
@Title("Telma-koulutus")
case class TelmaKoulutus(
  @KoodistoKoodiarvo("999903")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999903", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  laajuus: Option[LaajuusOsaamispisteissä] = None,
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with LaajuuttaEiValidoida

trait TelmaKoulutuksenOsa extends Koulutusmoduuli with LaajuuttaEiValidoida

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen osan tunnistiedot")
@Title("Paikallinen Telma-koulutuksen osa")
case class PaikallinenTelmaKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli with Valinnaisuus with TelmaKoulutuksenOsa

trait AmmatillinenKoodistostaLöytyväArviointi extends KoodistostaLöytyväArviointi with ArviointiPäivämäärällä {
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  @KoodistoUri("arviointiasteikkoammatillinen15")
  override def arvosana: Koodistokoodiviite
  override def arvioitsijat: Option[List[SuorituksenArvioitsija]]
  override def hyväksytty = arvosana.koodiarvo match {
    case "0" => false
    case "Hylätty" => false
    case _ => true
  }
}

case class AmmatillinenArviointi(
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None,
  kuvaus: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi with SanallinenArviointi

trait Tutkintonimikkeellinen {
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  def tutkintonimike: Option[List[Koodistokoodiviite]] = None
}

trait Osaamisalallinen {
  @Description("Tieto siitä mihin osaamisalaan/osaamisaloihin oppijan tutkinto liittyy")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  def osaamisala: Option[List[Osaamisalajakso]] = None
}
