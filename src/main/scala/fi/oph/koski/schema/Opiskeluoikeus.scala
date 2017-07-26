package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation._

object Opiskeluoikeus {
  type Id = Int
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
    "Tietoja päivitettäessä Koski tunnistaa opiskeluoikeuden joko tämän id:n tai muiden kenttien (oppijaOid, organisaatio, opiskeluoikeuden tyyppi, paikallinen id) perusteella")
  @Hidden
  def id: Option[Int]
  @Description("Versionumero, joka generoidaan Koski-järjestelmässä. Tietoja syötettäessä kenttä ei ole pakollinen. " +
    "Ensimmäinen tallennettu versio saa versionumeron 1, jonka jälkeen jokainen päivitys aiheuttaa versionumeron noston yhdellä. " +
    "Jos tietoja päivitettäessä käytetään versionumeroa, pitää sen täsmätä viimeisimpään tallennettuun versioon. " +
    "Tällä menettelyllä esimerkiksi käyttöliittymässä varmistetaan, ettei tehdä päivityksiä vanhentuneeseen dataan.")
  @Hidden
  def versionumero: Option[Int]
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def alkamispäivä: Option[LocalDate]
  @Description("Opiskelijan opiskeluoikeuden arvioitu päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def arvioituPäättymispäivä: Option[LocalDate]
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def päättymispäivä: Option[LocalDate]
  @Description("Oppilaitos, jossa opinnot on suoritettu")
  def oppilaitos: Option[Oppilaitos]
  @Description("Koulutustoimija, käytännössä oppilaitoksen yliorganisaatio")
  @ReadOnly("Tiedon syötössä tietoa ei tarvita; organisaation tiedot haetaan Organisaatiopalvelusta")
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
  @Description("Nämä tiedot kertovat, että kyseessä on ns. ulkopuolisen sopimuskumppanin suoritustieto joka liittyy päävastuullisen koulutuksen järjestäjän luomaan opiskeluoikeuteen.")
  def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

trait OpiskeluoikeudenLisätiedot

trait KoskeenTallennettavaOpiskeluoikeus extends Opiskeluoikeus {
  @MinItems(1)
  def suoritukset: List[PäätasonSuoritus]
  def withIdAndVersion(id: Option[Int], versionumero: Option[Int]): KoskeenTallennettavaOpiskeluoikeus
  def withVersion(version: Int) = this.withIdAndVersion(this.id, Some(version))
  def withSuoritukset(suoritukset: List[PäätasonSuoritus]): KoskeenTallennettavaOpiskeluoikeus
  def withKoulutustoimija(koulutustoimija: Koulutustoimija): KoskeenTallennettavaOpiskeluoikeus
  def withOppilaitos(oppilaitos: Oppilaitos): KoskeenTallennettavaOpiskeluoikeus
}

@Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden tiedot")
case class SisältäväOpiskeluoikeus(
  @Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden oppilaitostieto")
  oppilaitos: Oppilaitos,
  @Description("Päävastuullisen koulutuksen järjestäjän luoman opiskeluoikeuden yksilöivä tunniste")
  id: Int
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
  @Description("Lähdejärjestelmän tunniste ja opiskeluoikeuden tunniste lähdejärjestelmässä. " +
    "Käytetään silloin, kun opiskeluoikeus on tuotu Koskeen tiedonsiirrolla ulkoisesta järjestelmästä, eli käytännössä oppilashallintojärjestelmästä.")
  @Hidden
  def lähdejärjestelmänId: Option[LähdejärjestelmäId]
}
