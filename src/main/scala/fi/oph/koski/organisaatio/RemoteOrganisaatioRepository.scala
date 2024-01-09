package fi.oph.koski.organisaatio

import java.time.LocalDate

import fi.oph.koski.cache._
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering

import scala.concurrent.duration.DurationInt

class RemoteOrganisaatioRepository(http: Http, val koodisto: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager) extends OrganisaatioRepository {
  private val fullHierarchyCache = SingleValueCache[List[OrganisaatioHierarkia]](
    RefreshingCache(name = "OrganisaatioRepository.fullHierarkia", duration = 12.hours, maxSize = 2), // maxSize needs to be minimum 2 for the refreshing to work
    () => uncachedFindAllHierarkiatRaw.map(convertOrganisaatio)
  )

  private val nimetCache = KeyValueCache[String, List[OrganisaationNimihakuTulos]](
    RefreshingCache("OrganisaatioRepository.nimet", 12.hour, 15000),
    oid => runIO(http.get(uri"/organisaatio-service/rest/organisaatio/v2/${oid}/nimet")(Http.parseJson[List[OrganisaationNimihakuTulos]]))
  )

  private val oppilaitosnumeroCache = KeyValueCache[String, Option[Oppilaitos]](
    ExpiringCache("OrganisaatioRepository.oppilaitos", 12.hour, maxSize = 1000),
    { numero: String =>
      search(numero).flatMap {
        case o@Oppilaitos(_, Some(Koodistokoodiviite(koodiarvo, _, _, _, _)), _, _) if koodiarvo == numero => Some(o)
        case _ => None
      }.headOption
    }
  )

  def findByOppilaitosnumero(numero: String): Option[Oppilaitos] = oppilaitosnumeroCache(numero)

  override def findHierarkia(query: String) = {
    fetchSearchHierarchy(query).organisaatiot.map(convertOrganisaatio)
  }

  private def search(searchTerm: String): List[OrganisaatioWithOid] = fetchSearch(searchTerm).organisaatiot.map(convertOrganisaatio).map(_.toOrganisaatio)

  private def fetchSearch(searchTerm: String): OrganisaatioHakuTulos = {
    // Only for oppilaitosnumero search
    runIO(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hae?aktiiviset=true&lakkautetut=true&searchStr=${searchTerm}")(Http.parseJson[OrganisaatioHakuTulos]))
  }
  private def fetchSearchHierarchy(searchTerm: String): OrganisaatioHakuTulos = {
    // Only for "root" user organisatiopicker UI -> no cache

    runIO(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hierarkia/hae?aktiiviset=true&lakkautetut=true&searchStr=${searchTerm}")(Http.parseJson[OrganisaatioHakuTulos]))
  }

  override def getOrganisaationNimiHetkellä(oid: String, date: LocalDate) = {
    val nimetSorted: List[OrganisaationNimihakuTulos] = nimetCache(oid).sortBy(_.alkuPvm)(DateOrdering.localDateOrdering)
    val oldest = nimetSorted.headOption
    nimetSorted
      .takeWhile(nimi => nimi.alkuPvm.isBefore(date) || nimi.alkuPvm.isEqual(date))
      .lastOption
      .orElse(oldest)
      .flatMap(n => LocalizedString.sanitize(n.nimi))
  }

  override def findSähköpostiVirheidenRaportointiin(oid: String): Option[SähköpostiVirheidenRaportointiin] = {
    fetchV3(oid).flatMap(org => {
      extractSähköpostiVirheidenRaportointiin(org)
        .orElse { org.parentOid.flatMap(fetchV3).flatMap(extractSähköpostiVirheidenRaportointiin) }
    })
  }

  def fetchV3(oid: String): Option[OrganisaatioPalveluOrganisaatioV3] =
    runIO(http.get(uri"/organisaatio-service/rest/organisaatio/v3/${oid}")(Http.parseJsonOptional[OrganisaatioPalveluOrganisaatioV3]))

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

  override def findAllHierarkiat: List[OrganisaatioHierarkia] = fullHierarchyCache.apply

  private def uncachedFindAllHierarkiatRaw: List[OrganisaatioPalveluOrganisaatio] = {
    logger.info("Fetching organisaatiohierarkia")
    runIO(http.get(uri"/organisaatio-service/rest/organisaatio/v4/${Opetushallitus.organisaatioOid}/jalkelaiset")(Http.parseJson[OrganisaatioHakuTulos])).organisaatiot
  }
}

case class OrganisaatioHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaatioPalveluOrganisaatio(
  oid: String,
  parentOid: Option[String],
  parentOidPath: Option[String],
  ytunnus: Option[String],
  nimi: Map[String, String],
  oppilaitosKoodi: Option[String],
  organisaatiotyypit: List[String],
  oppilaitostyyppi: Option[String],
  kotipaikkaUri: Option[String],
  kieletUris: List[String],
  lakkautusPvm: Option[Long],
  children: List[OrganisaatioPalveluOrganisaatio],
) {
  def parentOids: List[String] =
    parentOidPath match {
      case Some(path) => path.split('/').toList.reverse
      case None => parentOid.toList
    }
}

case class OrganisaatioTyyppiHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatioTyyppi])
case class OrganisaatioPalveluOrganisaatioTyyppi(oid: String, nimi: Map[String, String], organisaatiotyypit: List[String], oppilaitostyyppi: Option[String], children: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaationNimihakuTulos(nimi: Map[String, String], alkuPvm: LocalDate)

case class OrganisaatioPalveluOrganisaatioV3(oid: String, nimi: Map[String, String], parentOid: Option[String], status: String, yhteystiedot: List[YhteystietoV3], yhteystietoArvos: List[YhteystietoArvoV3])
case class YhteystietoV3(email: Option[String])
case class YhteystietoArvoV3(`YhteystietojenTyyppi.oid`: String, `YhteystietoElementti.tyyppi`: String, `YhteystietoElementti.kaytossa`: String, `YhteystietoArvo.arvoText`: String)

case class SähköpostiVirheidenRaportointiin(organisaatioOid: String, organisaationNimi: LocalizedString, email: String)
