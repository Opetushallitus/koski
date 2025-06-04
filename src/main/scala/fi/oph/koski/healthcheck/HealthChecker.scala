package fi.oph.koski.healthcheck

import cats.effect.IO
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
import fi.oph.koski.cas.CasClientException
import fi.oph.koski.executors.Pools
import org.json4s.JString

import scala.concurrent.ExecutionContext.fromExecutor
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

trait HealthCheck extends Logging {
  private implicit val user = systemUser
  private implicit val accessType = AccessType.write
  private val oid = application.config.getString("healthcheck.oppija.oid")
  private val activeThreadThreshold = application.config.getInt("healthcheck.activeThreadThreshold")
  private val koodistoPalvelu = KoodistoPalvelu.withoutCache(application.config)
  private val ePerusteet = application.ePerusteet
  private val monitoring = application.healthMonitoring
  private def healthcheckOppija: Either[HttpStatus, Oppija] = {
    application.validator.updateFieldsAndValidateAsJson(Oppija(OidHenkilö(oid), List(perustutkintoOpiskeluoikeusValmis())))
  }

  val internalSystems: Seq[String] = List(
    Subsystem.KoskiDatabase,
    Subsystem.RaportointiDatabase,
    Subsystem.ValpasDatabase,
    Subsystem.PerustiedotIndex,
    Subsystem.TiedonsiirtoIndex,
    Subsystem.Oppijanumerorekisteri,
  )

  val externalSystems: Seq[String] = List(
    Subsystem.Oppijanumerorekisteri,
    Subsystem.OpenSearch,
    Subsystem.Koodistopalvelu,
    Subsystem.Organisaatiopalvelu,
    Subsystem.EPerusteet,
    Subsystem.CAS,
  )

  def healthcheckWithExternalSystems: HttpStatus = {
    logger.debug("Performing healthcheck")
    val results = checkSystems(externalSystems)
    val status = HttpStatus.fold(results.values)
    if (status.isError) {
      logger.warn(s"Healthcheck with external systems status failed $status")
    }
    logHealthStatus(results)
    status
  }

  def internalHealthcheck: HttpStatus = {
    logStackTracesIfPoolGettingFull()

    val results = checkSystems(internalSystems)
    val status = HttpStatus.fold(results.values)
    if (status.isError) {
      logger.error(s"Internal healthcheck failed: $status")
    }
    logHealthStatus(results)
    status
  }

  def checkSystems(systems: Seq[String]): Map[String, HttpStatus] =
    systems
      .par
      .map(system => system -> checkSystem(system))
      .toMap
      .seq

  def checkSystem(system: String) =
    system match {
      case Subsystem.Oppijanumerorekisteri => oppijaCheck(findOrCreateOppija)
      case Subsystem.OpenSearch => openSearchCheck(findOrCreateOppija)
      case Subsystem.Koodistopalvelu => koodistopalveluCheck
      case Subsystem.Organisaatiopalvelu => organisaatioPalveluCheck
      case Subsystem.EPerusteet => ePerusteetCheck
      case Subsystem.CAS => casCheck
      case Subsystem.KoskiDatabase => assertTrue("koski database", application.masterDatabase.util.databaseIsOnline)
      case Subsystem.RaportointiDatabase => assertTrue("raportointi database", application.raportointiDatabase.util.databaseIsOnline)
      case Subsystem.ValpasDatabase => assertTrue("valpas database", application.valpasDatabase.util.databaseIsOnline)
      case Subsystem.PerustiedotIndex => assertTrue("perustiedot index", application.perustiedotIndexer.index.isOnline)
      case Subsystem.TiedonsiirtoIndex => assertTrue("tiedonsiirrot index", application.tiedonsiirtoService.index.isOnline)
      case other: String => HttpStatus(404, List(ErrorDetail("invalid subsystem", JString(other))))
    }

  private def oppijaCheck(oppija: Either[HttpStatus, NimellinenHenkilö]): HttpStatus = oppija.left.getOrElse(HttpStatus.ok)

  private def openSearchCheck(oppija: Either[HttpStatus, NimellinenHenkilö]): HttpStatus = oppija.flatMap { henkilö =>
    get("opensearch", application.perustiedotRepository.findOids(henkilö.kokonimi))
      .filterOrElse(_.contains(oid), KoskiErrorCategory.notFound.oppijaaEiLöydy(s"Healthcheck user $oid, not found from OpenSearch"))
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

  private def logHealthStatus(status: scala.collection.Map[String, HttpStatus]): Unit = {
    val payload = status
      .map(SubsystemHealthStatus.apply)
      .toSeq
    monitoring.log(payload)
  }

  def casCheck: HttpStatus = {
    val VirkailijaCredentials(username, password) = try {
      VirkailijaCredentials(application.config, true)
    } catch {
      case _: Throwable => return KoskiErrorCategory.internalError("No CAS configuration")
    }

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

  private def checkPeruste(diaarinumero: String) = get("ePerusteet", ePerusteet.findTarkatRakenteet(diaarinumero, None).headOption, timeout = 15 seconds).flatMap {
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

  def logStackTracesIfPoolGettingFull(): HttpStatus = {
    import collection.JavaConverters._

    val activeCount = Pools.globalPoolExecutor.getActiveCount

    logger.info(s"Active threads: ${activeCount}. Threshold for outputting detailed stack traces is ${activeThreadThreshold}.")

    if (activeCount > activeThreadThreshold) {
      val traces = java.lang.Thread.getAllStackTraces.values.asScala
      val nonParkedTraces =
        traces
          .map(_.toSeq.map(_.toString))
          .filterNot(_.exists(_.contains("jdk.internal.misc.Unsafe.park")))
          .map(_.mkString("\n    "))

      logger.info(s"========== DEBUG STACK TRACES: globalPool active thread count has reached ${activeCount}, exceecing threshold of ${activeThreadThreshold}. Outputting stack traces of non-parked ${nonParkedTraces.size} threads out of total number of ${traces.size} threads.")

      nonParkedTraces.foreach(trace =>
        logger.info("========== DEBUG STACK TRACE\n    " + trace)
      )
    }

    HttpStatus.ok
  }

  def application: KoskiApplication
}

object HealthCheck {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): HealthCheck = {
    new HealthChecker(application)
  }
}

class HealthChecker(val application: KoskiApplication) extends HealthCheck
