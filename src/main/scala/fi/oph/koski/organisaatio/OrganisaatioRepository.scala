package fi.oph.koski.organisaatio

import java.lang.System.currentTimeMillis
import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.cache._
import fi.oph.koski.http.{ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._

import scala.collection.immutable
import scala.concurrent.duration._

trait OrganisaatioRepository {
  /**
   * Organisation hierarchy containing children of requested org. Parents are not included.
   */
  def getOrganisaatioHierarkia(oid: String): Option[OrganisaatioHierarkia] =
    getOrganisaatioHierarkiaIncludingParents(oid).map(_.find(oid)).find(_.isDefined).flatten

  /**
   * Organisation hierarchy containing parents and children of requested org.
   */
  def getOrganisaatioHierarkiaIncludingParents(oid: String): List[OrganisaatioHierarkia]
  def getOrganisaatio(oid: String): Option[OrganisaatioWithOid] = getOrganisaatioHierarkia(oid).map(_.toOrganisaatio)
  def getChildOids(oid: String): Option[Set[String]] = getOrganisaatioHierarkia(oid).map { hierarkia =>
    def flatten(orgs: List[OrganisaatioHierarkia]): List[OrganisaatioHierarkia] = {
      orgs.flatMap { org => org :: flatten(org.children) }
    }
    flatten(List(hierarkia)).map(_.oid).toSet
  }
  def getOrganisaationNimiHetkellä(oid: String, localDate: LocalDate): Option[LocalizedString]
  def findByOppilaitosnumero(numero: String): Option[Oppilaitos]
  def findKoulutustoimijaForOppilaitos(oppilaitos: OrganisaatioWithOid): Option[Koulutustoimija] = findParentWith(oppilaitos, _.toKoulutustoimija)
  def findOppilaitosForToimipiste(toimipiste: OrganisaatioWithOid): Option[Oppilaitos] = findParentWith(toimipiste, _.toOppilaitos)

  private def findParentWith[T <: OrganisaatioWithOid](org: OrganisaatioWithOid, findr: OrganisaatioHierarkia => Option[T]) = {
    def containsOid(root: OrganisaatioHierarkia) = root.oid == org.oid || root.children.exists(_.oid == org.oid)
    def findKoulutustoimijaFromHierarchy(root: OrganisaatioHierarkia): Option[T] = if (findr(root).isDefined && containsOid(root)) {
      findr(root)
    } else {
      root.children.flatMap(findKoulutustoimijaFromHierarchy).headOption
    }
    getOrganisaatioHierarkiaIncludingParents(org.oid).map(findKoulutustoimijaFromHierarchy).find(_.isDefined).flatten
  }

  def findHierarkia(query: String): List[OrganisaatioHierarkia]

  def findSähköpostiVirheidenRaportointiin(oid: String): Option[SähköpostiVirheidenRaportointiin]

  def findAllRaw: List[OrganisaatioPalveluOrganisaatio]

  def findVarhaiskasvatusHierarkiat: List[OrganisaatioHierarkia]
  def findAllVarhaiskasvatusToimipisteet: List[OrganisaatioPalveluOrganisaatio]
  def fetchByOids(list: List[String]): List[OrganisaatioPalveluOrganisaatio]
}

object OrganisaatioRepository {
  def apply(config: Config, koodisto: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager) = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
        MockOrganisaatioRepository
      case url =>
        val http = VirkailijaHttpClient(ServiceConfig.apply(config, "opintopolku.virkailija"), "/organisaatio-service", sessionCookieName = "SESSION")
        new RemoteOrganisaatioRepository(http, koodisto)
    }
  }
}

abstract class JsonOrganisaatioRepository(koodisto: KoodistoViitePalvelu) extends OrganisaatioRepository {
  protected def convertOrganisaatio(org: OrganisaatioPalveluOrganisaatio): OrganisaatioHierarkia = {
    val oppilaitosnumero = org.oppilaitosKoodi.flatMap(oppilaitosnumero => koodisto.validate("oppilaitosnumero", oppilaitosnumero))
    val kotipaikka = org.kotipaikkaUri.map(str => str.split("_")).flatMap {
      case Array(koodistoUri, koodi) => koodisto.validate(koodistoUri, koodi)
      case _ => None
    }
    val oppilaitostyyppi: Option[String] = org.oppilaitostyyppi.map(_.replace("oppilaitostyyppi_", "").replaceAll("#.*", ""))
    OrganisaatioHierarkia(org.oid, oppilaitosnumero, LocalizedString.sanitizeRequired(org.nimi, org.oid), org.ytunnus, kotipaikka, org.organisaatiotyypit, oppilaitostyyppi, org.lakkautusPvm.forall(_ > currentTimeMillis), org.children.map(convertOrganisaatio))
  }

  val varhaiskasvatusToimipaikkaTyyppi = "VARHAISKASVATUKSEN_TOIMIPAIKKA"
  private val parentOidPathRegex = ".*/(1.2.246.562.10.*)/1.2.246.562.10.00000000001".r
  protected def uncachedVarhaiskasvatusHierarkiat: List[OrganisaatioHierarkia] = {
    val varhaiskasvatusToimipisteet = findAllVarhaiskasvatusToimipisteet
    val parentOids = varhaiskasvatusToimipisteet.flatMap(_.parentOidPath).flatMap(parentOidPathRegex.findFirstMatchIn(_).map(_.group(1))).distinct
    fetchByOids(parentOids).map(convertOrganisaatio).flatMap { hierarkia =>
      val varhaiskasvatusToimipisteet = hierarkia.children.flatMap(c => OrganisaatioHierarkiaFilter.prune(c, org => {
        OrganisaatioHierarkia.flatten(c :: org.children).exists(_.organisaatiotyypit.contains(varhaiskasvatusToimipaikkaTyyppi))
      }))

      if (varhaiskasvatusToimipisteet.nonEmpty) {
        List(hierarkia.copy(children = varhaiskasvatusToimipisteet))
      } else {
        Nil
      }
    }
  }
}
