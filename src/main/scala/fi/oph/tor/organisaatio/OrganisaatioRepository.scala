package fi.oph.tor.organisaatio

import com.typesafe.config.Config
import fi.oph.tor.cache.{TorCache, CachingProxy}
import fi.oph.tor.http.{Http, VirkailijaHttpClient}
import fi.oph.tor.schema.OidOrganisaatio
import fi.oph.tor.util.TimedProxy

trait OrganisaatioRepository {
  def getOrganisaatioHierarkia(oid: String): Option[OrganisaatioHierarkia]
  def getOrganisaatio(oid: String): Option[OidOrganisaatio] = getOrganisaatioHierarkia(oid).map(org => OidOrganisaatio(oid, Some(org.nimi)))
}

object OrganisaatioRepository {
  def apply(config: Config) = {
    TimedProxy(CachingProxy[OrganisaatioRepository](TorCache.cacheStrategy, if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteOrganisaatioRepository(config)
    } else {
      MockOrganisaatioRepository
    }))
  }
}

class RemoteOrganisaatioRepository(config: Config) extends OrganisaatioRepository{
  val virkailijaClient = new VirkailijaHttpClient(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"), "/organisaatio-service")

  def getOrganisaatioHierarkia(oid: String): Option[OrganisaatioHierarkia] = {
    virkailijaClient.httpClient("/organisaatio-service/rest/organisaatio/v2/hierarkia/hae/tyyppi?aktiiviset=true&lakkautetut=false&oid=" + oid)(Http.parseJson[OrganisaatioHakuTulos])
      .run
      .organisaatiot.map(convertOrganisaatio)
      .headOption
  }

  private def convertOrganisaatio(org: OrganisaatioPalveluOrganisaatio): OrganisaatioHierarkia = {
    OrganisaatioHierarkia(org.oid, org.nimi("fi"), org.organisaatiotyypit, org.children.map(convertOrganisaatio))
  }
}

case class OrganisaatioHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaatioPalveluOrganisaatio(oid: String, nimi: Map[String, String], organisaatiotyypit: List[String], children: List[OrganisaatioPalveluOrganisaatio])

