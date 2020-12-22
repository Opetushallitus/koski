package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation._
import mojave.Traversal

object Opiskeluoikeus {
  type Id = Int
  type Oid = String
  type Versionumero = Int
  val VERSIO_1 = 1

  def isValidOpiskeluoikeusOid(oid: String) = oid.matches("""^1\.2\.246\.562\.15\.\d{11}$""")

  def oppilaitosTraversal: Traversal[KoskeenTallennettavaOpiskeluoikeus, Oppilaitos] = {
    import mojave._
    traversal[KoskeenTallennettavaOpiskeluoikeus].field[Option[Oppilaitos]]("oppilaitos").items
  }

  def koulutustoimijaTraversal: Traversal[KoskeenTallennettavaOpiskeluoikeus, Koulutustoimija] = {
    import mojave._
    traversal[KoskeenTallennettavaOpiskeluoikeus].field[Option[Koulutustoimija]]("koulutustoimija").items
  }

  def toimipisteetTraversal: Traversal[KoskeenTallennettavaOpiskeluoikeus, OrganisaatioWithOid] = {
    import mojave._
    Suoritus.toimipisteetTraversal.compose(traversal[KoskeenTallennettavaOpiskeluoikeus].field[List[Suoritus]]("suoritukset").items)
  }
}

trait Opiskeluoikeus extends Lähdejärjestelmällinen with OrganisaatioonLiittyvä {
  @Description("Opiskeluoikeuden tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) liittyvät opiskeluoikeudet")
  @OksaUri("tmpOKSAID869", "koulutusmuoto (1)")
  @KoodistoUri("opiskeluoikeudentyyppi")
  @Hidden
  @Discriminator
  def tyyppi: Koodistokoodiviite
  @Description("Opiskeluoikeuden yksilöivä tunniste, joka generoidaan Koski-järjestelmässä. Tietoja syötettäessä kenttä ei ole pakollinen. " +
    "Tietoja päivitettäessä Koski tunnistaa opiskeluoikeuden joko tämän oid:n tai muiden kenttien (oppijaOid, organisaatio, opiskeluoikeuden tyyppi, paikallinen id) perusteella")
  @Hidden
  def oid: Option[String]
  @Description("Versionumero, joka generoidaan Koski-järjestelmässä. Tietoja syötettäessä kenttä ei ole pakollinen. " +
    "Ensimmäinen tallennettu versio saa versionumeron 1, jonka jälkeen jokainen päivitys aiheuttaa versionumeron noston yhdellä. " +
    "Jos tietoja päivitettäessä käytetään versionumeroa, pitää sen täsmätä viimeisimpään tallennettuun versioon. " +
    "Tällä menettelyllä esimerkiksi käyttöliittymässä varmistetaan, ettei tehdä päivityksiä vanhentuneeseen dataan.")
  @Hidden
  def versionumero: Option[Int]
  @Description("Muoto YYYY-MM-DD. Tiedon syötössä tietoa ei tarvita; tieto poimitaan tila-kentän ensimmäisestä opiskeluoikeusjaksosta.")
  @SyntheticProperty
  def alkamispäivä: Option[LocalDate] = this.tila.opiskeluoikeusjaksot.headOption.map(_.alku)
  @Description("Muoto YYYY-MM-DD")
  def arvioituPäättymispäivä: Option[LocalDate]
  @Description("Muoto YYYY-MM-DD. Tiedon syötössä tietoa ei tarvita; tieto poimitaan tila-kentän viimeisestä opiskeluoikeusjaksosta.")
  @SyntheticProperty
  def päättymispäivä: Option[LocalDate] = this.tila.opiskeluoikeusjaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(_.alku)
  @Description("Oppilaitos, jossa opinnot on suoritettu")
  def oppilaitos: Option[Oppilaitos]
  @Hidden
  def koulutustoimija: Option[Koulutustoimija]
  @Description("Opiskeluoikeuteen liittyvien tutkinto- ja muiden suoritusten tiedot")
  def suoritukset: List[PäätasonSuoritus]
  @Description("Opiskeluoikeuden tila, joka muodostuu opiskeluoikeusjaksoista")
  @Tooltip("Opiskeluoikeuden tila, joka muodostuu opiskeluoikeusjaksoista. Tilojen kuvaukset löytyvät [täältä](https://confluence.csc.fi/pages/viewpage.action?pageId=71953716#KOSKI-palveluuntallennettavattiedotjatiedonsiirto/tietojenmanuaalinentallentaminen-tilat).")
  def tila: OpiskeluoikeudenTila
  def luokka: Option[String] = {
    val vuosiluokkasuoritukset = suoritukset.collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
    vuosiluokkasuoritukset.sortBy(_.koulutusmoduuli.tunniste.koodiarvo).reverse.headOption.map(_.luokka)
  }
  def ryhmä: Option[String] = suoritukset.collectFirst { case s: Ryhmällinen => s }.flatMap(_.ryhmä)
  def lisätiedot: Option[OpiskeluoikeudenLisätiedot]
  def omistajaOrganisaatio: Option[Oppilaitos] = oppilaitos
  def getOppilaitos: Oppilaitos = oppilaitos.getOrElse(throw new RuntimeException("Oppilaitos puuttuu"))
  def getOppilaitosOrKoulutusToimija: OrganisaatioWithOid = oppilaitos.orElse(koulutustoimija).getOrElse(throw new RuntimeException("Oppilaitos ja koulutustoimija puuttuu: " + this))
  @Tooltip("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden tiedot. Nämä tiedot kertovat, että kyseessä on ns. ulkopuolisen sopimuskumppanin suoritustieto, joka liittyy päävastuullisen koulutuksen järjestäjän luomaan opiskeluoikeuteen. Ks. tarkemmin ohjeet ja käyttötapaukset [usein kysyttyjen kysymysten](https://confluence.csc.fi/pages/viewpage.action?pageId=72811652) kohdasta Milloin ja miten käytetään linkitystä eri organisaatioissa olevien opintosuoritusten välillä KOSKI-palvelussa?")
  def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus]
  def mitätöity: Boolean = tila.opiskeluoikeusjaksot.lastOption.exists(_.tila.koodiarvo == "mitatoity")
}

