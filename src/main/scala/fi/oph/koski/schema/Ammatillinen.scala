package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedString.finnish
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class AmmatillinenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[Koulutustoimija] = None,
  alkamispäivä: Option[LocalDate] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: AmmatillinenOpiskeluoikeudenTila,
  @MinItems(1) @MaxItems(2)
  suoritukset: List[AmmatillinenPäätasonSuoritus],
  läsnäolotiedot: Option[YleisetLäsnäolotiedot] = None,
  lisätiedot: Option[AmmatillisenOpiskeluoikeudenLisätiedot] = None,
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinenkoulutus", "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[AmmatillinenPäätasonSuoritus]])
}

sealed trait AmmatillinenPäätasonSuoritus extends PäätasonSuoritus

case class AmmatillisenOpiskeluoikeudenLisätiedot(
  @Description("Jos kyseessä erityisopiskelija, jolle on tehty henkilökohtainen opetuksen järjestämistä koskeva suunnitelma (hojks), täytetään tämä tieto. Kentän puuttuminen tai null-arvo tulkitaan siten, että suunnitelmaa ei ole tehty.")
  hojks: Option[Hojks],
  oikeusMaksuttomaanAsuntolapaikkaan: Boolean = false,
  @Description("Opintoihin liittyvien ulkomaanjaksojen tiedot")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None
)

case class AmmatillinenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[AmmatillinenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

@Description("Sisältää myös tiedon opintojen rahoituksesta jaksoittain.")
case class AmmatillinenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: Koodistokoodiviite,
  @Description("Opintojen rahoitus")
  @KoodistoUri("opintojenrahoitus")
  opintojenRahoitus: Option[Koodistokoodiviite] = None
) extends KoskiOpiskeluoikeusjakso

case class NäyttötutkintoonValmistavanKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus = NäyttötutkintoonValmistavaKoulutus(),
  @Description("Tässä kentässä kuvataan sen tutkinnon tiedot, joihin valmistava koulutus tähtää")
  tutkinto: AmmatillinenTutkintoKoulutus,
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä mihin osaamisalaan/osaamisaloihin oppijan tutkinto liittyy")
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  osaamisala: Option[List[Koodistokoodiviite]] = None,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate],
  @Description("Suorituksen päättymispäivä. Muoto YYYY-MM-DD")
  val päättymispäivä: Option[LocalDate],
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Koulutuksen järjestämismuoto")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuoto: Option[Järjestämismuoto] = None,
  @Description("Valmistavan koulutuksen osat")
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus]] = None,
  @KoodistoKoodiarvo("nayttotutkintoonvalmistavakoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nayttotutkintoonvalmistavakoulutus", "suorituksentyyppi")
) extends AmmatillinenPäätasonSuoritus with Toimipisteellinen {
  def arviointi: Option[List[AmmatillinenArviointi]] = None
}

@Description("Näyttötutkintoon valmistavan koulutuksen tunnistetiedot")
case class NäyttötutkintoonValmistavaKoulutus(
  @KoodistoKoodiarvo("999904")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999904", "koulutus")
) extends Koulutus {
  def laajuus = None
}

case class AmmatillisenTutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  @Description("Tieto siitä mihin tutkintonimikkeeseen oppijan tutkinto liittyy")
  @KoodistoUri("tutkintonimikkeet")
  @OksaUri("tmpOKSAID588", "tutkintonimike")
  tutkintonimike: Option[List[Koodistokoodiviite]] = None,
  @Description("Tieto siitä mihin osaamisalaan/osaamisaloihin oppijan tutkinto liittyy")
  @KoodistoUri("osaamisala")
  @OksaUri(tunnus = "tmpOKSAID299", käsite = "osaamisala")
  osaamisala: Option[List[Koodistokoodiviite]] = None,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate] = None,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Tutkinnon suoritustapa (näyttö / ops). Ammatillisen perustutkinnon voi suorittaa joko opetussuunnitelmaperusteisesti tai näyttönä. Ammattitutkinnot ja erikoisammattitutkinnot suoritetaan aina näyttönä.")
  @OksaUri("tmpOKSAID141", "ammatillisen koulutuksen järjestämistapa")
  @KoodistoUri("ammatillisentutkinnonsuoritustapa")
  suoritustapa: Option[Koodistokoodiviite] = None,
  @Description("Koulutuksen järjestämismuoto")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuoto: Option[Järjestämismuoto] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  @KoodistoKoodiarvo("ammatillinentutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkinto", "suorituksentyyppi")
) extends AmmatillinenPäätasonSuoritus with Toimipisteellinen {
  def arviointi: Option[List[AmmatillinenArviointi]] = None
}

