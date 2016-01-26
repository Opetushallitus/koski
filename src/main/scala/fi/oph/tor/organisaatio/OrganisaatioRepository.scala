package fi.oph.tor.organisaatio

import com.typesafe.config.Config
import fi.oph.tor.cache.{CachingProxy, TorCache}
import fi.oph.tor.http.{Http, VirkailijaHttpClient}
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.log.TimedProxy
import fi.oph.tor.schema.{Oppilaitos, OrganisaatioWithOid}

trait OrganisaatioRepository {
  /**
   * Organisation hierarchy containing children of requested org. Parents are not included.
   */
  def getOrganisaatioHierarkia(oid: String): Option[OrganisaatioHierarkia] =  getOrganisaatioHierarkiaIncludingParents(oid).flatMap(_.find(oid))
  /**
   * Organisation hierarchy containing parents and children of requested org.
   */
  def getOrganisaatioHierarkiaIncludingParents(oid: String): Option[OrganisaatioHierarkia]
  def getOrganisaatio(oid: String): Option[OrganisaatioWithOid] = getOrganisaatioHierarkia(oid).map(_.toOrganisaatio)
}

object OrganisaatioRepository {
  def apply(config: Config, koodisto: KoodistoViitePalvelu) = {
    CachingProxy[OrganisaatioRepository](TorCache.cacheStrategy, TimedProxy(if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteOrganisaatioRepository(config, koodisto)
    } else {
      new MockOrganisaatioRepository(koodisto)
    }))
  }
}

abstract class JsonOrganisaatioRepository(koodisto: KoodistoViitePalvelu) extends OrganisaatioRepository {
  def getOrganisaatioHierarkiaIncludingParents(oid: String): Option[OrganisaatioHierarkia] = {
   fetch(oid).organisaatiot.map(convertOrganisaatio)
      .headOption
  }

  private def convertOrganisaatio(org: OrganisaatioPalveluOrganisaatio): OrganisaatioHierarkia = {
    val oppilaitosnumero = org.oppilaitosKoodi.flatMap(oppilaitosnumero => koodisto.getKoodistoKoodiViite("oppilaitosnumero", oppilaitosnumero))
    val nimi: String = org.nimi.getOrElse("fi", org.oid)
    OrganisaatioHierarkia(org.oid, oppilaitosnumero, nimi, org.organisaatiotyypit, org.children.map(convertOrganisaatio))
  }

  def fetch(oid: String): OrganisaatioHakuTulos
}

class RemoteOrganisaatioRepository(config: Config, koodisto: KoodistoViitePalvelu) extends JsonOrganisaatioRepository(koodisto) {
  val virkailijaClient = new VirkailijaHttpClient(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"), "/organisaatio-service")

  def fetch(oid: String): OrganisaatioHakuTulos = {
    virkailijaClient.httpClient("/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&oid=" + oid)(Http.parseJson[OrganisaatioHakuTulos]).run
  }
}

case class OrganisaatioHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaatioPalveluOrganisaatio(oid: String, nimi: Map[String, String], oppilaitosKoodi: Option[String], organisaatiotyypit: List[String], children: List[OrganisaatioPalveluOrganisaatio]) {

}

