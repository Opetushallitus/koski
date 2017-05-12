package fi.oph.koski.healthcheck

import fi.oph.koski.cache.{Cache, CacheManager, Cached, CachingProxy}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.AccessType
import fi.oph.koski.koskiuser.KoskiSession._
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{NimellinenHenkilö, OidHenkilö, Oppija}

trait HealthCheck extends Logging {
  private implicit val user = systemUser
  private implicit val accessType = AccessType.write
  private val oid = application.config.getString("healthcheck.oppija.oid")
  private def oppija: Either[HttpStatus, Oppija] = application.validator.validateAsJson(Oppija(OidHenkilö(oid), List(perustutkintoOpiskeluoikeusValmis())))

  def healthcheck: HttpStatus = try {
    application.oppijaFacade.findOppija(oid) match {
      case Left(HttpStatus(404, _)) => createHealthCheckUser
      case Left(status) => status
      case Right(Oppija(henkilö: NimellinenHenkilö, _)) =>
        if (application.perustiedotRepository.findOids(henkilö.kokonimi).contains(oid)) {
         HttpStatus.ok
        } else {
          logger.error(s"Healthcheck user $oid, not found from elasticsearch")
          KoskiErrorCategory.notFound.oppijaaEiLöydy(s"Healthcheck user $oid, not found from elasticsearch")
        }
      case Right(o) => KoskiErrorCategory.internalError(s"Healthcheck user didn't have a name ${o.henkilö}")
    }
  } catch {
    case e: Exception =>
      logger.error(e)("healthcheck failed")
      KoskiErrorCategory.internalError("healthcheck failed")
  }

  private def createHealthCheckUser: HttpStatus = {
    logger.info(s"Healtcheck user not found creating one with oid $oid")
    oppija match {
      case Left(status) => status
      case Right(oppija) =>
        application.oppijaFacade.createOrUpdate(oppija) match {
          case Left(status) =>
            logger.error(s"Problem creating healthchech oppija ${status.toString}")
            status
          case _ => HttpStatus.ok
        }
    }
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
