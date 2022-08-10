package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.koskiuser.Rooli
import fi.oph.scalaschema.annotation._
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
  @DefaultValue(false)
  @Description("Onko opiskeluoikeuteen liittyvä koulutus/tutkinto ostettu toiselta koulutustoimijalta (true/false). Jos kentän arvo on true, tällaiseen opiskeluoikeuteen tulisi olla linkitetty ainakin yksi toisen koulutustoimijan opiskeluoikeus.")
  @Tooltip("Valitse valintaruutu jos opiskeluoikeuteen liittyvä koulutus/tutkinto ostettu toiselta koulutustoimijalta. Jos valittu, tällaiseen opiskeluoikeuteen tulisi olla linkitetty ainakin yksi toisen koulutustoimijan opiskeluoikeus.")
  ostettu: Boolean = false,
  tila: AmmatillinenOpiskeluoikeudenTila,
  suoritukset: List[AmmatillinenPäätasonSuoritus],
  lisätiedot: Option[AmmatillisenOpiskeluoikeudenLisätiedot] = None,
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.ammatillinenkoulutus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa")
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
}

trait AmmatillinenPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus
  with Koulutussopimuksellinen
  with Suorituskielellinen

trait Työssäoppimisjaksollinen {
  @Description("Suoritukseen kuuluvien työssäoppimisjaksojen tiedot (aika, paikka, maa, työtehtävät, laajuus).")
  @Tooltip("Suoritukseen kuuluvien työssäoppimisjaksojen tiedot (aika, paikka, maa, työtehtävät, laajuus).")
  def työssäoppimisjaksot: Option[List[Työssäoppimisjakso]]
}

trait Koulutussopimuksellinen extends Työssäoppimisjaksollinen {
  @Description("Suoritukseen kuuluvien koulutussopimusjaksojen tiedot (aika, paikka, työtehtävät, laajuus).")
  @Tooltip("Suoritukseen kuuluvien koulutussopimusjaksojen tiedot (aika, paikka, työtehtävät, laajuus).")
  def koulutussopimukset: Option[List[Koulutussopimusjakso]]
}