object OpiskeluoikeudenTyyppi {
  private var tyypit: Set[Koodistokoodiviite] = Set()

  val aikuistenperusopetus = apply("aikuistenperusopetus")
  val ammatillinenkoulutus = apply("ammatillinenkoulutus")
  val esiopetus = apply("esiopetus")
  val ibtutkinto = apply("ibtutkinto")
  val diatutkinto = apply("diatutkinto")
  val internationalschool = apply("internationalschool")
  val korkeakoulutus = apply("korkeakoulutus")
  val lukiokoulutus = apply("lukiokoulutus")
  val luva = apply("luva")
  val perusopetukseenvalmistavaopetus = apply("perusopetukseenvalmistavaopetus")
  val perusopetuksenlisaopetus = apply("perusopetuksenlisaopetus")
  val perusopetus = apply("perusopetus")
  val ylioppilastutkinto = apply("ylioppilastutkinto")
  val vapaansivistystyonkoulutus = apply("vapaansivistystyonkoulutus")

  private def apply(koodiarvo: String): Koodistokoodiviite = {
    val tyyppi = Koodistokoodiviite(koodiarvo, "opiskeluoikeudentyyppi")
    tyypit = tyypit + tyyppi
    tyyppi
  }

  def kaikkiTyypit: Set[Koodistokoodiviite] = tyypit
}