@Description("Oppija suorittaa yhtä tai useampaa tutkinnon osaa, eikä koko tutkintoa.")
case class AmmatillisenTutkinnonOsittainenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AmmatillinenTutkintoKoulutus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Koulutuksen järjestämismuoto")
  @OksaUri("tmpOKSAID140", "koulutuksen järjestämismuoto")
  järjestämismuoto: Option[Järjestämismuoto] = None,
  @Description("Ammatilliseen tutkintoon liittyvät tutkinnonosan suoritukset")
  @Title("Tutkinnon osat")
  override val osasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None,
  @KoodistoKoodiarvo("ammatillinentutkintoosittainen")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillinentutkintoosittainen", "suorituksentyyppi")
) extends AmmatillinenPäätasonSuoritus with Toimipisteellinen with VahvistuksetonSuoritus {
  def arviointi: Option[List[AmmatillinenArviointi]] = None
}

case class AmmatillisenTutkinnonOsanSuoritus(
  @Description("Suoritettavan tutkinnon osan tunnistetiedot")
  @Title("Tutkinnon osa")
  koulutusmoduuli: AmmatillisenTutkinnonOsa,
  @Description("Tutkinto, jonka rakenteeseen tutkinnon osa liittyy. Käytetään vain tapauksissa, joissa tutkinnon osa on poimittu toisesta tutkinnosta.")
  tutkinto: Option[AmmatillinenTutkintoKoulutus] = None,
  @Description("Oppilaitoksen toimipiste, jossa opinnot on suoritettu")
  @OksaUri("tmpOKSAID148", "koulutusorganisaation toimipiste")
  toimipiste: Option[OrganisaatioWithOid],
  tila: Koodistokoodiviite,
  arviointi: Option[List[AmmatillinenArviointi]] = None,
  vahvistus: Option[Henkilövahvistus] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  @Description("Jos tutkinnon osa on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  näyttö: Option[Näyttö] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Tutkinnon suoritukseen kuuluvat työssäoppimisjaksot")
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @KoodistoKoodiarvo("ammatillisentutkinnonosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ammatillisentutkinnonosa", koodistoUri = "suorituksentyyppi")
) extends Suoritus

@Description("Työssäoppimisjakson tiedot (aika, paikka, työtehtävät, laajuuss)")
case class Työssäoppimisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("kunta")
  @Description("Kunta, jossa työssäoppiminen on tapahtunut")
  paikkakunta: Koodistokoodiviite,
  @Description("Maa, jossa työssäoppiminen on tapahtunut")
  @KoodistoUri("maatjavaltiot2")
  maa: Koodistokoodiviite,
  @Description("Työtehtävien kuvaus")
  työtehtävät: LocalizedString,
  laajuus: LaajuusOsaamispisteissä
) extends Jakso

@Description("Ammatillisen tutkinnon tunnistetiedot")
case class AmmatillinenTutkintoKoulutus(
 tunniste: Koodistokoodiviite,
 perusteenDiaarinumero: Option[String]
) extends DiaarinumerollinenKoulutus {
  override def laajuus = None
  override def isTutkinto = true
}

sealed trait AmmatillisenTutkinnonOsa extends Koulutusmoduuli {
  def laajuus: Option[LaajuusOsaamispisteissä]
}

@Description("Opetussuunnitelmaan kuuluvan tutkinnon osan tunnistetiedot")
case class ValtakunnallinenTutkinnonOsa(
  @Description("Tutkinnon osan kansallinen koodi")
  @KoodistoUri("tutkinnonosat")
  tunniste: Koodistokoodiviite,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsa
  with KoodistostaLöytyväKoulutusmoduuli
  with Valinnaisuus
  with AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa
  with TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa

@Description("Paikallisen tutkinnon osan tunnistetiedot")
case class PaikallinenTutkinnonOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  pakollinen: Boolean,
  override val laajuus: Option[LaajuusOsaamispisteissä]
) extends AmmatillisenTutkinnonOsa with PaikallinenKoulutusmoduuli with Valinnaisuus

@Description("Suoritukseen liittyvät lisätiedot, kuten mukautettu arviointi tai poikkeus arvioinnissa")
case class AmmatillisenTutkinnonOsanLisätieto(
  @Description("Lisätiedon tyyppi kooditettuna")
  @KoodistoUri("ammatillisentutkinnonosanlisatieto")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus siinä muodossa, kuin se näytetään todistuksella")
  kuvaus: LocalizedString
)

