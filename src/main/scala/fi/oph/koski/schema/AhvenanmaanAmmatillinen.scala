package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._

// Ahvenanmaan ammatillisen koulutuksen skeema (TOR-2837), haarautettu Ammatillinen.scala:sta
// tietomalliluonnoksen mukaan: https://wiki.eduuni.fi/pages/viewpage.action?pageId=854141212
// Wikissä punaisella merkityt kentät on pudotettu, oranssit (vielä auki) pidetty ennallaan.
//
// - Päätason suorituksilla on omat luokat ja suorituksentyypit: manner-Suomen koodiarvo samalla
//   rakenteella tekisi tunnustettu.osaaminen-kentän deserialisoinnista monitulkintaisen
//   (TooManyMatchingCasesException). Wikissä omien koodien tarve on vielä auki.
// - Osasuoritukset ovat manner-Suomen luokkia: tutkinnon suorituksella samat kuin
//   AmmatillisenTutkinnonSuorituksella, osa/osia-suorituksella samat kuin
//   AmmatillisenTutkinnonOsittainenSuorituksella. Osa/osia-osasuoritusten @OnlyWhen-ehdot
//   sallivat siksi myös päätason tyypin ahvenanmaanammatillinentutkintoosittainen. Wikin
//   pudottamat näytön kentät (työssäoppimisenYhteydessä, haluaaTodistuksen) jäävät rakenteeseen,
//   mutta ne on piilotettu reformin mukaisilta suorituksilta.
// - Manner-Suomen ammatillisia päätason traitteja (AmmatillinenPäätasonSuoritus,
//   OppivelvollisuudenSuorittamiseenKelpaava ym.) ei käytetä, koska ne vetäisivät suoritukset
//   manner-Suomen validointeihin ja tietoluovutuksiin. Osasuorituksia käsittelevät manner-Suomen
//   validoinnit ja tietoluovutukset rajautuvat pois samasta syystä: ne lähtevät manner-Suomen
//   opiskeluoikeus- tai päätason luokista.
//
// Tekemättä ennen kuin tyypin voi tallentaa:
// - OpiskeluoikeudenTyyppi-rekisteröinti, koodistot (opiskeluoikeudentyyppi, suorituksentyyppi),
//   käyttöoikeudet, validoinnit (wiki: manner-Suomen validoinnit soveltuvin osin), raportointikanta.
//
// Osasuoritusten on toteutettava AmmatillisenTutkinnonOsanLisätiedollinen, jotta
// FilterNonAnnotationableSensitiveData piilottaa mukautetun arvioinnin lisätiedot.

@Description("Ahvenanmaan ammatillisen koulutuksen opiskeluoikeus")
case class AhvenanmaanAmmatillinenOpiskeluoikeus(
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
  tila: AhvenanmaanAmmatillinenOpiskeluoikeudenTila,
  suoritukset: List[AhvenanmaanAmmatillinenPäätasonSuoritus],
  lisätiedot: Option[AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot] = None,
  @KoodistoKoodiarvo("ahvenanmaanammatillinenkoulutus")
  tyyppi: Koodistokoodiviite =
    Koodistokoodiviite("ahvenanmaanammatillinenkoulutus", koodistoUri = "opiskeluoikeudentyyppi"),
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None,
  lähdejärjestelmäkytkentäPurettu: Option[LähdejärjestelmäkytkennänPurkaminen] = None,
) extends KoskeenTallennettavaOpiskeluoikeus {
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa")
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) =
    this.copy(koulutustoimija = Some(koulutustoimija))
}

case class AhvenanmaanAmmatillisenOpiskeluoikeudenLisätiedot(
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @Description("Onko kyseessä koulutusvientikoulutus (kyllä/ei). Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole koulutusvientikoulutus.")
  @Tooltip("Valitse valintaruutu, jos kyseessä on koulutusvientikoulutus.")
  @DefaultValue(false)
  koulutusvienti: Boolean = false,
  @Description("Onko opiskeluoikeuden päättymisen syy siirtyminen tutkinnon uusiin perusteisiin.")
  siirtynytUusiinTutkinnonPerusteisiin: Option[Boolean] = None,
) extends OpiskeluoikeudenLisätiedot
  with Ulkomaanjaksollinen

case class AhvenanmaanAmmatillinenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AhvenanmaanAmmatillinenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class AhvenanmaanAmmatillinenOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("katsotaaneronneeksi")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("peruutettu")
  @KoodistoKoodiarvo("valiaikaisestikeskeytynyt")
  @KoodistoKoodiarvo("valmistunut")
  tila: Koodistokoodiviite
) extends KoskiOpiskeluoikeusjakso

// ---------- Päätason suoritukset ----------

