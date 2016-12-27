package fi.oph.koski.organisaatio

import com.typesafe.config.Config
import fi.oph.koski.cache.{Cache, CacheManager, CachingProxy}
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, VirkailijaHttpClient}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.TimedProxy
import fi.oph.koski.schema.{Koodistokoodiviite, Koulutustoimija, Oppilaitos, OrganisaatioWithOid}

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
  def findByOppilaitosnumero(numero: String): Option[Oppilaitos]
  def findKoulutustoimija(oppilaitos: Oppilaitos): Option[Koulutustoimija] = {
    def findKoulutustoimijaFromHierarchy(root: OrganisaatioHierarkia): Option[Koulutustoimija] = if (root.toKoulutustoimija.isDefined && root.children.exists(_.oid == oppilaitos.oid)) {
      root.toKoulutustoimija
    } else {
      root.children.flatMap(findKoulutustoimijaFromHierarchy).headOption
    }
    getOrganisaatioHierarkiaIncludingParents(oppilaitos.oid).flatMap(findKoulutustoimijaFromHierarchy)
  }
  def findHierarkia(query: String): List[OrganisaatioHierarkia]
}

object OrganisaatioRepository {
  def apply(config: Config, koodisto: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager) = {
    CachingProxy[OrganisaatioRepository](Cache.cacheAllRefresh("OrganisaatioRepository", durationSeconds = 3600, maxSize = 15000), withoutCache(config, koodisto))
  }

  def withoutCache(config: Config, koodisto: KoodistoViitePalvelu): OrganisaatioRepository = {
    if (config.hasPath("opintopolku.virkailija.url")) {
      val http = VirkailijaHttpClient(config.getString("opintopolku.virkailija.username"), config.getString("opintopolku.virkailija.password"), config.getString("opintopolku.virkailija.url"), "/organisaatio-service")
      new RemoteOrganisaatioRepository(http, koodisto)
    } else {
      MockOrganisaatioRepository
    }
  }
}

abstract class JsonOrganisaatioRepository(koodisto: KoodistoViitePalvelu) extends OrganisaatioRepository {
  protected def convertOrganisaatio(org: OrganisaatioPalveluOrganisaatio): OrganisaatioHierarkia = {
    val oppilaitosnumero = org.oppilaitosKoodi.flatMap(oppilaitosnumero => koodisto.getKoodistoKoodiViite("oppilaitosnumero", oppilaitosnumero))
    val oppilaitostyyppi: Option[String] = org.oppilaitostyyppi.map(_.replace("oppilaitostyyppi_", "").replaceAll("#.*", ""))
    OrganisaatioHierarkia(org.oid, oppilaitosnumero, LocalizedString.sanitizeRequired(org.nimi, org.oid), org.ytunnus, org.organisaatiotyypit, oppilaitostyyppi, org.children.map(convertOrganisaatio))
  }
}

class RemoteOrganisaatioRepository(http: Http, koodisto: KoodistoViitePalvelu) extends JsonOrganisaatioRepository(koodisto) {
  def getOrganisaatioHierarkiaIncludingParents(oid: String): Option[OrganisaatioHierarkia] = {
    fetch(oid).organisaatiot.map(convertOrganisaatio).headOption
  }

  def findByOppilaitosnumero(numero: String): Option[Oppilaitos] = {
    search(numero).flatMap {
      case o@Oppilaitos(_, Some(Koodistokoodiviite(koodiarvo, _, _, _, _)), _) if koodiarvo == numero => Some(o)
      case _ => None
    }.headOption
  }

  override def findHierarkia(query: String) = {
    fetchSearchHierarchy(query).organisaatiot.map(convertOrganisaatio)
  }

  private def search(searchTerm: String): List[OrganisaatioWithOid] = fetchSearch(searchTerm).organisaatiot.map(convertOrganisaatio).map(_.toOrganisaatio)

  def fetch(oid: String): OrganisaatioHakuTulos = {
    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&oid=${oid}")(Http.parseJson[OrganisaatioHakuTulos]))
  }
  private def fetchSearch(searchTerm: String): OrganisaatioHakuTulos = {
    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hae?aktiiviset=true&lakkautetut=false&searchStr=${searchTerm}")(Http.parseJson[OrganisaatioHakuTulos]))
  }
  private def fetchSearchHierarchy(searchTerm: String): OrganisaatioHakuTulos = {
    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&searchStr=${searchTerm}")(Http.parseJson[OrganisaatioHakuTulos]))
  }

}

case class OrganisaatioHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaatioPalveluOrganisaatio(oid: String, ytunnus: Option[String], nimi: Map[String, String], oppilaitosKoodi: Option[String], organisaatiotyypit: List[String], oppilaitostyyppi: Option[String], children: List[OrganisaatioPalveluOrganisaatio])