@Description("Ammatillisen opiskeluoikeuden lisätiedot (mm. rahoituksessa käytettävät)")
case class AmmatillisenOpiskeluoikeudenLisätiedot(
  @Description("Onko opiskelijalla oikeus maksuttomaan asuntolapaikkaan (true / false)")
  @Tooltip("Valitse valintaruutu, jos opiskelijalla on oikeus maksuttomaan asuntolapaikkaan.")
  @DefaultValue(None)
  @RedundantData
  oikeusMaksuttomaanAsuntolapaikkaan: Option[Boolean] = None,
  @Description("Koulutuksen tarjoajan majoitus, huoneeseen muuttopäivä ja lähtöpäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Koulutuksen järjestäjän tarjoama(t) majoitusjakso(t). Huoneeseen muuttopäivä ja lähtöpäivä. Rahoituksen laskennassa käytettävä tieto.")
  majoitus: Option[List[Aikajakso]] = None,
  @Description("Sisäoppilaitosmuotoinen majoitus, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Sisäoppilaitosmuotoisen majoituksen aloituspäivä ja loppupäivä. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
  @Description("Vaativan erityisen tuen yhteydessä järjestettävä majoitus. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Vaativan erityisen tuen yhteydessä järjestettävä majoitus (aloitus- ja loppupäivä). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[Aikajakso]] = None,
  @Description("Tieto siitä että oppija on erityisopetuksessa, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä että oppija on erityisopetuksessa. Merkitään erityisopetuksen alku- ja loppupäivä. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityinenTuki: Option[List[Aikajakso]] = None,
  @Description("Tieto siitä että oppija on vaativan erityisen tuen erityisen tehtävän erityisen tuen piirissä (aloituspäivä ja loppupäivä). Lista alku-loppu päivämääräpareja. Oppilaitoksen opetusluvassa tulee olla myönnetty vaativan erityisen tuen tehtävä. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä että oppija on vaativan erityisen tuen erityisen tehtävän erityisen tuen piirissä (aloituspäivä ja loppupäivä). Voi olla useita erillisiä jaksoja. Oppilaitoksen opetusluvassa tulee olla myönnetty vaativan erityisen tuen tehtävä. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenErityinenTehtävä: Option[List[Aikajakso]] = None,
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  hojks: Option[Hojks],
  @Description("Osallistuuko oppija vaikeasti vammaisille järjestettyyn opetukseen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, osallistuuko oppija vaikeasti vammaisille järjestettyyn opetukseen (alku- ja loppupäivä). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @Title("Vaikeasti vammaisille järjestetty opetus")
  vaikeastiVammainen: Option[List[Aikajakso]] = None,
  @Description("Onko oppija vammainen ja hänellä on avustaja. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Onko oppija vammainen ja hänellä on avustaja (alku- ja loppupäivä). Voit olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  vammainenJaAvustaja: Option[List[Aikajakso]] = None,
  @Description("Kyseessä on osa-aikainen opiskelu. Lista alku-loppu päivämääräpareja. Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole osa-aikainen opiskelu. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Osa-aikaisuusjaksojen tiedot (jakson alku- ja loppupäivät sekä osa-aikaisuus prosenttilukuna). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto")
  @Title("Osa-aikaisuusjaksot")
  osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]] = None,
  @Description("Opiskeluvalmiuksia tukevat opinnot (Laki ammatillisesta koulutuksesta 531/2017 63 §) välitetään jaksoina (alku- ja loppupäivämäärä) kun kyseessä on päätoiminen opiskelu. Jaksojen alku- ja loppupäivämäärätietojen lisäksi välitetään opintojen vapaamuotoinen kuvaus.")
  @Tooltip("Opiskeluvalmiuksia tukevat opinnot (Laki ammatillisesta koulutuksesta 531/2017 63 §) tallennetaan jaksoina (alku- ja loppupäivämäärä) kun kyseessä on päätoiminen opiskelu. Jaksojen alku- ja loppupäivämäärätietojen lisäksi tallennetaan opintojen vapaamuotoinen kuvaus. Voi olla useita erillisiä jaksoja.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  opiskeluvalmiuksiaTukevatOpinnot: Option[List[OpiskeluvalmiuksiaTukevienOpintojenJakso]] = None,
  @Description("Kyseessä on henkilöstökoulutus (kyllä/ei). Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole henkilöstökoulutus. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Valitse valintaruutu, jos kyseessä on henkilöstökoulutus.")
  @DefaultValue(false)
  henkilöstökoulutus: Boolean = false,
  @Description("Kyseessä on vankilaopetus. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Tieto vankilaopetusjaksoista (alku- ja loppupäivämäärä). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vankilaopetuksessa: Option[List[Aikajakso]] = None,
  @Description("Onko kyseessä koulutusvientikoulutus (kyllä/ei). Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole koulutusvientikoulutus.")
  @Tooltip("Valitse valintaruutu, jos kyseessä on koulutusvientikoulutus.")
  @DefaultValue(false)
  koulutusvienti: Boolean = false,
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None
) extends OpiskeluoikeudenLisätiedot
  with Ulkomaajaksollinen
  with SisäoppilaitosmainenMajoitus
  with VaikeastiVammainen
  with MaksuttomuusTieto
  with OikeusmaksuttomaanAsuntolapaikkaanBooleanina

@Title("Osa-aikaisuusjakso")
@Description("Osa-aikaisuusjakson kesto ja suuruus")
case class OsaAikaisuusJakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @Description("Osa-aikaisuuden suuruus prosentteina. Yksi täysipäiväinen opiskelupäivä viikossa = 20.")
  @Tooltip("Osa-aikaisuuden suuruus prosentteina. Yksi täysipäiväinen opiskelupäivä viikossa = 20.")
  @MinValue(0)
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
) extends DateContaining {
  def contains(d: LocalDate): Boolean = !d.isBefore(alku) && !d.isAfter(loppu)
}

@Description("Ks. tarkemmin ammatillisen opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/pages/viewpage.action?pageId=190612822#id-1.1.Ammatillistenopiskeluoikeuksienl%C3%A4sn%C3%A4olotiedotjaopiskeluoikeudenrahoitusmuodontiedot-Opiskeluoikeudentilat)")
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
  @Tooltip("Suoritettava koulutus")
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus = NäyttötutkintoonValmistavaKoulutus(),
  @Description("Tässä kentässä kuvataan sen tutkinnon tiedot, johon valmistava koulutus tähtää")
  @Tooltip("Ammatillinen tutkinto, johon näyttötutkintoon valmistava koulutus liittyy, ja tutkinnon opetussuunnitelman perusteiden diaarinumero.")
  tutkinto: AmmatillinenTutkintoKoulutus,
  @Tooltip("Tutkintonimike/-nimikkeet, johon koulutus näyttötutkintoon valmistavaan koulutukseen liittyvä ammatillinen tutkinto johtaa.")
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Tooltip("Näyttötutkintoon valmistavaan koulutukseen liittyvässä ammatillisessa tutkinnossa suoritettavat osaamisalat. Voi olla useampia eri jaksoissa.")
  override val osaamisala: Option[List[Osaamisalajakso]] = None,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate],
  @Description("Suorituksen päättymispäivä. Muoto YYYY-MM-DD")
  @Tooltip("Suorituksen päättymispäivä.")
  päättymispäivä: Option[LocalDate],
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään rakennetta osaamisenHankkimistapa tämän sijaan.")
  @Tooltip("Koulutuksen järjestämismuoto jaksotietona (alku- ja loppupäivämäärä). Oppilaitosmuotoinen tai oppisopimuskoulutus. Voi olla useita erillisiä jaksoja. Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään kenttää 'Osaamisen hankkimistavat' tämän sijaan.")
  järjestämismuodot: Option[List[Järjestämismuotojakso]] = None,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  @Tooltip("Osaamisen hankkimistavat (oppisopimus, koulutussopimus, oppilaitosmuotoinen koulutus) eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus]] = None,
  @Tooltip("Todistuksella näkyvät lisätiedot. Esim. jos jokin valmistavan koulutuksen sisällöistä on valittu toisesta tutkinnosta.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("nayttotutkintoonvalmistavakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nayttotutkintoonvalmistavakoulutus", "suorituksentyyppi"),
  @Tooltip("Oppijan opetusryhmä")
  ryhmä: Option[String] = None
) extends AmmatillinenPäätasonSuoritus
  with Toimipisteellinen
  with Todistus
  with Arvioinniton
  with Ryhmällinen
  with Tutkintonimikkeellinen
  with Osaamisalallinen
  with Järjestämismuodollinen
  with OsaamisenHankkimistavallinen

@Description("Näyttötutkintoon valmistavan koulutuksen tunnistetiedot")
case class NäyttötutkintoonValmistavaKoulutus(
  @KoodistoKoodiarvo("999904")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999904", "koulutus"),
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Koulutus with Laajuudeton

@Description("Suoritettavan ammatillisen tutkinnon tiedot")
case class AmmatillisenTutkinnonSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava ammatillinen tutkinto ja tutkinnon opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  @Tooltip("Suoritetaanko tutkinto näyttötutkintona vai opetussuunnitelmaperustaisena koulutuksena.")
  suoritustapa: Koodistokoodiviite,
  @Tooltip("Tutkintonimike/-nimikkeet, johon koulutus johtaa.")
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Tooltip("Suoritettava osaamisala. Voi olla useampia eri jaksoissa.")
  override val osaamisala: Option[List[Osaamisalajakso]] = None,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Koulutuksen järjestämismuoto jaksotietona (alku- ja loppupäivämäärä). Oppilaitosmuotoinen tai oppisopimuskoulutus. Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään rakennetta osaamisenHankkimistapa tämän sijaan.")
  @Tooltip("Koulutuksen järjestämismuoto jaksotietona (alku- ja loppupäivämäärä). Oppilaitosmuotoinen tai oppisopimuskoulutus. Voi olla useita erillisiä jaksoja. Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään kenttää 'Osaamisen hankkimistavat' tämän sijaan.")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuodot: Option[List[Järjestämismuotojakso]] = None,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  @Tooltip("Osaamisen hankkimistavat (oppisopimus, koulutussopimus, oppilaitosmuotoinen koulutus) eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  @Tooltip("Todistuksella näkyvät lisätiedot. Esim. erikoistuminen, tutkintoon kuulumattoman eristyisosaamisen osoittaminen jne.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkinto", "suorituksentyyppi"),
  @Tooltip("Oppijan opetusryhmä")
  ryhmä: Option[String] = None,
  @Title("Painotettu keskiarvo")
  @Tooltip("Ammatillisen tutkinnon osaamispistein painotettu keskiarvo.")
  @OnlyWhen("suoritustapa/koodiarvo","reformi")
  @OnlyWhen("suoritustapa/koodiarvo","ops")
  @MinValue(1)
  @MaxValue(5)
  @Scale(2)
  keskiarvo: Option[Double] = None,
  @Title("Sisältää mukautettuja arvosanoja")
  @Tooltip("Keskiarvoon sisältyy mukautettuja arvosanoja")
  @OnlyWhen("suoritustapa/koodiarvo","reformi")
  @OnlyWhen("suoritustapa/koodiarvo","ops")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  keskiarvoSisältääMukautettujaArvosanoja: Option[Boolean] = None
) extends AmmatillisenTutkinnonOsittainenTaiKokoSuoritus
  with Todistus
  with Järjestämismuodollinen
  with OsaamisenHankkimistavallinen

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
  @Tooltip("Ammatillinen tutkinto, johon suoritettava tutkinnon osa kuuluu/suoritettavat tutkinnon osat kuuluvat.")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  @Tooltip("Suoritetaanko tutkinnon osa/tutkinnon osat näyttötutkintona vai opetussuunnitelmaperustaisena koulutuksena.")
  suoritustapa: Koodistokoodiviite,
  @Tooltip("Tutkintonimike/-nimikkeet, joiden suorittamiseen tutkinnon osat voivat johtaa.")
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Description("Onko kyse uuden tutkintonimikkeen suorituksesta liittyen aiemmin suoritettuun tutkintoon.")
  @Tooltip("Onko kyse uuden tutkintonimikkeen suorituksesta liittyen aiemmin suoritettuun tutkintoon.")
  @DefaultValue(false)
  toinenTutkintonimike: Boolean = false,
  @Tooltip("Suoritettava osaamisala. Voi olla useampia eri jaksoissa.")
  override val osaamisala: Option[List[Osaamisalajakso]] = None,
  @Description("Onko kyse uuden osaamisalan suorituksesta liittyen aiemmin suoritettuun tutkintoon.")
  @Tooltip("Onko kyse uuden osaamisalan suorituksesta liittyen aiemmin suoritettuun tutkintoon.")
  @DefaultValue(false)
  toinenOsaamisala: Boolean = false,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Koulutuksen järjestämismuoto. Oppilaitosmuotoinen tai - oppisopimuskoulutus. Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään rakennetta osaamisenHankkimistapa tämän sijaan.")
  @Tooltip("Koulutuksen järjestämismuoto jaksotietona (alku- ja loppupäivämäärä). Oppilaitosmuotoinen tai oppisopimuskoulutus. Voi olla useita erillisiä jaksoja. Mikäli kyseessä on ammatillisen reformin mukainen suoritus, käytetään kenttää 'Osaamisen hankkimistavat' tämän sijaan.")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuodot: Option[List[Järjestämismuotojakso]] = None,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  @Tooltip("Osaamisen hankkimistavat (oppisopimus, koulutussopimus, oppilaitosmuotoinen koulutus) eri ajanjaksoina. Reformin mukaisten suoritusten välittämisessä käytetään tätä kenttää järjestämismuodon sijaan.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[OsittaisenAmmatillisenTutkinnonOsanSuoritus]] = None,
  @Description("Kun kyseessä on toinen osaamisala tai tutkintonimike, viittaus aiempaan suoritukseen välitetään tässä.")
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi, kun kyseessä on toinen osaamisala tai tutkintonimike, viittaus aiempaan suoritukseen välitetään tässä.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkintoosittainen", "suorituksentyyppi"),
  @Tooltip("Oppijan opetusryhmä")
  ryhmä: Option[String] = None,
  @Title("Painotettu keskiarvo")
  @Tooltip("Ammatillisen tutkinnon osaamispistein painotettu keskiarvo.")
  @OnlyWhen("suoritustapa/koodiarvo","reformi")
  @OnlyWhen("suoritustapa/koodiarvo","ops")
  @MinValue(1)
  @MaxValue(5)
  @Scale(2)
  keskiarvo: Option[Double] = None,
  @Title("Sisältää mukautettuja arvosanoja")
  @Tooltip("Keskiarvoon sisältyy mukautettuja arvosanoja")
  @OnlyWhen("suoritustapa/koodiarvo","reformi")
  @OnlyWhen("suoritustapa/koodiarvo","ops")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  keskiarvoSisältääMukautettujaArvosanoja: Option[Boolean] = None
) extends AmmatillisenTutkinnonOsittainenTaiKokoSuoritus
  with Järjestämismuodollinen
  with OsaamisenHankkimistavallinen

trait AmmatillisenTutkinnonOsittainenTaiKokoSuoritus extends AmmatillinenPäätasonSuoritus
  with Toimipisteellinen
  with Arvioinniton
  with Ryhmällinen
  with Tutkintonimikkeellinen
  with Osaamisalallinen
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta
{
  def koulutusmoduuli: AmmatillinenTutkintoKoulutus
  @Description("Tutkinnon suoritustapa (näyttö / ops / reformi). Ammatillisen perustutkinnon voi suorittaa joko opetussuunnitelmaperusteisesti tai näyttönä. Ammatillisen reformin (531/2017) mukaiset suoritukset välitetään suoritustavalla reformi. ")
  @OksaUri("tmpOKSAID141", "ammatillisen koulutuksen järjestämistapa")
  @KoodistoUri("ammatillisentutkinnonsuoritustapa")
  @ReadOnly("Suoritustapaa ei tyypillisesti vaihdeta suorituksen luonnin jälkeen")
  def suoritustapa: Koodistokoodiviite
}

// Tätä traittia käytetään auttamaan lisätiedollisten osasuoritusten tunnistamista.
// Ammatillisessa opiskeluoikeudessa joudutaan tietosuojasyistä filtteröimään
// tietynlaiset lisätiedot pois. Katso FilterNonAnnotationableSensitiveData
trait AmmatillisenTutkinnonOsanLisätiedollinen extends Suoritus {
  @Tooltip("Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa. Sisältää lisätiedon tyypin sekä vapaamuotoisen kuvauksen.")
  @ComplexObject
  def lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]

  def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): Suoritus
}

trait TutkinnonOsanSuoritus extends Suoritus
  with MahdollisestiSuorituskielellinen
  with MahdollisestiToimipisteellinen
  with MahdollisestiTunnustettu
  with DuplikaatitSallittu
  with AmmatillisenTutkinnonOsanLisätiedollinen
{
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
  @Tooltip("Tiedot aiemmin hankitun osaamisen tunnustamisesta.")
  @ComplexObject
  def tunnustettu: Option[OsaamisenTunnustaminen]
  @Description("Suoritukseen liittyvän näytön tiedot")
  @Tooltip("Tutkinnon tai koulutuksen osan suoritukseen kuuluvan ammattiosaamisen näytön tiedot.")
  @ComplexObject
  def näyttö: Option[Näyttö]
  def suorituskieli: Option[Koodistokoodiviite]
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  def tyyppi: Koodistokoodiviite

  override def ryhmittelytekijä: Option[String] = tutkinnonOsanRyhmä.map(_.toString)
}

trait AmmatillisenTutkinnonOsanSuoritus extends TutkinnonOsanSuoritus {
  def toimipisteellä(toimipiste: OrganisaatioWithOid): AmmatillisenTutkinnonOsanSuoritus = lens[AmmatillisenTutkinnonOsanSuoritus].field[Option[OrganisaatioWithOid]]("toimipiste").set(this)(Some(toimipiste))
}

trait OsittaisenAmmatillisenTutkinnonOsanSuoritus extends TutkinnonOsanSuoritus {
  def toimipisteellä(toimipiste: OrganisaatioWithOid): OsittaisenAmmatillisenTutkinnonOsanSuoritus = lens[OsittaisenAmmatillisenTutkinnonOsanSuoritus].field[Option[OrganisaatioWithOid]]("toimipiste").set(this)(Some(toimipiste))
}

trait YhteisenTutkinnonOsanSuoritus extends Suoritus with AmmatillisenTutkinnonOsanLisätiedollinen

@Description("Ammatilliseen tutkintoon liittyvän yhteisen tutkinnonosan suoritus")
@Title("Yhteisen tutkinnon osan suoritus")
@OnlyWhen("../../tyyppi/koodiarvo", "ammatillinentutkintoosittainen") // Tarvitaan jotta osaamisen tunnustamisen suoritus deserialisoituu
case class YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(
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
  override val lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  näyttö: Option[Näyttö] = None,
  @Title("Osa-alueet")
  override val osasuoritukset: Option[List[YhteisenTutkinnonOsanOsaAlueenSuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi"),
) extends OsittaisenAmmatillisenTutkinnonOsanSuoritus
  with MahdollisestiToimipisteellinen
  with YhteisenTutkinnonOsanSuoritus {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =
    shapeless.lens[YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
}

@Description("Ammatilliseen tutkintoon liittyvän, muun kuin yhteisen tutkinnonosan suoritus")
@Title("Muun tutkinnon osan suoritus")
@OnlyWhen("../../tyyppi/koodiarvo", "ammatillinentutkintoosittainen") // Tarvitaan jotta osaamisen tunnustamisen suoritus deserialisoituu
case class MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(
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
) extends OsittaisenAmmatillisenTutkinnonOsanSuoritus
  with MahdollisestiToimipisteellinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =
    shapeless.lens[MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
}

@Description("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
@OnlyWhen("../../suoritustapa/koodiarvo", "reformi")
case class OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa,
  @Description("Tieto siitä mihin tutkinnon osan ryhmään osan suoritus (Ammatilliset tutkinnon osat, Yhteiset tutkinnon osat, Vapaavalintaiset tutkinnon osat, Tutkintoa yksilöllisesti laajentavat tutkinnon osat) kuuluu")
  @KoodistoKoodiarvo("1") // Ammatilliset tutkinnon osat
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite] = Some(Koodistokoodiviite("1", "ammatillisentutkinnonosanryhma")),
  override val osasuoritukset: Option[List[YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends JatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  with OsittaisenAmmatillisenTutkinnonOsanSuoritus {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus = {
    if (lisätiedot.toList.flatten.nonEmpty) {
      throw new InternalError("withLisätiedot-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla lisätietoja")
    }
    this
  }
}

@Description("Korkeakouluopintoja")
@Title("Korkeakouluopintoja")
@OnlyWhen("../../suoritustapa/koodiarvo", "reformi")
case class OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(
  koulutusmoduuli: KorkeakouluopinnotTutkinnonOsa,
  @Description("Tieto siitä mihin tutkinnon osan ryhmään osan suoritus (Ammatilliset tutkinnon osat, Yhteiset tutkinnon osat, Vapaavalintaiset tutkinnon osat, Tutkintoa yksilöllisesti laajentavat tutkinnon osat) kuuluu")
  @KoodistoKoodiarvo("1") // Ammatilliset tutkinnon osat
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite] = Some(Koodistokoodiviite("1", "ammatillisentutkinnonosanryhma")),
  override val osasuoritukset: Option[List[KorkeakouluopintojenSuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends KorkeakouluopintoSuoritus
  with OsittaisenAmmatillisenTutkinnonOsanSuoritus {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = {
    if (lisätiedot.toList.flatten.nonEmpty) {
      throw new InternalError("withLisätiedot-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla lisätietoja")
    }
    this
  }
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
) extends AmmatillisenTutkinnonOsanSuoritus
  with MahdollisestiToimipisteellinen
  with YhteisenTutkinnonOsanSuoritus {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): YhteisenAmmatillisenTutkinnonOsanSuoritus =
    shapeless.lens[YhteisenAmmatillisenTutkinnonOsanSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
}

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
) extends AmmatillisenTutkinnonOsanSuoritus
  with MahdollisestiToimipisteellinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): MuunAmmatillisenTutkinnonOsanSuoritus =
    shapeless.lens[MuunAmmatillisenTutkinnonOsanSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
}

@Description("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
@Title("Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja")
@OnlyWhen("../../suoritustapa/koodiarvo", "reformi")
case class AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa,
  @Description("Tieto siitä mihin tutkinnon osan ryhmään osan suoritus (Ammatilliset tutkinnon osat, Yhteiset tutkinnon osat, Vapaavalintaiset tutkinnon osat, Tutkintoa yksilöllisesti laajentavat tutkinnon osat) kuuluu")
  @KoodistoKoodiarvo("1") // Ammatilliset tutkinnon osat
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite] = Some(Koodistokoodiviite("1", "ammatillisentutkinnonosanryhma")),
  override val osasuoritukset: Option[List[YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends JatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  with AmmatillisenTutkinnonOsanSuoritus {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus = {
    if (lisätiedot.toList.flatten.nonEmpty) {
      throw new InternalError("withLisätiedot-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla lisätietoja")
    }
    this
  }
}

@Description("Korkeakouluopintoja")
@Title("Korkeakouluopintoja")
@OnlyWhen("../../suoritustapa/koodiarvo", "reformi")
case class AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(
  koulutusmoduuli: KorkeakouluopinnotTutkinnonOsa,
  @Description("Tieto siitä mihin tutkinnon osan ryhmään osan suoritus (Ammatilliset tutkinnon osat, Yhteiset tutkinnon osat, Vapaavalintaiset tutkinnon osat, Tutkintoa yksilöllisesti laajentavat tutkinnon osat) kuuluu")
  @KoodistoKoodiarvo("1") // Ammatilliset tutkinnon osat
  tutkinnonOsanRyhmä: Option[Koodistokoodiviite] = Some(Koodistokoodiviite("1", "ammatillisentutkinnonosanryhma")),
  override val osasuoritukset: Option[List[KorkeakouluopintojenSuoritus]] = None,
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends KorkeakouluopintoSuoritus
  with AmmatillisenTutkinnonOsanSuoritus {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = {
    if (lisätiedot.toList.flatten.nonEmpty) {
      throw new InternalError("withLisätiedot-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla lisätietoja")
    }
    this
  }
}

trait JatkoOpintovalmiuksiaTukevienOpintojenSuoritus extends ValinnanMahdollisuus

trait KorkeakouluopintoSuoritus extends ValinnanMahdollisuus

trait ValinnanMahdollisuus extends TutkinnonOsanSuoritus
  with Välisuoritus
  with Vahvistukseton
  with Toimipisteetön
{
  override def tutkinto: Option[AmmatillinenTutkintoKoulutus] = None
  override def näyttö: Option[Näyttö] = None
  override def toimipiste: Option[OrganisaatioWithOid] = None
  override def alkamispäivä: Option[LocalDate] = None
  override def tunnustettu: Option[OsaamisenTunnustaminen] = None
  override def lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None
  override def suorituskieli: Option[Koodistokoodiviite] = None
  override def vahvistus: Option[HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla] = None
}

case class JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa(
  @Description("Tutkinnon osan kansallinen koodi")
  @KoodistoUri("tutkinnonosatvalinnanmahdollisuus")
  @KoodistoKoodiarvo("1")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "1", koodistoUri = "tutkinnonosatvalinnanmahdollisuus"),
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends AmmatillisenTutkinnonOsa with KoodistostaLöytyväKoulutusmoduuli {
  override def pakollinen: Boolean = false
}

case class KorkeakouluopinnotTutkinnonOsa(
  @Description("Tutkinnon osan kansallinen koodi")
  @KoodistoUri("tutkinnonosatvalinnanmahdollisuus")
  @KoodistoKoodiarvo("2")
  tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "2", koodistoUri = "tutkinnonosatvalinnanmahdollisuus"),
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends AmmatillisenTutkinnonOsa with KoodistostaLöytyväKoulutusmoduuli {
  override def pakollinen: Boolean = false
}

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
  @Tooltip("Työssäoppimispaikan nimi")
  def työssäoppimispaikka: Option[LocalizedString]
  @KoodistoUri("kunta")
  @Description("Kunta, jossa työssäoppiminen on tapahtunut.")
  @Tooltip("Kunta, jossa työssäoppiminen on tapahtunut.")
  def paikkakunta: Koodistokoodiviite
  @Description("Maa, jossa työssäoppiminen on tapahtunut.")
  @Tooltip("Maa, jossa työssäoppiminen on tapahtunut.")
  @KoodistoUri("maatjavaltiot2")
  def maa: Koodistokoodiviite
  @Description("Työtehtävien kuvaus")
  @Tooltip("Työtehtävien vapaamuotoinen kuvaus")
  def työtehtävät: Option[LocalizedString]
}

case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[LocalizedString],
  paikkakunta: Koodistokoodiviite,
  maa: Koodistokoodiviite,
  työtehtävät: Option[LocalizedString],
  @Description("Työssäoppimisjakson laajuus osaamispisteissä.")
  @Tooltip("Työssäoppimisjakson laajuus osaamispisteissä.")
  laajuus: LaajuusOsaamispisteissä
) extends Oppimisjakso

case class Koulutussopimusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  työssäoppimispaikka: Option[LocalizedString],
  @Description("Työssäoppimispaikan Y-tunnus")
  @RegularExpression("^\\d{7}-\\d$")
  @Example("1234567-8")
  @Title("Työssäoppimispaikan Y-tunnus")
  työssäoppimispaikanYTunnus: Option[String],
  paikkakunta: Koodistokoodiviite,
  maa: Koodistokoodiviite,
  työtehtävät: Option[LocalizedString]
) extends Oppimisjakso

@Title("Ammatillinen tutkintokoulutus")
@Description("Ammatillisen tutkinnon tunnistetiedot. Ammatillisille koulutuksille on ePerusteet")
case class AmmatillinenTutkintoKoulutus(
  tunniste: Koodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  @Description("Tutkinnon perusteen nimi. Tiedon syötössä tietoa ei tarvita; tieto haetaan e-perusteet palvelusta.")
  perusteenNimi: Option[LocalizedString] = None,
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Laajuudeton with Tutkinto

sealed trait AmmatillisenTutkinnonOsa extends KoulutusmoduuliValinnainenLaajuus
  with LaajuuttaEiValidoida
{
  def laajuus: Option[LaajuusOsaamispisteissä]
  def pakollinen: Boolean
}

object AmmatillisenTutkinnonOsa {
  val reformiMuotoisenTutkinnonYhteisetOsat = List(
    // Yhteiset tutkinnon osat ennen 1.8.2022
    "400012", "400013", "400014",
    // Yhteiset tutkinnon osat 1.8.2022 alkaen
    "106727", "106728", "106729",
    // Koulutusvientikokeilut
    "600001", "600002",
    ).map(Koodistokoodiviite(_, "tutkinnonosat"))
  val opsMuotoisenTutkinnonYhteisetOsat = List("101053", "101054", "101055", "101056").map(Koodistokoodiviite(_, "tutkinnonosat"))
  val yhteisetTutkinnonOsat = reformiMuotoisenTutkinnonYhteisetOsat ::: opsMuotoisenTutkinnonYhteisetOsat
}

trait ValtakunnallinenTutkinnonOsa extends AmmatillisenTutkinnonOsa
  with KoodistostaLöytyväKoulutusmoduuli
  with Valinnaisuus
  with ValmaKoulutuksenOsa
  with TelmaKoulutuksenOsa
  with NäyttötutkintoonValmistavanKoulutuksenOsa
{
  @Description("Tutkinnon osan kansallinen koodi")
  @KoodistoUri("tutkinnonosat")
  def tunniste: Koodistokoodiviite
}

sealed trait MuuKuinYhteinenTutkinnonOsa extends AmmatillisenTutkinnonOsa

@Description("Yhteisen tutkinnon osan tunnistetiedot")
case class YhteinenTutkinnonOsa(
  @KoodistoKoodiarvo("101053")
  @KoodistoKoodiarvo("101054")
  @KoodistoKoodiarvo("101055")
  @KoodistoKoodiarvo("101056")
  @KoodistoKoodiarvo("106727")
  @KoodistoKoodiarvo("106728")
  @KoodistoKoodiarvo("106729")
  @KoodistoKoodiarvo("400012")
  @KoodistoKoodiarvo("400013")
  @KoodistoKoodiarvo("400014")
  @KoodistoKoodiarvo("600001")
  @KoodistoKoodiarvo("600002")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends ValtakunnallinenTutkinnonOsa

@Description("Opetussuunnitelmaan kuuluvan tutkinnon osan tunnistetiedot")
case class MuuValtakunnallinenTutkinnonOsa(
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä],
  @MultiLineString(5)
  kuvaus: Option[LocalizedString] = None
) extends ValtakunnallinenTutkinnonOsa with MuuKuinYhteinenTutkinnonOsa

@Description("Paikallisen tutkinnon osan tunnistetiedot")
case class PaikallinenTutkinnonOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  @Tooltip("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsa
  with PaikallinenKoulutusmoduuli
  with Valinnaisuus
  with MuuKuinYhteinenTutkinnonOsa

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
) extends Suoritus
  with Vahvistukseton
  with MahdollisestiSuorituskielellinen
  with MahdollisestiTunnustettu
  with AmmatillisenTutkinnonOsanLisätiedollinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus =
    shapeless.lens[AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
  override def osasuoritusLista: List[Suoritus] = List()
  override def withOsasuoritukset(oss: Option[List[Suoritus]]): Suoritus = {
    if (oss.toList.flatten.nonEmpty) {
      throw new InternalError("withOsasuoritukset-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla osasuorituksia")
    }
    this
  }
}

case class MuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
  koulutusmoduuli: PaikallinenOpintovalmiuksiaTukevaOpinto,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ammatillinenmuitaopintovalmiuksiatukeviaopintoja")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinenmuitaopintovalmiuksiatukeviaopintoja", "suorituksentyyppi")
) extends Suoritus
  with Vahvistukseton
  with MahdollisestiSuorituskielellinen
  with YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
  with AmmatillisenTutkinnonOsanLisätiedollinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): MuidenOpintovalmiuksiaTukevienOpintojenSuoritus =
    shapeless.lens[MuidenOpintovalmiuksiaTukevienOpintojenSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
  override def osasuoritusLista: List[Suoritus] = List()
  override def withOsasuoritukset(oss: Option[List[Suoritus]]): Suoritus = {
    if (oss.toList.flatten.nonEmpty) {
      throw new InternalError("withOsasuoritukset-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla osasuorituksia")
    }
    this
  }
}

@Title("Lukion oppiaineen tai lukion kurssin suoritus")
case class LukioOpintojenSuoritus(
  koulutusmoduuli: PaikallinenLukionOpinto,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ammatillinenlukionopintoja")
  tyyppi: Koodistokoodiviite
) extends Suoritus
  with Vahvistukseton
  with MahdollisestiSuorituskielellinen
  with YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
  with AmmatillisenTutkinnonOsanLisätiedollinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): LukioOpintojenSuoritus =
    shapeless.lens[LukioOpintojenSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
  override def osasuoritusLista: List[Suoritus] = List()
  override def withOsasuoritukset(oss: Option[List[Suoritus]]): Suoritus = {
    if (oss.toList.flatten.nonEmpty) {
      throw new InternalError("withOsasuoritukset-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla osasuorituksia")
    }
    this
  }
}

trait YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
  extends Suoritus
  with MahdollisestiTunnustettu

case class PaikallinenOpintovalmiuksiaTukevaOpinto(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends PaikallinenKoulutusmoduuliValinnainenLaajuus

case class PaikallinenLukionOpinto(
  tunniste: PaikallinenKoodi,
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä] = None,
  @Description("Tutkinnon perusteen diaarinumero. Ks. ePerusteet-palvelu.")
  @Tooltip("Tutkinnon perusteen diaarinumero.")
  @Title("Peruste")
  @ClassName("peruste")
  perusteenDiaarinumero: String
) extends PaikallinenKoulutusmoduuliValinnainenLaajuus

case class KorkeakouluopintojenSuoritus(
  @Title("Kokonaisuus")
  koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("ammatillinenkorkeakouluopintoja")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinenkorkeakouluopintoja", "suorituksentyyppi")
) extends Suoritus
  with Vahvistukseton
  with MahdollisestiSuorituskielellinen
  with MahdollisestiTunnustettu
  with AmmatillisenTutkinnonOsanLisätiedollinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): KorkeakouluopintojenSuoritus =
    shapeless.lens[KorkeakouluopintojenSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
  override def osasuoritusLista: List[Suoritus] = List()
  override def withOsasuoritukset(oss: Option[List[Suoritus]]): Suoritus = {
    if (oss.toList.flatten.nonEmpty) {
      throw new InternalError("withOsasuoritukset-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla osasuorituksia")
    }
    this
  }
}

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
  @ComplexObject
  @OnlyWhen("../../../../suoritustapa/koodiarvo","reformi")
  näyttö: Option[Näyttö] = None,
  @KoodistoKoodiarvo("ammatillisentutkinnonosanosaalue")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosanosaalue", "suorituksentyyppi")
) extends Suoritus
  with Vahvistukseton
  with MahdollisestiSuorituskielellinen
  with PakollisenTaiValinnaisenSuoritus
  with ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus
  with YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
  with TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus
  with MuuAmmatillinenOsasuoritus
  with AmmatillisenTutkinnonOsanLisätiedollinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): YhteisenTutkinnonOsanOsaAlueenSuoritus =
    shapeless.lens[YhteisenTutkinnonOsanOsaAlueenSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
  override def osasuoritusLista: List[Suoritus] = List()
  override def withOsasuoritukset(oss: Option[List[Suoritus]]): Suoritus = {
    if (oss.toList.flatten.nonEmpty) {
      throw new InternalError("withOsasuoritukset-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla osasuorituksia")
    }
    this
  }
}

@Description("Korkeakouluopintojen tunnistetiedot")
case class KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus(
  tunniste: PaikallinenKoodi,
  @Description("Opintokokonaisuuden kuvaus")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends PaikallinenKoulutusmoduuli
  with LaajuuttaEiValidoida

@Description("Ammatillisen tutkinnon osaa pienemmän kokonaisuuden tunnistetiedot")
case class AmmatillisenTutkinnonOsaaPienempiKokonaisuus(
  tunniste: PaikallinenKoodi,
  @Description("Opintokokonaisuuden kuvaus")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends PaikallinenKoulutusmoduuli
  with LaajuuttaEiValidoida

trait AmmatillisenTutkinnonOsanOsaAlue extends KoulutusmoduuliValinnainenLaajuus
  with LaajuuttaEiValidoida
  with Valinnaisuus

@Description("Paikallisen tutkinnon osan osa-alueen tunnistetiedot")
@Title("Paikallinen tutkinnon osan osa-alue")
case class PaikallinenAmmatillisenTutkinnonOsanOsaAlue(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan osa-alueen kuvaus")
  kuvaus: LocalizedString,
  @Description("Onko pakollinen tutkinnossa (true/false)")
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä] = None
) extends AmmatillisenTutkinnonOsanOsaAlue
  with PaikallinenKoulutusmoduuli

@Description("Valtakunnallisen tutkinnon osan osa-alueen tunnistetiedot")
@Title("Valtakunnallinen tutkinnon osan osa-alue")
case class ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(
  @Description("Valtakunnallisen tutkinnon osan osa-alueen tunniste")
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  @Description("Onko pakollinen tutkinnossa (true/false)")
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue
  with KoodistostaLöytyväKoulutusmoduuli

@Title("Vieras tai toinen kotimainen kieli")
case class AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(
  @KoodistoKoodiarvo("VK")
  @KoodistoKoodiarvo("TK1")
  @KoodistoKoodiarvo("TK2")
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  @Discriminator
  kieli: Koodistokoodiviite,
  @Description("Onko pakollinen tutkinnossa (true/false)")
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue
  with KoodistostaLöytyväKoulutusmoduuli
  with Kieliaine {
  override def description = kieliaineDescription
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
) extends AmmatillisenTutkinnonOsanOsaAlue
  with KoodistostaLöytyväKoulutusmoduuli
  with Äidinkieli {
  override def description = kieliaineDescription
}

@Title("Viestintä ja vuorovaikutus kielivalinnalla")
case class AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(
  @KoodistoKoodiarvo("VVTK")
  @KoodistoKoodiarvo("VVAI")
  @KoodistoKoodiarvo("VVAI22")
  @KoodistoKoodiarvo("VVVK")
  @KoodistoUri("ammatillisenoppiaineet")
  tunniste: Koodistokoodiviite,
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  @Discriminator
  kieli: Koodistokoodiviite,
  @Description("Onko pakollinen tutkinnossa (true/false)")
  pakollinen: Boolean,
  laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsanOsaAlue
  with KoodistostaLöytyväKoulutusmoduuli
  with Kieliaine {
  override def description = kieliaineDescription
}

@Description("Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa.")
case class AmmatillisenTutkinnonOsanLisätieto(
  @Description("Lisätiedon tyyppi kooditettuna")
  @KoodistoUri("ammatillisentutkinnonosanlisatieto")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus siinä muodossa, kuin se näytetään todistuksella")
  kuvaus: LocalizedString
)

@Description("Tutkinnon tai koulutuksen osan suoritukseen kuuluvan ammattiosaamisen näytön tiedot.")
case class Näyttö(
  @Description("Vapaamuotoinen kuvaus suoritetusta näytöstä")
  @Tooltip("Vapaamuotoinen kuvaus suoritetusta näytöstä")
  @MultiLineString(5)
  kuvaus: Option[LocalizedString],
  @Tooltip("Näytön suorituspaikka (suorituspaikan tyyppi ja nimi).")
  suorituspaikka: Option[NäytönSuorituspaikka],
  @Description("Näyttötilaisuuden ajankohta")
  suoritusaika: Option[NäytönSuoritusaika],
  @Description("Onko näyttö suoritettu työssäoppimisen yhteydessä (true/false)")
  @Tooltip("Onko näyttö suoritettu työssäoppimisen yhteydessä?")
  @DefaultValue(false)
  työssäoppimisenYhteydessä: Boolean = false,
  @Description("Näytön arvioinnin lisätiedot")
  @Tooltip("Näytön arviointitiedot (arvosana, arviointipäivä, arvioinnista päättäneet, arviointikeskusteluun osallistuneet)")
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
  @Tooltip("Näytön arvosana")
  arvosana: Koodistokoodiviite,
  @Tooltip("Näytön arviointipäivä")
  päivä: LocalDate,
  arvioitsijat: Option[List[NäytönArvioitsija]] = None,
  @Tabular
  arviointikohteet: Option[List[NäytönArviointikohde]],
  @KoodistoUri("ammatillisennaytonarvioinnistapaattaneet")
  @Description("Arvioinnista päättäneet tahot, ilmaistuna 1-numeroisella koodilla.")
  @Tooltip("Näytön arvioinnista päättäneet tahot.")
  arvioinnistaPäättäneet: Option[List[Koodistokoodiviite]],
  @KoodistoUri("ammatillisennaytonarviointikeskusteluunosallistuneet")
  @Description("Arviointikeskusteluun osallistuneet tahot, ilmaistuna 1-numeroisella koodilla.")
  @Tooltip("Arviointikeskusteluun osallistuneet tahot.")
  arviointikeskusteluunOsallistuneet: Option[List[Koodistokoodiviite]],
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
  työnantaja: Yritys,
  @Description("Onko oppisopimus purettu. Puuttuva arvo tulkitaan siten, että oppisopimusta ei olla purettu")
  oppisopimuksenPurkaminen: Option[OppisopimuksenPurkaminen] = None
)

case class OppisopimuksenPurkaminen(
  päivä: LocalDate,
  purettuKoeajalla: Boolean
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
) extends MahdollisestiAlkupäivällinenJakso {
  // TODO: Onko toteutus oikea alkupäivän puuttuessa?
  override def contains(d: LocalDate): Boolean = (alku.isEmpty || !d.isBefore(alku.get)) && (loppu.isEmpty || !d.isAfter(loppu.get))
}

@Description("Suoritettavan näyttötutkintoon valmistavan koulutuksen osan tiedot")
case class NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  @Description("Näyttötutkintoon valmistavan koulutuksen osan tunnistetiedot")
  koulutusmoduuli: NäyttötutkintoonValmistavanKoulutuksenOsa,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("nayttotutkintoonvalmistavankoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nayttotutkintoonvalmistavankoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends Vahvistukseton
  with MahdollisestiSuorituskielellinen
  with Arvioinniton
  with DuplikaatitSallittu

trait NäyttötutkintoonValmistavanKoulutuksenOsa extends KoulutusmoduuliValinnainenLaajuus
  with LaajuuttaEiValidoida

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
case class PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli
  with NäyttötutkintoonValmistavanKoulutuksenOsa
  with Laajuudeton

trait ValmentavaSuoritus extends KoskeenTallennettavaPäätasonSuoritus
  with Toimipisteellinen
  with Todistus
  with Arvioinniton
  with Suorituskielellinen
{
  override def osasuoritukset: Option[List[ValmentavanKoulutuksenOsanSuoritus]] = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
@Title("VALMA-koulutuksen suoritus")
case class ValmaKoulutuksenSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus")
  koulutusmoduuli: ValmaKoulutus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset todistuksella näkyvät lisätiedot.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus]],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", koodistoUri = "suorituksentyyppi"),
  @Tooltip("Oppijan opetusryhmä")
  ryhmä: Option[String] = None
) extends ValmentavaSuoritus
  with AmmatillinenPäätasonSuoritus
  with Ryhmällinen
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

trait ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus extends ValmentavanKoulutuksenOsanSuoritus
  with MahdollisestiSuorituskielellinen

@Description("Suoritettavan VALMA-koulutuksen osan / osien tiedot")
@Title("VALMA-koulutuksen osan suoritus")
case class ValmaKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  @Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
  koulutusmoduuli: ValmaKoulutuksenOsa,
  arviointi: Option[List[TelmaJaValmaArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Tooltip("Tiedot aiemmin hankitun osaamisen tunnustamisesta.")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  @Tooltip("Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa. Sisältää lisätiedon tyypin sekä vapaamuotoisen kuvauksen.")
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  @Tooltip("Tutkinnon tai koulutuksen osan suoritukseen kuuluvan ammattiosaamisen näytön tiedot.")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  @KoodistoKoodiarvo("valmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valmakoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus
  with AmmatillisenTutkinnonOsanLisätiedollinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): ValmaKoulutuksenOsanSuoritus =
    shapeless.lens[ValmaKoulutuksenOsanSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
  override def osasuoritusLista: List[Suoritus] = List()
  override def withOsasuoritukset(oss: Option[List[Suoritus]]): Suoritus = {
    if (oss.toList.flatten.nonEmpty) {
      throw new InternalError("withOsasuoritukset-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla osasuorituksia")
    }
    this
  }
}

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen (VALMA) tunnistetiedot")
@Title("Valma-koulutus")
case class ValmaKoulutus(
  @KoodistoKoodiarvo("999901")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999901", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  @Tooltip("Koulutuksen laajuus osaamispisteinä")
  laajuus: Option[LaajuusOsaamispisteissä] = None,
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus
  with LaajuuttaEiValidoida

trait ValmaKoulutuksenOsa extends KoulutusmoduuliValinnainenLaajuus
  with LaajuuttaEiValidoida

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
@Title("Paikallinen Valma-koulutuksen osa")
case class PaikallinenValmaKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Koulutuksen osan kuvaus sisältäen ammattitaitovaatimukset.")
  @Tooltip("Koulutuksen osan kuvaus sisältäen ammattitaitovaatimukset.")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli
  with Valinnaisuus
  with ValmaKoulutuksenOsa

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
@Title("TELMA-koulutuksen suoritus")
case class TelmaKoulutuksenSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus")
  koulutusmoduuli: TelmaKoulutus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset todistuksella näkyvät lisätiedot.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen osasuoritukset")
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[TelmaKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telma", koodistoUri = "suorituksentyyppi"),
  @Tooltip("Oppijan opetusryhmä")
  ryhmä: Option[String] = None
) extends ValmentavaSuoritus
  with AmmatillinenPäätasonSuoritus
  with Ryhmällinen
  with SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta

@Title("TELMA-koulutuksen osan suoritus")
@Description("Suoritettavan TELMA-koulutuksen osan tiedot")
case class TelmaKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  @Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen (TELMA) osan tunnistetiedot")
  koulutusmoduuli: TelmaKoulutuksenOsa,
  arviointi: Option[List[TelmaJaValmaArviointi]],
  vahvistus: Option[HenkilövahvistusValinnaisellaTittelillä] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Tooltip("Tiedot aiemmin hankitun osaamisen tunnustamisesta.")
  @ComplexObject
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  @Tooltip("Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa. Sisältää lisätiedon tyypin sekä vapaamuotoisen kuvauksen.")
  override val lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  @Tooltip("Tutkinnon tai koulutuksen osan suoritukseen kuuluvan ammattiosaamisen näytön tiedot.")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telmakoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends ValmentavanKoulutuksenOsanSuoritus
  with MahdollisestiSuorituskielellinen
  with AmmatillisenTutkinnonOsanLisätiedollinen {
  override def withLisätiedot(lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]): TelmaKoulutuksenOsanSuoritus =
    shapeless.lens[TelmaKoulutuksenOsanSuoritus].field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot").set(this)(lisätiedot)
  override def osasuoritusLista: List[Suoritus] = List()
  override def withOsasuoritukset(oss: Option[List[Suoritus]]): Suoritus = {
    if (oss.toList.flatten.nonEmpty) {
      throw new InternalError("withOsasuoritukset-metodia, joka palauttaa objektin muokkaamatta sitä, kutsuttiin listalla osasuorituksia")
    }
    this
  }
}

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen (TELMA) tunnistetiedot")
@Title("Telma-koulutus")
case class TelmaKoulutus(
  @KoodistoKoodiarvo("999903")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999903", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  laajuus: Option[LaajuusOsaamispisteissä] = None,
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus
  with LaajuuttaEiValidoida

trait TelmaKoulutuksenOsa extends KoulutusmoduuliValinnainenLaajuus
  with LaajuuttaEiValidoida

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen osan tunnistiedot")
@Title("Paikallinen Telma-koulutuksen osa")
case class PaikallinenTelmaKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Koulutuksen osan kuvaus sisältäen ammattitaitovaatimukset.")
  @Tooltip("Koulutuksen osan kuvaus sisältäen ammattitaitovaatimukset.")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli
  with Valinnaisuus
  with TelmaKoulutuksenOsa

trait AmmatillinenKoodistostaLöytyväArviointi extends KoodistostaLöytyväArviointi
  with ArviointiPäivämäärällä
{
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  @KoodistoUri("arviointiasteikkoammatillinen15")
  override def arvosana: Koodistokoodiviite
  override def arvioitsijat: Option[List[SuorituksenArvioitsija]]
  override def hyväksytty = AmmatillinenKoodistostaLöytyväArviointi.hyväksytty(arvosana)
}
object AmmatillinenKoodistostaLöytyväArviointi{
  def hyväksytty(arvosana: Koodistokoodiviite): Boolean = arvosana.koodiarvo match {
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
  @Hidden
  kuvaus: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi
  with SanallinenArviointi

case class TelmaJaValmaArviointi(
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None,
  kuvaus: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi
  with SanallinenArviointi

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


trait Järjestämismuodollinen {
  def järjestämismuodot: Option[List[Järjestämismuotojakso]]
}

trait OsaamisenHankkimistavallinen {
  def osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]]
}