sealed trait AhvenanmaanAmmatillinenPäätasonSuoritus extends KoskeenTallennettavaPäätasonSuoritus
  with Toimipisteellinen
  with Suorituskielellinen
  with Arvioinniton
  with Ryhmällinen
  with Tutkintonimikkeellinen
  with Osaamisalallinen
  with OsaamisenHankkimistavallinen
  with Koulutussopimuksellinen
{
  def koulutusmoduuli: AmmatillinenTutkintoKoulutus
  @Description("Tutkinnon suoritustapa. Ahvenanmaan ammatillisessa koulutuksessa aina reformi.")
  @OksaUri("tmpOKSAID141", "ammatillisen koulutuksen järjestämistapa")
  @KoodistoUri("ammatillisentutkinnonsuoritustapa")
  @KoodistoKoodiarvo("reformi")
  @ReadOnly("Suoritustapaa ei tyypillisesti vaihdeta suorituksen luonnin jälkeen")
  def suoritustapa: Koodistokoodiviite
  def vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla]
  @Tooltip("Oppijan opetusryhmä")
  def ryhmä: Option[String]
}

@Title("Ammatillisen tutkinnon suoritus")
@Description("Suoritettavan ammatillisen tutkinnon tiedot")
case class AhvenanmaanAmmatillisenTutkinnonSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava ammatillinen tutkinto ja tutkinnon opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  suoritustapa: Koodistokoodiviite = Koodistokoodiviite("reformi", "ammatillisentutkinnonsuoritustapa"),
  @Tooltip("Tutkintonimike/-nimikkeet, johon koulutus johtaa.")
  override val tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Tooltip("Suoritettava osaamisala. Voi olla useampia eri jaksoissa.")
  override val osaamisala: Option[List[Osaamisalajakso]] = None,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate] = None,
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  @Tooltip("Todistuksella näkyvät lisätiedot. Esim. erikoistuminen, tutkintoon kuulumattoman eristyisosaamisen osoittaminen jne.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ahvenanmaanammatillinentutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ahvenanmaanammatillinentutkinto", "suorituksentyyppi"),
  ryhmä: Option[String] = None,
  @Title("Painotettu keskiarvo")
  @Tooltip("Ammatillisen tutkinnon osaamispistein painotettu keskiarvo.")
  @MinValue(1)
  @MaxValue(5)
  @Scale(2)
  keskiarvo: Option[Double] = None,
  @Title("Sisältää mukautettuja arvosanoja")
  @Tooltip("Keskiarvoon sisältyy mukautettuja arvosanoja")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.SUORITUSJAKO_KATSELIJA))
  keskiarvoSisältääMukautettujaArvosanoja: Option[Boolean] = None
) extends AhvenanmaanAmmatillinenPäätasonSuoritus
  with Todistus

@Title("Ammatillisen tutkinnon osa/osia")
@Description("Oppija suorittaa yhtä tai useampaa tutkinnon osaa, eikä koko tutkintoa. Mikäli opiskelija suorittaa toista osaamisalaa tai tutkintonimikettä erillisessä opiskeluoikeudessa, välitään tieto tällöin tämän rakenteen kautta")
case class AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus(
  @Title("Koulutus")
  @Tooltip("Ammatillinen tutkinto, johon suoritettava tutkinnon osa kuuluu/suoritettavat tutkinnon osat kuuluvat.")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  suoritustapa: Koodistokoodiviite = Koodistokoodiviite("reformi", "ammatillisentutkinnonsuoritustapa"),
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
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[OsittaisenAmmatillisenTutkinnonOsanSuoritus]] = None,
  @Description("Kun kyseessä on toinen osaamisala tai tutkintonimike, viittaus aiempaan suoritukseen välitetään tässä.")
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi, kun kyseessä on toinen osaamisala tai tutkintonimike, viittaus aiempaan suoritukseen välitetään tässä.")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("ahvenanmaanammatillinentutkintoosittainen")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ahvenanmaanammatillinentutkintoosittainen", "suorituksentyyppi"),
  ryhmä: Option[String] = None,
  @Title("Painotettu keskiarvo")
  @Tooltip("Ammatillisen tutkinnon osaamispistein painotettu keskiarvo.")
  @MinValue(1)
  @MaxValue(5)
  @Scale(2)
  keskiarvo: Option[Double] = None,
  @Title("Sisältää mukautettuja arvosanoja")
  @Tooltip("Keskiarvoon sisältyy mukautettuja arvosanoja")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.SUORITUSJAKO_KATSELIJA))
  keskiarvoSisältääMukautettujaArvosanoja: Option[Boolean] = None,
  @Title("Korotetun suorituksen alkuperäinen opiskeluoikeus")
  @Description("Korotetun suorituksen alkuperäinen opiskeluoikeus")
  korotettuOpiskeluoikeusOid: Option[String] = None,
  @Title("Korotettu painotettu keskiarvo")
  @Tooltip("Ammatillisen tutkinnon osaamispistein painotettu korotettu keskiarvo.")
  @MinValue(1)
  @MaxValue(5)
  @Scale(2)
  korotettuKeskiarvo: Option[Double] = None,
  @Title("Korotus sisältää mukautettuja arvosanoja")
  @Tooltip("Korotettuun keskiarvoon sisältyy mukautettuja arvosanoja")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.SUORITUSJAKO_KATSELIJA))
  korotettuKeskiarvoSisältääMukautettujaArvosanoja: Option[Boolean] = None
) extends AhvenanmaanAmmatillinenPäätasonSuoritus
