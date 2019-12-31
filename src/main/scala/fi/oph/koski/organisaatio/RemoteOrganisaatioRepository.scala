package fi.oph.koski.organisaatio

import java.time.LocalDate

import fi.oph.koski.cache._
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering

import scala.concurrent.duration._

class RemoteOrganisaatioRepository(http: Http, koodisto: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager) extends JsonOrganisaatioRepository(koodisto) {
  private val hierarkiaCache = KeyValueCache[String, List[OrganisaatioHierarkia]](
    RefreshingCache("OrganisaatioRepository.hierarkia", 1.hour, 15000),
    fetch(_).organisaatiot.map(convertOrganisaatio)
  )

  private val nimetCache = KeyValueCache[String, List[OrganisaationNimihakuTulos]](
    RefreshingCache("OrganisaatioRepository.nimet", 1.hour, 15000),
    oid => runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/${oid}/nimet")(Http.parseJson[List[OrganisaationNimihakuTulos]]))
  )

  private val oppilaitosnumeroCache = KeyValueCache[String, Option[Oppilaitos]](
    ExpiringCache("OrganisaatioRepository.oppilaitos", 1.hour, maxSize = 1000),
    { numero: String =>
      search(numero).flatMap {
        case o@Oppilaitos(_, Some(Koodistokoodiviite(koodiarvo, _, _, _, _)), _, _) if koodiarvo == numero => Some(o)
        case _ => None
      }.headOption
    }
  )

  private val päiväkotiCache = SingleValueCache[List[OrganisaatioPalveluOrganisaatioTyyppi]](
    RefreshingCache("OrganisaatioRepository.varhaiskasvatusToimipisteet", 1.hour, 5000),
    () => fetchAllVarhaiskasvatusToimipisteet
  )

  def getOrganisaatioHierarkiaIncludingParents(oid: String): List[OrganisaatioHierarkia] = hierarkiaCache(oid)

  def findByOppilaitosnumero(numero: String): Option[Oppilaitos] = oppilaitosnumeroCache(numero)

  override def findHierarkia(query: String) = {
    fetchSearchHierarchy(query).organisaatiot.map(convertOrganisaatio)
  }

  def findAllVarhaiskasvatusToimipisteet: List[OrganisaatioPalveluOrganisaatioTyyppi] = päiväkotiCache.apply

  private def fetchAllVarhaiskasvatusToimipisteet: List[OrganisaatioPalveluOrganisaatioTyyppi] =
    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v4/hae?aktiiviset=true&suunnitellut=true&lakkautetut=true&organisaatiotyyppi=organisaatiotyyppi_08")(Http.parseJson[OrganisaatioTyyppiHakuTulos])).organisaatiot

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
    val nimet: List[OrganisaationNimihakuTulos] = nimetCache(oid)
    nimet.sortBy(_.alkuPvm)(DateOrdering.localDateOrdering)
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

  override def findAllRaw: List[OrganisaatioPalveluOrganisaatio] = {
    runTask(http.get(uri"/organisaatio-service/rest/organisaatio/v2/hae?aktiiviset=true&lakkautetut=true&suunnitellut=true&searchStr=")(Http.parseJson[OrganisaatioHakuTulos])).organisaatiot
  }
}

case class OrganisaatioHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaatioPalveluOrganisaatio(oid: String, ytunnus: Option[String], nimi: Map[String, String], oppilaitosKoodi: Option[String], organisaatiotyypit: List[String], oppilaitostyyppi: Option[String], kotipaikkaUri: Option[String], kieletUris: List[String], lakkautusPvm: Option[Long], children: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaatioTyyppiHakuTulos(organisaatiot: List[OrganisaatioPalveluOrganisaatioTyyppi])
case class OrganisaatioPalveluOrganisaatioTyyppi(oid: String, nimi: Map[String, String], organisaatiotyypit: List[String], oppilaitostyyppi: Option[String], children: List[OrganisaatioPalveluOrganisaatio])
case class OrganisaationNimihakuTulos(nimi: Map[String, String], alkuPvm: LocalDate)

case class OrganisaatioPalveluOrganisaatioV3(oid: String, nimi: Map[String, String], parentOid: Option[String], status: String, yhteystiedot: List[YhteystietoV3], yhteystietoArvos: List[YhteystietoArvoV3])
case class YhteystietoV3(email: Option[String])
case class YhteystietoArvoV3(`YhteystietojenTyyppi.oid`: String, `YhteystietoElementti.tyyppi`: String, `YhteystietoElementti.kaytossa`: String, `YhteystietoArvo.arvoText`: String)

case class SähköpostiVirheidenRaportointiin(organisaatioOid: String, organisaationNimi: LocalizedString, email: String)