trait KoskeenTallennettavaOpiskeluoikeus extends Opiskeluoikeus {
  import mojave._
  @Hidden
  @ReadOnly("Aikaleima muodostetaan Koski-palvelimella tallennettaessa")
  def aikaleima: Option[LocalDateTime]
  @MinItems(1)
  def suoritukset: List[KoskeenTallennettavaPäätasonSuoritus]
  @ReadOnly("Muodostetaan Koski-palvelimella tallennettaessa")
  @Title("Opiskeluoikeuden organisaatiohistoria")
  def organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]]
  def withOidAndVersion(oid: Option[String], versionumero: Option[Int]): KoskeenTallennettavaOpiskeluoikeus = {
    val withOid = shapeless.lens[KoskeenTallennettavaOpiskeluoikeus].field[Option[String]]("oid").set(this)(oid)
    shapeless.lens[KoskeenTallennettavaOpiskeluoikeus].field[Option[Int]]("versionumero").set(withOid)(versionumero)
  }
  final def withSuoritukset(suoritukset: List[PäätasonSuoritus]): KoskeenTallennettavaOpiskeluoikeus = {
    shapeless.lens[KoskeenTallennettavaOpiskeluoikeus].field[List[PäätasonSuoritus]]("suoritukset").set(this)(suoritukset)
  }
  final def withHistoria(historia: Option[List[OpiskeluoikeudenOrganisaatiohistoria]]): KoskeenTallennettavaOpiskeluoikeus = {
    shapeless.lens[KoskeenTallennettavaOpiskeluoikeus].field[Option[List[OpiskeluoikeudenOrganisaatiohistoria]]]("organisaatiohistoria").set(this)(historia)
  }
  def withKoulutustoimija(koulutustoimija: Koulutustoimija): KoskeenTallennettavaOpiskeluoikeus
  def withOppilaitos(oppilaitos: Oppilaitos): KoskeenTallennettavaOpiskeluoikeus
  final def withTila(tila: OpiskeluoikeudenTila): KoskeenTallennettavaOpiskeluoikeus =
    shapeless.lens[KoskeenTallennettavaOpiskeluoikeus].field[OpiskeluoikeudenTila]("tila").set(this)(tila)
}

trait TukimuodollinenOpiskeluoikeus extends Opiskeluoikeus {
  def lisätiedot: Option[TukimuodollisetLisätiedot]
  def lisätiedotSisältääOsaAikaisenErityisopetuksen: Boolean = lisätiedot.exists(_.sisältääOsaAikaisenErityisopetuksen)
  def suoritusSisältääOsaAikaisenErityisopetuksen: Boolean =
    suoritukset.collect { case s: ErityisopetuksellinenPäätasonSuoritus => s }.exists(_.sisältääOsaAikaisenErityisopetuksen)
}


@Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden tiedot. Nämä tiedot kertovat, että kyseessä on ns. ulkopuolisen sopimuskumppanin suoritustieto, joka liittyy päävastuullisen koulutuksen järjestäjän luomaan opiskeluoikeuteen. Ks. tarkemmin https://confluence.csc.fi/pages/viewpage.action?pageId=70627182")
@Tooltip("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden tiedot. Nämä tiedot kertovat, että kyseessä on ns. ulkopuolisen sopimuskumppanin suoritustieto, joka liittyy päävastuullisen koulutuksen järjestäjän luomaan opiskeluoikeuteen. Ks. tarkemmin ohjeet ja käyttötapaukset [usein kysyttyjen kysymysten](https://confluence.csc.fi/pages/viewpage.action?pageId=72811652) kohdasta Milloin ja miten käytetään linkitystä eri organisaatioissa olevien opintosuoritusten välillä KOSKI-palvelussa?")
case class SisältäväOpiskeluoikeus(
  @Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden oppilaitostieto.")
  @Tooltip("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden oppilaitostieto.")
  oppilaitos: Oppilaitos,
  @Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden yksilöivä tunniste.")
  @Tooltip("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden yksilöivä tunniste.")
  oid: String
)

trait OpiskeluoikeudenTila {
  @Representative
  def opiskeluoikeusjaksot: List[Opiskeluoikeusjakso]
}

@Description("Opiskeluoikeuden tilahistoria (Läsnä, Eronnut, Valmistunut...) jaksoittain")
trait Opiskeluoikeusjakso extends Alkupäivällinen {
  @Description("Opiskeluoikeuden tila (Läsnä, Eronnut, Valmistunut...) jaksottain")
  def tila: Koodistokoodiviite
  def opiskeluoikeusPäättynyt: Boolean
}

object KoskiSuppeaOpiskeluoikeusjakso {
  def päätöstilat = List("valmistunut", "katsotaaneronneeksi")
}