trait AmmatillinenKoodistostaLöytyväArviointi extends KoodistostaLöytyväArviointi with ArviointiPäivämäärällä {
  override def hyväksytty = arvosana.koodiarvo match {
    case "0" => false
    case "Hylätty" => false
    case _ => true
  }
}

case class AmmatillinenArviointi(
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends AmmatillinenKoodistostaLöytyväArviointi

@Description("Näytön kuvaus")
case class Näyttö(
  @Description("Vapaamuotoinen kuvaus suoritetusta näytöstä")
  kuvaus: LocalizedString,
  suorituspaikka: NäytönSuorituspaikka,
  @Description("Näytön arvioinnin lisätiedot")
  arviointi: Option[NäytönArviointi],
  @Description("Onko näyttö suoritettu työssäoppimisen yhteydessä (true/false)")
  työssäoppimisenYhteydessä: Boolean = false
)

@Description("Ammatillisen näytön suorituspaikka")
case class NäytönSuorituspaikka(
  @Description("Suorituspaikan tyyppi 1-numeroisella koodilla")
  @KoodistoUri("ammatillisennaytonsuorituspaikka")
  tunniste: Koodistokoodiviite,
  @Description("Vapaamuotoinen suorituspaikan kuvaus")
  kuvaus: LocalizedString
)

case class NäytönArviointi (
  @Description("Näytön eri arviointikohteiden (Työprosessin hallinta jne) arvosanat.")
  arviointikohteet: Option[List[NäytönArviointikohde]],
  @KoodistoUri("ammatillisennaytonarvioinnistapaattaneet")
  @Description("Arvioinnista päättäneet tahot, ilmaistuna 1-numeroisella koodilla")
  arvioinnistaPäättäneet: Koodistokoodiviite,
  @KoodistoUri("ammatillisennaytonarviointikeskusteluunosallistuneet")
  @Description("Arviointikeskusteluun osallistuneet tahot, ilmaistuna 1-numeroisella koodilla")
  arviointikeskusteluunOsallistuneet: Koodistokoodiviite,
  @Description("Jos näyttö on hylätty, kuvataan hylkäyksen perusteet tänne.")
  hylkäyksenPeruste: Option[LocalizedString] = None
)

case class NäytönArviointikohde(
  @Description("Arviointikohteen tunniste")
  @KoodistoUri("ammatillisennaytonarviointikohde")
  tunniste: Koodistokoodiviite,
  @Description("Arvosana. Kullekin arviointiasteikolle löytyy oma koodistonsa")
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  arvosana: Koodistokoodiviite
)

@Description("Oppisopimuksen tiedot")
case class Oppisopimus(
  työnantaja: Yritys
)

trait Järjestämismuoto {
  def tunniste: Koodistokoodiviite
}

@Description("Järjestämismuoto ilman lisätietoja")
case class JärjestämismuotoIlmanLisätietoja(
  @KoodistoUri("jarjestamismuoto")
  @Representative
  tunniste: Koodistokoodiviite
) extends Järjestämismuoto

@Description("Koulutuksen järjestäminen oppisopimuskoulutuksena. Sisältää oppisopimuksen lisätiedot")
case class OppisopimuksellinenJärjestämismuoto(
  @KoodistoUri("jarjestamismuoto")
  @KoodistoKoodiarvo("20")
  tunniste: Koodistokoodiviite,
  oppisopimus: Oppisopimus
) extends Järjestämismuoto

@Description("Henkilökohtainen opetuksen järjestämistä koskeva suunnitelma, https://fi.wikipedia.org/wiki/HOJKS")
@OksaUri("tmpOKSAID228", "erityisopiskelija")
case class Hojks(
  @KoodistoUri("opetusryhma")
  opetusryhmä: Koodistokoodiviite,
  @KoodistoUri("ammatillisenerityisopetuksenperuste")
  peruste: Koodistokoodiviite
)

case class LaajuusOsaamispisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("6")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("6", Some(finnish("Osaamispistettä")), "opintojenlaajuusyksikko")
) extends Laajuus

