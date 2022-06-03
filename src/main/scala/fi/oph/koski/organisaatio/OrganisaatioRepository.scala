package fi.oph.koski.organisaatio

import java.lang.System.currentTimeMillis
import java.time.LocalDate

import com.typesafe.config.Config
import fi.oph.koski.cache._
import fi.oph.koski.http.{ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema._

trait OrganisaatioRepository extends Logging {
  /**
   * Organisation hierarchy containing children of requested org. Parents are not included.
   */
  def getOrganisaatioHierarkia(oid: String): Option[OrganisaatioHierarkia] =
    getOrganisaatioHierarkiaIncludingParents(oid).map(_.find(oid)).find(_.isDefined).flatten

  /**
   * Organisation hierarchy containing parents and children of requested org.
   */
  def getOrganisaatioHierarkiaIncludingParents(oid: String): List[OrganisaatioHierarkia] = findWithOid(oid).toList
  def getOrganisaatio(oid: String): Option[OrganisaatioWithOid] = getOrganisaatioHierarkia(oid).map(_.toOrganisaatio)
  def getChildOids(oid: String): Option[Set[String]] = getOrganisaatioHierarkia(oid).map { hierarkia =>
    OrganisaatioHierarkia.flatten(List(hierarkia)).map(_.oid).toSet
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

  def findWithOid(organisaatioOid: Oid): Option[OrganisaatioHierarkia] =
    find(_.oid == organisaatioOid)

  def find(condition: OrganisaatioHierarkia => Boolean): Option[OrganisaatioHierarkia] = {
    filterView(condition).force.headOption
  }

  def filter(condition: OrganisaatioHierarkia => Boolean): List[OrganisaatioHierarkia] = {
    filterView(condition).toList
  }

  private def filterView(condition: OrganisaatioHierarkia => Boolean) = {
    findAllHierarkiat.view.flatMap { h =>
      OrganisaatioHierarkiaFilter.filter(h, condition).toList
    }
  }

  def findHierarkia(query: String): List[OrganisaatioHierarkia]

  def findSähköpostiVirheidenRaportointiin(oid: String): Option[SähköpostiVirheidenRaportointiin]

  def findAllFlattened: List[OrganisaatioHierarkia] = OrganisaatioHierarkia.flatten(findAllHierarkiat)
  def findAllHierarkiat: List[OrganisaatioHierarkia]

  def findAllVarhaiskasvatusToimipisteet: List[(OrganisaatioWithOid, Boolean)] = {
    OrganisaatioHierarkia.flatten(findAllHierarkiat)
      .filter(o =>
        o.organisaatiotyypit.contains(Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.peruskoulut) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.peruskouluasteenErityiskoulut) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.perusJaLukioasteenKoulut)
      )
      .map(o => (o.toOrganisaatio, o.organisaatiotyypit.contains(Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA)))
  }

  def findVarhaiskasvatusHierarkiat: List[OrganisaatioHierarkia] = {
    filter(o =>
      o.organisaatiotyypit.contains(Organisaatiotyyppi.VARHAISKASVATUKSEN_TOIMIPAIKKA) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.peruskoulut) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.peruskouluasteenErityiskoulut) ||
        o.oppilaitostyyppi.contains(Oppilaitostyyppi.perusJaLukioasteenKoulut)
    )
  }

  def findKunnat(): Seq[OrganisaatioHierarkia] = {
    filter(_.organisaatiotyypit.contains(Organisaatiotyyppi.KUNTA))
  }

  def convertOrganisaatio(org: OrganisaatioPalveluOrganisaatio): OrganisaatioHierarkia = {
    val oppilaitosnumero = org.oppilaitosKoodi.flatMap(oppilaitosnumero => koodisto.validate("oppilaitosnumero", oppilaitosnumero))
    val kotipaikka = org.kotipaikkaUri.map(str => str.split("_")).flatMap {
      case Array(koodistoUri, koodi) => koodisto.validate(koodistoUri, koodi)
      case _ => None
    }
    val oppilaitostyyppi: Option[String] = org.oppilaitostyyppi.map(_.replace("oppilaitostyyppi_", "").replaceAll("#.*", ""))
    val organisaatiotyypit = org.organisaatiotyypit.map(orgType => Organisaatiotyyppi.convertFromNew(orgType))
    OrganisaatioHierarkia(
      oid = org.oid,
      oppilaitosnumero = oppilaitosnumero,
      nimi = LocalizedString.sanitizeRequired(org.nimi, org.oid),
      yTunnus = org.ytunnus,
      kotipaikka = kotipaikka,
      organisaatiotyypit = organisaatiotyypit,
      oppilaitostyyppi = oppilaitostyyppi,
      aktiivinen = org.lakkautusPvm.forall(_ > currentTimeMillis),
      kielikoodit = org.kieletUris,
      children = org.children.map(convertOrganisaatio)
    )
  }

  def koodisto: KoodistoViitePalvelu
}

object OrganisaatioRepository {
  def apply(config: Config, koodisto: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager) = {
    config.getString("opintopolku.virkailija.url") match {
      case "mock" =>
        MockOrganisaatioRepository
      case url =>
        val http = VirkailijaHttpClient(ServiceConfig.apply(config, "opintopolku.virkailija"), "/organisaatio-service", sessionCookieName = "SESSION", true)
        new RemoteOrganisaatioRepository(http, koodisto)
    }
  }
}

object Organisaatiotyyppi {
  val KOULUTUSTOIMIJA = "KOULUTUSTOIMIJA"
  val OPPILAITOS = "OPPILAITOS"
  val TOIMIPISTE = "TOIMIPISTE"
  val OPPISOPIMUSTOIMIPISTE = "OPPISOPIMUSTOIMIPISTE"
  val VARHAISKASVATUKSEN_TOIMIPAIKKA = "VARHAISKASVATUKSEN_TOIMIPAIKKA"
  val VARHAISKASVATUKSEN_JARJESTAJA = "VARHAISKASVATUKSEN_JARJESTAJA"
  val KUNTA = "KUNTA"

  private val fromNew = Map(
    "organisaatiotyyppi_01" -> KOULUTUSTOIMIJA,
    "organisaatiotyyppi_02" -> OPPILAITOS,
    "organisaatiotyyppi_03" -> TOIMIPISTE,
    "organisaatiotyyppi_04" -> OPPISOPIMUSTOIMIPISTE,
    "organisaatiotyyppi_08" -> VARHAISKASVATUKSEN_TOIMIPAIKKA,
    "organisaatiotyyppi_07" -> VARHAISKASVATUKSEN_JARJESTAJA,
    "organisaatiotyyppi_09" -> KUNTA
  )

  def convertFromNew(newOrgType: String): Oid =
    fromNew.getOrElse(newOrgType, newOrgType)
}