trait KoskiSuppeaOpiskeluoikeusjakso extends Opiskeluoikeusjakso {
  @KoodistoUri("koskiopiskeluoikeudentila")
  @KoodistoKoodiarvo("katsotaaneronneeksi")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("valiaikaisestikeskeytynyt")
  @KoodistoKoodiarvo("valmistunut")
  def tila: Koodistokoodiviite
  def opiskeluoikeusPäättynyt = KoskiSuppeaOpiskeluoikeusjakso.päätöstilat.contains(tila.koodiarvo) || tila.koodiarvo == "mitatoity"
}

object KoskiOpiskeluoikeusjakso {
  def päätöstilat = List("valmistunut", "eronnut", "peruutettu", "katsotaaneronneeksi")
}

trait KoskiOpiskeluoikeusjakso extends KoskiSuppeaOpiskeluoikeusjakso {
  @KoodistoUri("koskiopiskeluoikeudentila")
  @KoodistoKoodiarvo("eronnut")
  @KoodistoKoodiarvo("peruutettu")
  def tila: Koodistokoodiviite
  override def opiskeluoikeusPäättynyt = KoskiOpiskeluoikeusjakso.päätöstilat.contains(tila.koodiarvo) || tila.koodiarvo == "mitatoity"
  def opintojenRahoitus: Option[Koodistokoodiviite] = None
}

trait Alkupäivällinen {
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  def alku: LocalDate
}

trait MahdollisestiAlkupäivällinenJakso extends DateContaining {
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  def alku: Option[LocalDate]
  @Description("Jakson loppupäivämäärä. Muoto YYYY-MM-DD")
  def loppu: Option[LocalDate]

  // TODO: Onko toteutus oikea alkupäivän puuttuessa?
  def contains(d: LocalDate): Boolean =
    (alku.isDefined && !d.isBefore(alku.get)) && (loppu.isEmpty || !d.isAfter(loppu.get))
}

trait DateContaining {
  def contains(date: LocalDate): Boolean
}

trait Jakso extends Alkupäivällinen with DateContaining {
  @Description("Jakson loppupäivämäärä. Muoto YYYY-MM-DD")
  def loppu: Option[LocalDate]

  def contains(d: LocalDate): Boolean = !d.isBefore(alku) && (loppu.isEmpty || !d.isAfter(loppu.get))

  def overlaps(other: Jakso): Boolean =
    contains(other.alku) || other.loppu.exists(contains) || other.contains(alku) || loppu.exists(other.contains)
}

@Description("Aikajakson pituus (alku- ja loppupäivämäärä)")
case class Aikajakso (
  alku: LocalDate,
  loppu: Option[LocalDate]
) extends Jakso

trait Läsnäolojakso extends Alkupäivällinen {
  @Description("Läsnäolotila (läsnä, poissa...)")
  def tila: Koodistokoodiviite
}

@Title("Lähdejärjestelmä-ID")
@Description("Lähdejärjestelmän tunniste ja opiskeluoikeuden tunniste lähdejärjestelmässä. " +
  "Käytetään silloin, kun opiskeluoikeus on tuotu Koskeen tiedonsiirrolla ulkoisesta järjestelmästä, eli käytännössä oppilashallintojärjestelmästä.")
case class LähdejärjestelmäId(
  @Description("Opiskeluoikeuden paikallinen uniikki tunniste lähdejärjestelmässä. Tiedonsiirroissa tarpeellinen, jotta voidaan varmistaa päivitysten osuminen oikeaan opiskeluoikeuteen")
  id: Option[String],
  @Description("Lähdejärjestelmän yksilöivä tunniste. Tällä tunnistetaan sen järjestelmän tyyppi, josta tiedot on tuotu Koskeen. " +
    "Yksittäisillä lähdejärjestelmäinstansseilla ei tarvitse olla omaa tunnistetta; tässä identifioidaan vain lähdejärjestelmän tyyppi " +
    "(esimerkiksi primus, peppi, winha...)")
  @KoodistoUri("lahdejarjestelma")
  lähdejärjestelmä: Koodistokoodiviite
)
trait Lähdejärjestelmällinen {
  @Hidden
  def lähdejärjestelmänId: Option[LähdejärjestelmäId]
}