case class NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  koulutusmoduuli: NäyttötutkintoonValmistavanKoulutuksenOsa,
  tila: Koodistokoodiviite,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Tutkinnon suoritukseen kuuluvat työssäoppimisjaksot")
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,

  @KoodistoKoodiarvo("nayttotutkintoonvalmistavankoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("nayttotutkintoonvalmistavankoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends VahvistuksetonSuoritus {
  override def osasuoritukset = None
  def arviointi = None
}

// TODO: myös koodistosta löytyvät tutkinnon osat (ei varsinaisesti tutkinnon osan suoritus, vaan siihen liittyvää sisältöä)

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
case class NäyttötutkintoonValmistavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli with AmmatillisenTutkinnonOsa {
  def laajuus = None
}

trait ValmentavaSuoritus extends Suoritus with Toimipisteellinen {
  def todistuksellaNäkyvätLisätiedot: Option[LocalizedString]
  def arviointi = None
  override def osasuoritukset: Option[List[ValmentavanKoulutuksenOsanSuoritus]] = None
}

@Description("Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)")
case class AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: AmmatilliseenPeruskoulutukseenValmentavaKoulutus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osasuoritukset")
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("valma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valma", koodistoUri = "suorituksentyyppi")
) extends ValmentavaSuoritus with AmmatillinenPäätasonSuoritus

case class AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  koulutusmoduuli: AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa,
  tila: Koodistokoodiviite,
  arviointi: Option[List[AmmatillinenArviointi]],
  vahvistus: Option[Henkilövahvistus] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Jos tutkinnon osa on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  näyttö: Option[Näyttö] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Tutkinnon suoritukseen kuuluvat työssäoppimisjaksot")
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @KoodistoKoodiarvo("valmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("valmakoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends ValmentavanKoulutuksenOsanSuoritus

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen (VALMA) tunnistetiedot")
case class AmmatilliseenPeruskoulutukseenValmentavaKoulutus(
  @KoodistoKoodiarvo("999901")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999901", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = None,
  laajuus: Option[Laajuus] = None
) extends DiaarinumerollinenKoulutus

trait AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa extends Koulutusmoduuli

@Description("Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot")
case class PaikallinenAmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli with Valinnaisuus with AmmatilliseenPeruskoulutukseenValmentavanKoulutuksenOsa

@Description("Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)")
case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: TyöhönJaItsenäiseenElämäänValmentavaKoulutus,
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Henkilövahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen osasuoritukset")
  @Title("Koulutuksen osat")
  override val osasuoritukset: Option[List[TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsanSuoritus]],
  @KoodistoKoodiarvo("telma")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telma", koodistoUri = "suorituksentyyppi")
) extends ValmentavaSuoritus with AmmatillinenPäätasonSuoritus

case class TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsanSuoritus(
  @Title("Koulutuksen osa")
  koulutusmoduuli: TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa,
  tila: Koodistokoodiviite,
  arviointi: Option[List[TelmaArviointi]],
  vahvistus: Option[Henkilövahvistus] = None,
  override val alkamispäivä: Option[LocalDate] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Jos koulutuksen osa on suoritettu osaamisen tunnustamisena, syötetään tänne osaamisen tunnustamiseen liittyvät lisätiedot")
  tunnustettu: Option[OsaamisenTunnustaminen] = None,
  @Description("Suoritukseen liittyvän näytön tiedot")
  näyttö: Option[Näyttö] = None,
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]] = None,
  @Description("Tutkinnon suoritukseen kuuluvat työssäoppimisjaksot")
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None,
  @KoodistoKoodiarvo("telmakoulutuksenosa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("telmakoulutuksenosa", koodistoUri = "suorituksentyyppi")
) extends ValmentavanKoulutuksenOsanSuoritus

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen (TELMA) tunnistetiedot")
case class TyöhönJaItsenäiseenElämäänValmentavaKoulutus(
  @KoodistoKoodiarvo("999903")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999903", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String] = None,
  laajuus: Option[Laajuus] = None
) extends DiaarinumerollinenKoulutus

trait TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa extends Koulutusmoduuli

@Description("Työhön ja itsenäiseen elämään valmentavan koulutuksen osan tunnistiedot")
case class PaikallinenTyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa(
  tunniste: PaikallinenKoodi,
  @Description("Tutkinnonosan kuvaus sisältäen ammattitaitovaatimukset")
  kuvaus: LocalizedString,
  laajuus: Option[LaajuusOsaamispisteissä],
  pakollinen: Boolean
) extends PaikallinenKoulutusmoduuli with Valinnaisuus with TyöhönJaItsenäiseenElämäänValmentavanKoulutuksenOsa

case class TelmaArviointi(
  @KoodistoUri("arviointiasteikkoammatillinenhyvaksyttyhylatty")
  @KoodistoUri("arviointiasteikkoammatillinent1k3")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None,
  kuvaus: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi with SanallinenArviointi