package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.log.Loggable
import fi.oph.scalaschema.annotation._

object Opiskeluoikeus {
  type Id = Int
  type Versionumero = Int
  val VERSIO_1 = 1
}

trait Opiskeluoikeus extends Loggable {
  @Description("Opiskeluoikeden tyyppi, jolla erotellaan eri koulutusmuotoihin (perusopetus, lukio, ammatillinen...) liittyvät opiskeluoikeudet")
  @OksaUri("tmpOKSAID869", "koulutusmuoto (1)")
  @KoodistoUri("opiskeluoikeudentyyppi")
  def tyyppi: Koodistokoodiviite
  @Description("Opiskeluoikeuden uniikki tunniste, joka generoidaan TOR-järjestelmässä. Tietoja syötettäessä kenttä ei ole pakollinen. " +
    "Tietoja päivitettäessä TOR tunnistaa opiskeluoikeuden joko tämän id:n tai muiden kenttien (oppijaOid, organisaatio, diaarinumero) perusteella")
  def id: Option[Int]
  @Description("Versionumero, joka generoidaan TOR-järjestelmässä. Tietoja syötettäessä kenttä ei ole pakollinen. " +
    "Ensimmäinen tallennettu versio saa versionumeron 1, jonka jälkeen jokainen päivitys aiheuttaa versionumeron noston yhdellä. " +
    "Jos tietoja päivitettäessä käytetään versionumeroa, pitää sen täsmätä viimeisimpään tallennettuun versioon. " +
    "Tällä menettelyllä esimerkiksi käyttöliittymässä varmistetaan, ettei tehdä päivityksiä vanhentuneeseen dataan.")
  def versionumero: Option[Int]
  @Description("Lähdejärjestelmän tunniste ja opiskeluoikeuden tunniste lähdejärjestelmässä. " +
    "Käytetään silloin, kun opiskeluoikeus on tuotu TOR:iin tiedonsiirrolla ulkoisesta järjestelmästä, eli käytännössä oppilashallintojärjestelmästä.")
  def lähdejärjestelmänId: Option[LähdejärjestelmäId]
  @Description("Opiskelijan opiskeluoikeuden alkamisaika joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def alkamispäivä: Option[LocalDate]
  @Description("Opiskelijan opiskeluoikeuden arvioitu päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def arvioituPäättymispäivä: Option[LocalDate]
  @Description("Opiskelijan opiskeluoikeuden päättymispäivä joko tutkintotavoitteisessa koulutuksessa tai tutkinnon osa tavoitteisessa koulutuksessa. Muoto YYYY-MM-DD")
  def päättymispäivä: Option[LocalDate]
  @Description("Oppilaitos, jossa opinnot on suoritettu")
  def oppilaitos: Oppilaitos
  @Description("Koulutustoimija, käytännössä oppilaitoksen yliorganisaatio")
  @ReadOnly("Tiedon syötössä tietoa ei tarvita; kuvaus haetaan Organisaatiopalvelusta")
  def koulutustoimija: Option[OrganisaatioWithOid]
  @Description("Opiskeluoikeuteen liittyvien (tutkinto-)suorituksien tiedot")
  def suoritukset: List[Suoritus]
  def tila: Option[OpiskeluoikeudenTila]
  def läsnäolotiedot: Option[Läsnäolotiedot]

  override def logString = id match {
    case None => "opiskeluoikeus"
    case Some(id) => "opiskeluoikeus " + id
  }

  def withIdAndVersion(id: Option[Int], versionumero: Option[Int]): Opiskeluoikeus
  def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid): Opiskeluoikeus
}

trait OpiskeluoikeudenTila {
  @Description("Opiskeluoikeuden tilahistoria (aktiivinen, keskeyttänyt, päättynyt...) jaksoittain. Sisältää myös tiedon opintojen rahoituksesta jaksoittain.")
  def opiskeluoikeusjaksot: List[Opiskeluoikeusjakso]
}

trait Opiskeluoikeusjakso extends Jakso {
  def alku: LocalDate
  def loppu: Option[LocalDate]
  @Description("Opiskeluoikeuden tila (aktiivinen, keskeyttänyt, päättynyt...)")
  def tila: Koodistokoodiviite
}

trait Läsnäolotiedot {
  @Description("Läsnä- ja poissaolojaksot päivämääräväleinä.")
  def läsnäolojaksot: List[Läsnäolojakso]
}

trait Jakso {
  def alku: LocalDate
  def loppu: Option[LocalDate]
}

trait Läsnäolojakso extends Jakso {
  def alku: LocalDate
  def loppu: Option[LocalDate]
  @Description("Läsnäolotila (läsnä, poissa...)")
  def tila: Koodistokoodiviite
}

case class LähdejärjestelmäId(
  @Description("Opiskeluoikeuden paikallinen uniikki tunniste lähdejärjestelmässä. Tiedonsiirroissa tarpeellinen, jotta voidaan varmistaa päivitysten osuminen oikeaan opiskeluoikeuteen.")
  id: String,
  @Description("Lähdejärjestelmän yksilöivä tunniste. Tällä tunnistetaan järjestelmä, josta tiedot on tuotu TOR:iin. " +
    "Kullakin erillisellä tietojärjestelmäinstanssilla tulisi olla oma tunniste. " +
    "Jos siis oppilaitoksella on oma tietojärjestelmäinstanssi, tulee myös tällä instanssilla olla uniikki tunniste.")
  @KoodistoUri("lahdejarjestelma")
  lähdejärjestelmä: Koodistokoodiviite
)