package fi.oph.koski.organisaatio

import java.lang.System.currentTimeMillis
import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.cache._
import fi.oph.koski.date.DateOrdering
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema.{Koodistokoodiviite, Koulutustoimija, Oppilaitos, OrganisaatioWithOid}

import scala.concurrent.duration._
trait OrganisaatioRepository {
  /**
   * Organisation hierarchy containing children of requested org. Parents are not included.
   */
  def getOrganisaatioHierarkia(oid: String): Option[OrganisaatioHierarkia] = getOrganisaatioHierarkiaIncludingParents(oid).flatMap(_.find(oid))
  /**
   * Organisation hierarchy containing parents and children of requested org.
   */
  def getOrganisaatioHierarkiaIncludingParents(oid: String): Option[OrganisaatioHierarkia]
  def getOrganisaatio(oid: String): Option[OrganisaatioWithOid] = getOrganisaatioHierarkia(oid).map(_.toOrganisaatio)
  def getChildOids(oid: String): Option[Set[String]] = getOrganisaatioHierarkia(oid).map { hierarkia =>
    def flatten(orgs: List[OrganisaatioHierarkia]): List[OrganisaatioHierarkia] = {
      orgs.flatMap { org => org :: flatten(org.children) }
    }
    flatten(List(hierarkia)).map(_.oid).toSet
  }
  def getOrganisaationNimiHetkellä(oid: String, localDate: LocalDate): Option[LocalizedString]
  def findByOppilaitosnumero(numero: String): Option[Oppilaitos]
  def findKoulutustoimijaForOppilaitos(oppilaitos: Oppilaitos): Option[Koulutustoimija] = findParentWith(oppilaitos, _.toKoulutustoimija)
  def findOppilaitosForToimipiste(toimipiste: OrganisaatioWithOid): Option[Oppilaitos] = findParentWith(toimipiste, _.toOppilaitos)

  private def findParentWith[T <: OrganisaatioWithOid](org: OrganisaatioWithOid, findr: OrganisaatioHierarkia => Option[T]) = {
    def containsOid(root: OrganisaatioHierarkia) = (root.oid == org.oid || root.children.exists(_.oid == org.oid))
    def findKoulutustoimijaFromHierarchy(root: OrganisaatioHierarkia): Option[T] = if (findr(root).isDefined && containsOid(root)) {
      findr(root)
    } else {
      root.children.flatMap(findKoulutustoimijaFromHierarchy).headOption
    }
    getOrganisaatioHierarkiaIncludingParents(org.oid).flatMap(findKoulutustoimijaFromHierarchy)
  }

  def findHierarkia(query: String): List[OrganisaatioHierarkia]

  def findSähköpostiVirheidenRaportointiin(oid: String): Option[SähköpostiVirheidenRaportointiin]
}

object OrganisaatioRepository {
  def apply(config: Config, koodisto: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager) = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
        MockOrganisaatioRepository
      case url =>
        new RemoteOrganisaatioRepository(Http(url), koodisto)
    }
  }
}

abstract class JsonOrganisaatioRepository(koodisto: KoodistoViitePalvelu) extends OrganisaatioRepository {
  protected def convertOrganisaatio(org: OrganisaatioPalveluOrganisaatio): OrganisaatioHierarkia = {
    val oppilaitosnumero = org.oppilaitosKoodi.flatMap(oppilaitosnumero => koodisto.getKoodistoKoodiViite("oppilaitosnumero", oppilaitosnumero))
    val kotipaikka = org.kotipaikkaUri.map(str => str.split("_")).flatMap {
      case Array(koodistoUri, koodi) => koodisto.getKoodistoKoodiViite(koodistoUri, koodi)
      case _ => None
    }
    val oppilaitostyyppi: Option[String] = org.oppilaitostyyppi.map(_.replace("oppilaitostyyppi_", "").replaceAll("#.*", ""))
    OrganisaatioHierarkia(org.oid, oppilaitosnumero, LocalizedString.sanitizeRequired(org.nimi, org.oid), org.ytunnus, kotipaikka, org.organisaatiotyypit, oppilaitostyyppi, org.lakkautusPvm.forall(_ > currentTimeMillis), org.children.map(convertOrganisaatio))
  }
}

class RemoteOrganisaatioRepository(http: Http, koodisto: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager) extends JsonOrganisaatioRepository(koodisto) {
  private val hierarkiaCache = KeyValueCache[String, Option[OrganisaatioHierarkia]](
    RefreshingCache("OrganisaatioRepository.hierarkia", 1 hour, 15000),
    oid => fetch(oid).organisaatiot.map(convertOrganisaatio).headOption
  )

  private val nimetCache = KeyValueCache[String, List[OrganisaationNimihakuTulos]](
    RefreshingCache("OrganisaatioRepository.nimet", 1 hour, 15000),
    oid => runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/${oid}/nimet")(Http.parseJson[List[OrganisaationNimihakuTulos]]))
  )

