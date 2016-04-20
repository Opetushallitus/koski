package fi.oph.tor.organisaatio

import com.typesafe.config.Config
import fi.oph.tor.cache.{CachingProxy, TorCache}
import fi.oph.tor.http.Http.runTask
import fi.oph.tor.http.{Http, VirkailijaHttpClient}
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.log.TimedProxy
import fi.oph.tor.schema.OrganisaatioWithOid

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
}

object OrganisaatioRepository {
  def apply(config: Config, koodisto: KoodistoViitePalvelu) = {
    CachingProxy[OrganisaatioRepository](TorCache.cacheStrategy, TimedProxy(withoutCache(config, koodisto).asInstanceOf[OrganisaatioRepository]))
  }

  def withoutCache(config: Config, koodisto: KoodistoViitePalvelu): JsonOrganisaatioRepository = {
    if (config.hasPath("opintopolku.virkailija.url")) {
      val http = VirkailijaHttpClient(config.getString("opintopolku.virkailija.username"), config.getString("opintopolku.virkailija.password"), config.getString("opintopolku.virkailija.url"), "/organisaatio-service")
      new RemoteOrganisaatioRepository(http, koodisto)
    } else {
      new MockOrganisaatioRepository(koodisto)
    }
  }
}

abstract class JsonOrganisaatioRepository(koodisto: KoodistoViitePalvelu) extends OrganisaatioRepository {
  def getOrganisaatioHierarkiaIncludingParents(oid: String): Option[OrganisaatioHierarkia] = {
   fetch(oid).organisaatiot.map(convertOrganisaatio)
      .headOption
  }

  private def convertOrganisaatio(org: OrganisaatioPalveluOrganisaatio): OrganisaatioHierarkia = {
    val oppilaitosnumero = org.oppilaitosKoodi.flatMap(oppilaitosnumero => koodisto.getKoodistoKoodiViite("oppilaitosnumero", oppilaitosnumero))
    OrganisaatioHierarkia(org.oid, oppilaitosnumero, LocalizedString.sanitizeRequired(org.nimi, org.oid), org.organisaatiotyypit, org.children.map(convertOrganisaatio))
  }

  def fetch(oid: String): OrganisaatioHakuTulos
}

class RemoteOrganisaatioRepository(http: Http, koodisto: KoodistoViitePalvelu) extends JsonOrganisaatioRepository(koodisto) {
  def fetch(oid: String): OrganisaatioHakuTulos = {
    runTask(http("/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=false&oid=" + oid)(Http.parseJson[OrganisaatioHakuTulos]))
  }
}

case class OrganisaatioHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaatioPalveluOrganisaatio(oid: String, nimi: Map[String, String], oppilaitosKoodi: Option[String], organisaatiotyypit: List[String], children: List[OrganisaatioPalveluOrganisaatio])

