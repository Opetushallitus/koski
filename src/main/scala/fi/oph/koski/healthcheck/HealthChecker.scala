package fi.oph.koski.healthcheck

import cats.effect.IO
import fi.oph.koski.cache._
import fi.oph.koski.config.{Environment, KoskiApplication}
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
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.healthcheck.Subsystem._
import org.json4s.JString

import scala.collection.parallel.ExecutionContextTaskSupport
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

class HealthChecker(val application: KoskiApplication) extends Logging with Timing with GlobalExecutionContext {
  private implicit val user = systemUser
  private implicit val accessType = AccessType.write
  private val oid = application.config.getString("healthcheck.oppija.oid")
  private val koodistoPalvelu = KoodistoPalvelu.withoutCache(application.config)
  private val ePerusteet = application.ePerusteet
  private val monitoring = application.healthMonitoring
  private def healthcheckOppija: Either[HttpStatus, Oppija] = {
    application.validator.updateFieldsAndValidateAsJson(Oppija(OidHenkilö(oid), List(perustutkintoOpiskeluoikeusValmis())))
  }

  val internalSystems: Seq[Subsystem] = List(
    KoskiDatabase,
    RaportointiDatabase,
    ValpasDatabase,
    PerustiedotIndex,
    TiedonsiirtoIndex,
  ) ++ (
    if (Environment.isUnitTestEnvironment(application.config)) {
      List(MockSystemForTests)
    } else {
      List.empty
    }
  )

  val externalSystems: Seq[Subsystem] = List(
    Oppijanumerorekisteri,
    OpenSearch,
    Koodistopalvelu,
    Organisaatiopalvelu,
    EPerusteet,
    CAS,
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
    val results = checkSystems(internalSystems)
    val status = HttpStatus.fold(results.values)
    if (status.isError) {
      logger.error(s"Internal healthcheck failed: $status")
    }
    logHealthStatus(results)
    status
  }

  def checkSystems(systems: Seq[Subsystem]): Map[Subsystem, HttpStatus] = {
    val systemsPar = systems
      .par
    systemsPar.tasksupport = new ExecutionContextTaskSupport(executor)

    systemsPar
      .map(system => system -> cachedCheckSystem(system))
      .toMap
      .seq
  }

  private val cachedCheckSystem: KeyValueCache[Subsystem, HttpStatus] =
    KeyValueCache(new SeldomBlockingRefreshingCache("HealthChecker", SeldomBlockingRefreshingCache.Params(
      // duration should be longer than the time between ALB healthchecks, otherwise every healthcheck call would block.
      // refreshDuration should be shorter than the time between ALB healthchecks, in order to trigger background refresh often enough.
      // As of writing this: ALB healthcheck interval is 30 seconds, and there are healthcheck calls from 3 availability zones.
      duration = if (Environment.isUnitTestEnvironment(application.config)) { 3.seconds } else { 45.seconds },
      refreshDuration = if (Environment.isUnitTestEnvironment(application.config)) { 1.seconds } else { 10.seconds },
      maxSize = Subsystem.values.size,
      executor = executor,
      storeValuePredicate = {
        case (_, value) => value == HttpStatus.ok
      }
    ))(application.cacheManager), checkSystem)

  private def checkSystem(system: Subsystem): HttpStatus = {
    timed(s"${system}", 0) {
      system match {
        case Oppijanumerorekisteri => oppijaCheck(findOrCreateOppija)
        case OpenSearch => openSearchCheck(findOrCreateOppija)
        case Koodistopalvelu => koodistopalveluCheck
        case Organisaatiopalvelu => organisaatioPalveluCheck
        case EPerusteet => ePerusteetCheck
        case CAS => casCheck
        case KoskiDatabase => assertTrue("koski database", application.masterDatabase.util.databaseIsOnline)
        case RaportointiDatabase => assertTrue("raportointi database", application.raportointiDatabase.util.databaseIsOnline)
        case ValpasDatabase => assertTrue("valpas database", application.valpasDatabase.util.databaseIsOnline)
        case PerustiedotIndex => assertTrue("perustiedot index", application.perustiedotIndexer.index.isOnline)
        case TiedonsiirtoIndex => assertTrue("tiedonsiirrot index", application.tiedonsiirtoService.index.isOnline)
        case MockSystemForTests => assertTrue("mock system for tests", {
          mockSystemCounter -= 1

          mockSystemCheckFunction(mockSystemCounter)
        })
        case other: Subsystem => HttpStatus(404, List(ErrorDetail("invalid subsystem", JString(other.toString))))
      }
    }
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

  private def logHealthStatus(status: scala.collection.Map[Subsystem, HttpStatus]): Unit = {
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

  def initializeMockSystem(initialCounterValue: Long, checkFunction: Long => Boolean): Unit = {
    mockSystemCounter = initialCounterValue
    mockSystemCheckFunction = checkFunction
  }
  def getMockSystemCounter: Long = mockSystemCounter

  private var mockSystemCounter: Long = 0
  private var mockSystemCheckFunction: Long => Boolean = (_: Long) => true
}
