package fi.oph.koski.healthcheck

import fi.oph.koski.cache.{Cache, CacheManager, Cached, CachingProxy}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.http.{ErrorDetail, HttpStatus, HttpStatusException, KoskiErrorCategory}
import fi.oph.koski.koodisto.{KoodistoPalvelu, KoodistoViite}
import fi.oph.koski.koskiuser.AccessType
import fi.oph.koski.koskiuser.KoskiSession._
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.{MockOrganisaatiot, RemoteOrganisaatioRepository}
import fi.oph.koski.schema._

trait HealthCheck extends Logging {
  private implicit val user = systemUser
  private implicit val accessType = AccessType.write
  private val oid = application.config.getString("healthcheck.oppija.oid")
  private val koodistoPalvelu = KoodistoPalvelu.withoutCache(application.config)
  private def healthcheckOppija: Either[HttpStatus, Oppija] = application.validator.validateAsJson(Oppija(OidHenkilö(oid), List(perustutkintoOpiskeluoikeusValmis())))

  def healthcheck: HttpStatus = {
    val oppija = findOrCreateOppija
    val checks: List[() => HttpStatus] = List(
      () => oppijaCheck(oppija),
      () => elasticCheck(oppija),
      () => koodistopalveluCheck,
      () => organisaatioPalveluCheck
    )

    HttpStatus.fold(checks.par.map(_.apply).toList)
  }

  private def oppijaCheck(oppija: Either[HttpStatus, NimellinenHenkilö]): HttpStatus = oppija.left.getOrElse(HttpStatus.ok)

  private def elasticCheck(oppija: Either[HttpStatus, NimellinenHenkilö]): HttpStatus = oppija.flatMap { henkilö =>
    get("elasticsearch", application.perustiedotRepository.findOids(henkilö.kokonimi))
      .filterOrElse(_.contains(oid), KoskiErrorCategory.notFound.oppijaaEiLöydy(s"Healthcheck user $oid, not found from elasticsearch"))
  }.left.getOrElse(HttpStatus.ok)

  private def koodistopalveluCheck: HttpStatus =
    get("koodistopalvelu", koodistoPalvelu.getKoodistoKoodit(KoodistoViite("suorituksentyyppi", 1)).toList.flatten)
      .filterOrElse(_.nonEmpty, KoskiErrorCategory.notFound.koodistoaEiLöydy())
      .left.getOrElse(HttpStatus.ok)

  private def organisaatioPalveluCheck: HttpStatus =
    application.organisaatioRepository match {
      case remote: RemoteOrganisaatioRepository =>
        get("organisaatiopalvelu", remote.fetch(MockOrganisaatiot.helsinginYliopisto)).filterOrElse(_.organisaatiot.nonEmpty, KoskiErrorCategory.notFound.oppilaitostaEiLöydy())
          .left.getOrElse(HttpStatus.ok)
      case _ => HttpStatus.ok
    }

  private def findOrCreateOppija: Either[HttpStatus, NimellinenHenkilö] = {
    def findOrCreate(canCreate: Boolean): Either[HttpStatus, NimellinenHenkilö] = getOppija(oid) match {
      case Left(HttpStatus(404, _)) if canCreate =>
        createHealthCheckUser
        findOrCreate(canCreate = false)
      case Right(Oppija(henkilö: NimellinenHenkilö, _)) => Right(henkilö)
      case Right(o) => Left(KoskiErrorCategory.internalError(s"Healthcheck käyttäjällä ei ollut nimeä ${o.henkilö}"))
      case Left(status) => Left(status)
    }
    findOrCreate(canCreate = true)
  }

  private def getOppija(oid: String): Either[HttpStatus, Oppija] = {
    get("oppijanumerorekisteri", application.henkilöRepository.opintopolku.findByOid(oid))
      .flatMap(_.toRight(KoskiErrorCategory.notFound.oppijaaEiLöydy(s"Healtcheck käyttäjää $oid ei löydy oppijanumerorekisteristä")))
      .flatMap { henkilö =>
        get("postgres", application.opiskeluoikeusRepository.findByOppijaOid(oid)).flatMap { oos =>
          if (oos.isEmpty) Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Healthcheck käyttäjän $oid opiskeluoikeuksia ei löydy tietokannasta"))
          else Right(Oppija(henkilö, oos))
        }
      }
  }

  private def createHealthCheckUser: HttpStatus = {
    logger.info(s"Healtcheck user not found creating one with oid $oid")
    healthcheckOppija match {
      case Left(status) => status
      case Right(oppija) =>
        application.oppijaFacade.createOrUpdate(oppija, allowUpdate = true) match {
          case Left(status) =>
            logger.error(s"Problem creating healthchech oppija ${status.toString}")
            status
          case _ => HttpStatus.ok
        }
    }
  }

  private def get[T](key: String, f: => T): Either[HttpStatus, T] = try {
    Right(f)
  } catch {
    case e: HttpStatusException =>
      Left(HttpStatus(e.status, List(ErrorDetail(key, e.text))))
    case e =>
      logger.error(e)("healthcheck failed")
      Left(KoskiErrorCategory.internalError.subcategory(key, "healthcheck failed")())
  }

  def application: KoskiApplication
}

object HealthCheck {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): HealthCheck with Cached = {
    CachingProxy[HealthCheck](
      Cache.cacheAllNoRefresh("HealthCheck", durationSeconds = 10, maxSize = 1),
      new HealthChecker(application)
    )
  }
}

class HealthChecker(val application: KoskiApplication) extends HealthCheck