  private val oppilaitosnumeroCache = KeyValueCache[String, Option[Oppilaitos]](
    ExpiringCache("OrganisaatioRepository.oppilaitos", 1 hour, maxSize = 1000),
    { numero: String =>
      search(numero).flatMap {
        case o@Oppilaitos(_, Some(Koodistokoodiviite(koodiarvo, _, _, _, _)), _, _) if koodiarvo == numero => Some(o)
        case _ => None
      }.headOption
    }
  )

  def getOrganisaatioHierarkiaIncludingParents(oid: String): Option[OrganisaatioHierarkia] = hierarkiaCache(oid)

  def findByOppilaitosnumero(numero: String): Option[Oppilaitos] = oppilaitosnumeroCache(numero)

  override def findHierarkia(query: String) = {
    fetchSearchHierarchy(query).organisaatiot.map(convertOrganisaatio)
  }

  private def search(searchTerm: String): List[OrganisaatioWithOid] = fetchSearch(searchTerm).organisaatiot.map(convertOrganisaatio).map(_.toOrganisaatio)

  def fetch(oid: String): OrganisaatioHakuTulos = {
    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=true&oid=${oid}")(Http.parseJson[OrganisaatioHakuTulos]))
  }
  private def fetchSearch(searchTerm: String): OrganisaatioHakuTulos = {
    // Only for oppilaitosnumero search
    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hae?aktiiviset=true&lakkautetut=true&searchStr=${searchTerm}")(Http.parseJson[OrganisaatioHakuTulos]))
  }
  private def fetchSearchHierarchy(searchTerm: String): OrganisaatioHakuTulos = {
    // Only for "root" user organisatiopicker UI -> no cache

    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=true&searchStr=${searchTerm}")(Http.parseJson[OrganisaatioHakuTulos]))
  }

  override def getOrganisaationNimiHetkellä(oid: String, date: LocalDate) = {
    import DateOrdering._
    val nimet: List[OrganisaationNimihakuTulos] = nimetCache(oid)
    nimet.sortBy(_.alkuPvm)
      .takeWhile(nimi => nimi.alkuPvm.isBefore(date) || nimi.alkuPvm.isEqual(date))
      .lastOption.flatMap(n => LocalizedString.sanitize(n.nimi))
  }

  override def findSähköpostiVirheidenRaportointiin(oid: String): Option[SähköpostiVirheidenRaportointiin] = {
    fetchV3(oid).flatMap(org => {
      extractSähköpostiVirheidenRaportointiin(org)
        .orElse { org.parentOid.flatMap(fetchV3).flatMap(extractSähköpostiVirheidenRaportointiin) }
    })
  }

  private def fetchV3(oid: String): Option[OrganisaatioPalveluOrganisaatioV3] =
    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v3/${oid}")(Http.parseJsonOptional[OrganisaatioPalveluOrganisaatioV3]))

  private def extractSähköpostiVirheidenRaportointiin(org: OrganisaatioPalveluOrganisaatioV3): Option[SähköpostiVirheidenRaportointiin] = {
    val YhteystietojenTyyppiKoski = "1.2.246.562.5.79385887983"
    val YhteystietoElementtiTyyppiEmail = "Email"
    if (org.status != "AKTIIVINEN") {
      None
    } else {
      val koskiEmail = org.yhteystietoArvos
        .filter(_.`YhteystietoElementti.kaytossa` == "true")
        .filter(_.`YhteystietojenTyyppi.oid` == YhteystietojenTyyppiKoski)
        .find(_.`YhteystietoElementti.tyyppi` == YhteystietoElementtiTyyppiEmail)
        .map(_.`YhteystietoArvo.arvoText`)
      val defaultEmail = org.yhteystiedot
        .find(_.email.nonEmpty)
        .flatMap(_.email)
      koskiEmail.orElse(defaultEmail).map(email => SähköpostiVirheidenRaportointiin(org.oid, LocalizedString.sanitizeRequired(org.nimi, org.oid), email))
    }
  }
}

case class OrganisaatioHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaatioPalveluOrganisaatio(oid: String, ytunnus: Option[String], nimi: Map[String, String], oppilaitosKoodi: Option[String], organisaatiotyypit: List[String], oppilaitostyyppi: Option[String], kotipaikkaUri: Option[String], lakkautusPvm: Option[Long], children: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaationNimihakuTulos(nimi: Map[String, String], alkuPvm: LocalDate)

case class OrganisaatioPalveluOrganisaatioV3(oid: String, nimi: Map[String, String], parentOid: Option[String], status: String, yhteystiedot: List[YhteystietoV3], yhteystietoArvos: List[YhteystietoArvoV3])
case class YhteystietoV3(email: Option[String])
case class YhteystietoArvoV3(`YhteystietojenTyyppi.oid`: String, `YhteystietoElementti.tyyppi`: String, `YhteystietoElementti.kaytossa`: String, `YhteystietoArvo.arvoText`: String)

case class SähköpostiVirheidenRaportointiin(organisaatioOid: String, organisaationNimi: LocalizedString, email: String)
