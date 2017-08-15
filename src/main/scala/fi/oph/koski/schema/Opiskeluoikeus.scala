package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation._
import org.json4s.FieldSerializer

object Opiskeluoikeus {
  type Id = Int
  type Oid = String
  type Versionumero = Int
  val VERSIO_1 = 1
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
  @Description("Muoto YYYY-MM-DD")
  def alkamispäivä: Option[LocalDate]
  @Description("Muoto YYYY-MM-DD")
  def arvioituPäättymispäivä: Option[LocalDate]
  @Description("Muoto YYYY-MM-DD")
  def päättymispäivä: Option[LocalDate]
  @Description("Oppilaitos, jossa opinnot on suoritettu")
  def oppilaitos: Option[Oppilaitos]
  @Hidden
  def koulutustoimija: Option[Koulutustoimija]
  @Description("Opiskeluoikeuteen liittyvien tutkinto- ja muiden suoritusten tiedot")
  def suoritukset: List[PäätasonSuoritus]
  @Description("Opiskeluoikeuden tila, joka muodostuu opiskeluoikeusjaksoista.")
  def tila: OpiskeluoikeudenTila
  def luokka = {
    val vuosiluokkasuoritukset = suoritukset.collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
    vuosiluokkasuoritukset.sortBy(_.koulutusmoduuli.tunniste.koodiarvo).reverse.headOption.map(_.luokka)
  }
  def ryhmä = suoritukset.collect { case s: Ryhmällinen => s }.headOption.flatMap(_.ryhmä)
  def lisätiedot: Option[OpiskeluoikeudenLisätiedot]
  def omistajaOrganisaatio = oppilaitos
  def getOppilaitos: Oppilaitos = oppilaitos.getOrElse(throw new RuntimeException("Oppilaitos puuttuu"))
  def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

trait OpiskeluoikeudenLisätiedot

trait KoskeenTallennettavaOpiskeluoikeus extends Opiskeluoikeus {
  @MinItems(1)
  def suoritukset: List[PäätasonSuoritus]
  def withOidAndVersion(oid: Option[String], versionumero: Option[Int]): KoskeenTallennettavaOpiskeluoikeus
  def withVersion(version: Int) = this.withOidAndVersion(this.oid, Some(version))
  def withSuoritukset(suoritukset: List[PäätasonSuoritus]): KoskeenTallennettavaOpiskeluoikeus
  def withKoulutustoimija(koulutustoimija: Koulutustoimija): KoskeenTallennettavaOpiskeluoikeus
  def withOppilaitos(oppilaitos: Oppilaitos): KoskeenTallennettavaOpiskeluoikeus
}

@Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden tiedot. Nämä tiedot kertovat, että kyseessä on ns. ulkopuolisen sopimuskumppanin suoritustieto joka liittyy päävastuullisen koulutuksen järjestäjän luomaan opiskeluoikeuteen. Ks. tarkemmin https://confluence.csc.fi/pages/viewpage.action?pageId=70627182.")
case class SisältäväOpiskeluoikeus(
  @Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden oppilaitostieto.")
  oppilaitos: Oppilaitos,
  @Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden yksilöivä tunniste.")
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

trait KoskiOpiskeluoikeusjakso extends Opiskeluoikeusjakso {
  @KoodistoUri("koskiopiskeluoikeudentila")
  def tila: Koodistokoodiviite
  def opiskeluoikeusPäättynyt = List("valmistunut", "eronnut", "erotettu", "peruutettu").contains(tila.koodiarvo)
}


trait Alkupäivällinen {
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  def alku: LocalDate
}

trait Jakso extends Alkupäivällinen {
  @Description("Jakson loppupäivämäärä. Muoto YYYY-MM-DD")
  def loppu: Option[LocalDate]
}

trait Läsnäolojakso extends Alkupäivällinen {
  @Description("Läsnäolotila (läsnä, poissa...)")
  def tila: Koodistokoodiviite
}

@Title("Lähdejärjestelmä-ID")
@Description("Lähdejärjestelmän tunniste ja opiskeluoikeuden tunniste lähdejärjestelmässä. " +
  "Käytetään silloin, kun opiskeluoikeus on tuotu Koskeen tiedonsiirrolla ulkoisesta järjestelmästä, eli käytännössä oppilashallintojärjestelmästä.")
case class LähdejärjestelmäId(
  @Description("Opiskeluoikeuden paikallinen uniikki tunniste lähdejärjestelmässä. Tiedonsiirroissa tarpeellinen, jotta voidaan varmistaa päivitysten osuminen oikeaan opiskeluoikeuteen.")
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
