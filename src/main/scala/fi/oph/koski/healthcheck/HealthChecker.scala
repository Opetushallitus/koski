package fi.oph.koski.healthcheck

import cats.effect.IO
import fi.oph.koski.cache.RefreshingCache.Params
import fi.oph.koski.cache._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.eperusteet.ERakenneOsa
import fi.oph.koski.http._
import fi.oph.koski.koodisto.{KoodistoPalvelu, KoodistoViite}
import fi.oph.koski.koskiuser.AccessType
import fi.oph.koski.koskiuser.KoskiSpecificSession._
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.{MockOrganisaatiot, RemoteOrganisaatioRepository}
import fi.oph.koski.schema._
import fi.oph.koski.userdirectory.Password
import fi.oph.koski.util.Timing
import fi.vm.sade.utils.cas.CasClientException

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

trait HealthCheck extends Logging {
  private implicit val user = systemUser
  private implicit val accessType = AccessType.write
  private val oid = application.config.getString("healthcheck.oppija.oid")
  private val koodistoPalvelu = KoodistoPalvelu.withoutCache(application.config)
  private val ePerusteet = application.ePerusteet
  private def healthcheckOppija: Either[HttpStatus, Oppija] =
    application.validator.updateFieldsAndValidateAsJson(Oppija(OidHenkilö(oid), List(perustutkintoOpiskeluoikeusValmis())))

  def healthcheckWithExternalSystems: HttpStatus = {
    logger.debug("Performing healthcheck")
    val oppija = findOrCreateOppija
    val checks: List[() => HttpStatus] = List(
      () => oppijaCheck(oppija),
      () => elasticCheck(oppija),
      () => koodistopalveluCheck,
      () => organisaatioPalveluCheck,
      () => ePerusteetCheck,
      () => casCheck
    )

    val status = HttpStatus.fold(checks.par.map(_.apply).seq)
    if (status.isError) {
      logger.warn(s"Healthcheck with external systems status failed $status")
    }
    status
  }

  def internalHealthcheck: HttpStatus = {
    val checks: Seq[() => HttpStatus] = List(
      () => assertTrue("koski database", application.masterDatabase.util.databaseIsOnline),
      () => assertTrue("raportointi database", application.raportointiDatabase.util.databaseIsOnline),
      () => assertTrue("valpas database", application.valpasDatabase.util.databaseIsOnline),
      () => assertTrue("perustiedot index", application.perustiedotIndexer.index.isOnline),
      () => assertTrue("tiedonsiirrot index", application.tiedonsiirtoService.index.isOnline)
    )
    val status = HttpStatus.fold(checks.par.map(_.apply).seq)
    if (status.isError) {
      logger.error(s"Internal healthcheck failed: $status")
    }
    status
  }

  private def oppijaCheck(oppija: Either[HttpStatus, NimellinenHenkilö]): HttpStatus = oppija.left.getOrElse(HttpStatus.ok)

  private def elasticCheck(oppija: Either[HttpStatus, NimellinenHenkilö]): HttpStatus = oppija.flatMap { henkilö =>
    get("elasticsearch", application.perustiedotRepository.findOids(henkilö.kokonimi))
      .filterOrElse(_.contains(oid), KoskiErrorCategory.notFound.oppijaaEiLöydy(s"Healthcheck user $oid, not found from elasticsearch"))
  }.left.getOrElse(HttpStatus.ok)

  private def koodistopalveluCheck: HttpStatus =
    get("koodistopalvelu", koodistoPalvelu.getKoodistoKoodit(KoodistoViite("suorituksentila", 1)))
      .filterOrElse(_.nonEmpty, KoskiErrorCategory.notFound.koodistoaEiLöydy())
      .left.getOrElse(HttpStatus.ok)

  private def organisaatioPalveluCheck: HttpStatus =
    application.organisaatioRepository match {
      case remote: RemoteOrganisaatioRepository =>
        get("organisaatiopalvelu", remote.fetchV3(MockOrganisaatiot.helsinginYliopisto)).filterOrElse(_.isDefined, KoskiErrorCategory.notFound.oppilaitostaEiLöydy())
          .left.getOrElse(HttpStatus.ok)
      case _ => HttpStatus.ok
    }

  private def ePerusteetCheck: HttpStatus = Timing.timed("ePerusteetCheck", 3000, this.getClass) {
    val diaarinumerot = List("OPH-2664-2017")
    HttpStatus.fold(diaarinumerot.map(checkPeruste))
  }

  def casCheck: HttpStatus = {
    val VirkailijaCredentials(username, password) = VirkailijaCredentials(application.config, true)

    def authenticate = try {
      Some(application.casService.authenticateVirkailija(username, Password(password)))
    } catch {
      case _: CasClientException => None
    }

    get("cas", authenticate).flatMap(_.toRight(KoskiErrorCategory.internalError("CAS check failed"))) match {
      case Right(authOk) if authOk => HttpStatus.ok
      case Right(_) => KoskiErrorCategory.unauthorized.loginFail("Koski login failed")
      case Left(status) => status
    }
  }

  private def checkPeruste(diaarinumero: String) = get("ePerusteet", ePerusteet.findRakenne(diaarinumero), timeout = 15 seconds).flatMap {
    case None => Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy(s"Tutkinnon rakennetta $diaarinumero ei löydy Perusteista"))
    case Some(rakenne) =>
      val rakenteet: List[ERakenneOsa] = rakenne.suoritustavat.toList.flatten.flatMap(_.rakenne)
      rakenteet match {
        case Nil => Left(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy(s"Tutkinnon $diaarinumero rakenne on tyhjä"))
        case _ => Right(HttpStatus.ok)
      }
  }.left.getOrElse(HttpStatus.ok)

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
        get("postgres", application.possu.findByOppijaOids(List(oid))).flatMap { oos =>
          if (oos.isEmpty) Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Healthcheck käyttäjän $oid opiskeluoikeuksia ei löydy tietokannasta"))
          else Right(Oppija(henkilö.toHenkilötiedotJaOid, oos))
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
            logger.error(s"Problem creating healthcheck oppija ${status.toString}")
            status
          case _ => HttpStatus.ok
        }
    }
  }

  private def get[T](key: String, f: => T, timeout: FiniteDuration = 5 seconds): Either[HttpStatus, T] = {
    Http.runIO(
      IO(f)
        .timeout(timeout)
        .map(Right(_))
        .handleError {
          case e: HttpStatusException =>
            Left(HttpStatus(e.status, List(ErrorDetail(key, e.msg))))
          case e: Exception =>
            logger.warn(e)("healthcheck failed")
            Left(KoskiErrorCategory.internalError.subcategory(key, "healthcheck failed")())
        }
    )
  }

  private def assertTrue(key: String, f: => Boolean): HttpStatus = {
    get(key, f) match {
      case Left(err) => err
      case Right(false) => KoskiErrorCategory.internalError.subcategory(key, s"healthcheck for $key failed")()
      case Right(true) => HttpStatus.ok
    }
  }

  def application: KoskiApplication
}

object HealthCheck {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): HealthCheck with Cached = {
    CachingProxy[HealthCheck](
      new RefreshingCache("HealthCheck", Params(15 seconds, maxSize = 10, refreshScatteringRatio = 0)),
      new HealthChecker(application)
    )
  }
}

class HealthChecker(val application: KoskiApplication) extends HealthCheck
