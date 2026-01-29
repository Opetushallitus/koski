package fi.oph.koski.todistus

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExamplesKielitutkinto
import fi.oph.koski.henkilo.{ OppijaHenkilö}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{AuditLogTester}
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Opiskeluoikeus, Suoritus, YleisenKielitutkinnonSuoritus}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}


import java.net.URL
import java.security.MessageDigest
import java.time.{Duration, LocalDate, LocalDateTime}

import scala.jdk.CollectionConverters._

class TodistusSpecHelpers extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with PutOpiskeluoikeusTestMethods[KielitutkinnonOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[KielitutkinnonOpiskeluoikeus]]

  implicit val formats: DefaultFormats.type = DefaultFormats

  val app = KoskiApplicationForTests

  val vahvistettuKielitutkinnonOpiskeluoikeus = ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2011, 1, 3), "FI", "kt")
  val defaultOpiskeluoikeus = vahvistettuKielitutkinnonOpiskeluoikeus

  override protected def beforeAll(): Unit = {
    resetFixtures()
  }

  protected def cleanup(): Unit = {
    Wait.until { !hasWork }
    Wait.until(!app.todistusScheduler.schedulerInstance.exists(_.isTaskRunning))
    Wait.until(!app.todistusCleanupScheduler.schedulerInstance.exists(_.isTaskRunning))

    app.todistusRepository.truncateForLocal()
    AuditLogTester.clearMessages()
  }

  def hasWork: Boolean = {
    KoskiApplicationForTests.todistusRepository.numberOfMyRunningJobs > 0 || KoskiApplicationForTests.todistusRepository.numberOfQueuedJobs > 0
  }

  def getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid: String): Option[KielitutkinnonOpiskeluoikeus] = {
    getOpiskeluoikeudet(oppijaOid).find(_.suoritukset.exists {
      case s: YleisenKielitutkinnonSuoritus if s.vahvistus.isDefined => true
      case _ => false
    }).map(_.asInstanceOf[KielitutkinnonOpiskeluoikeus])
  }

  def getVahvistettuOpiskeluoikeus(oppijaOid: String): Option[Opiskeluoikeus] = {
    getOpiskeluoikeudet(oppijaOid).find(_.suoritukset.exists {
      case s: Suoritus if s.vahvistus.isDefined => true
      case _ => false
    })
  }

  def addGenerateJob[T](req: TodistusGenerateRequest, hetu: String)(f: => T): T =
    get(s"api/todistus/generate/${req.toPathParams}", headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)

  def addGenerateJobSuccessfully[T](req: TodistusGenerateRequest, hetu: String)(f: TodistusJob => T): T = {
    addGenerateJob(req, hetu) {
      f(parsedResponse)
    }
  }

  def addGenerateJobAsVirkailijaPääkäyttäjä[T](req: TodistusGenerateRequest)(f: => T): T =
    get(s"api/todistus/generate/${req.toPathParams}", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent)(f)

  def addGenerateJobSuccessfullyAsVirkailijaPääkäyttäjä[T](req: TodistusGenerateRequest)(f: TodistusJob => T): T = {
    addGenerateJobAsVirkailijaPääkäyttäjä(req) {
      f(parsedResponse)
    }
  }

  def getStatus[T](id: String, hetu: String)(f: => T): T =
    get(s"api/todistus/status/$id", headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)

  def getStatusSuccessfully[T](id: String, hetu: String)(f: TodistusJob => T): T = {
    getStatus(id, hetu) {
      f(parsedResponse)
    }
  }

  def getStatusAsVirkailijaPääkäyttäjä[T](id: String)(f: => T): T =
    get(s"api/todistus/status/$id", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent)(f)

  def getStatusSuccessfullyAsVirkailijaPääkäyttäjä[T](id: String)(f: TodistusJob => T): T = {
    getStatusAsVirkailijaPääkäyttäjä(id) {
      f(parsedResponse)
    }
  }

  def checkStatusByParameters[T](req: TodistusGenerateRequest, hetu: String)(f: => T): T =
    get(s"api/todistus/status/${req.toPathParams}", headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)

  def checkStatusByParametersSuccessfully[T](req: TodistusGenerateRequest, hetu: String)(f: TodistusJob => T): T = {
    checkStatusByParameters(req, hetu) {
      f(parsedResponse)
    }
  }

  def getResult[T](url: String, hetu: String)(f: => T): T = {
    val rootUrl = KoskiApplicationForTests.config.getString("koski.root.url")
    get(url.replace(rootUrl, ""), headers = kansalainenLoginHeaders(hetu))(f)
  }

  def verifyPresignedResult(url: String): Unit =
    verifyPresignedResultAndContent(url) {}

  def verifyPresignedResultAndContent[T](url: String)(f: => T): T = {
    val location = new URL(get(url, headers = authHeaders(MockUsers.paakayttaja)) {
      verifyResponseStatus(302) // 302: Found (redirect)
      response.header("Location")
    })
    withBaseUrl(location) {
      get(s"${location.getPath}?${location.getQuery}") {
        verifyResponseStatusOk()
        f
      }
    }
  }

  def verifyDownloadResult(url: String, hetu: String): Unit =
    verifyDownloadResultAndContent(url, hetu) {}

  def verifyDownloadResultAndContent[T](url: String, hetu: String)(f: => T): T = {
    getResult(url, hetu) {
      verifyResponseStatusOk()
      f
    }
  }

  def waitForStateTransition(id: String, hetu: String)(states: String*): TodistusJob = {
    var lastResponse: Option[TodistusJob] = None
    Wait.until {
      getStatusSuccessfully(id, hetu) { response =>
        states should contain(response.state)
        lastResponse = Some(response)
        response.state == states.last
      }
    }
    lastResponse.get
  }

  def waitForCompletion(id: String, hetu: String): TodistusJob =
    waitForStateTransition(id, hetu)(
      TodistusState.QUEUED,
      TodistusState.GATHERING_INPUT,
      TodistusState.GENERATING_RAW_PDF,
      TodistusState.SAVING_RAW_PDF,
      TodistusState.STAMPING_PDF,
      TodistusState.SAVING_STAMPED_PDF,
      TodistusState.COMPLETED
    )

  def waitForCompletionSkipStateChecks(id:String, hetu: String): TodistusJob = {
    var lastResponse: Option[TodistusJob] = None

    Wait.until {
      getStatusSuccessfully(id, hetu) { response =>
        lastResponse = Some(response)
        response.state == TodistusState.COMPLETED
      }
    }
    lastResponse.get
  }

  def waitForError(id: String, hetu: String): TodistusJob = {
    var lastResponse: Option[TodistusJob] = None
    Wait.until {
      getStatusSuccessfully(id, hetu) { response =>
        lastResponse = Some(response)
        response.state == TodistusState.ERROR
      }
    }
    lastResponse.get
  }

  def createOrphanJob(oppijaOid: String, opiskeluoikeusOid: String, language: String, attempts: Int): TodistusJob = {
    val orphanJob = TodistusJob(
      id = java.util.UUID.randomUUID().toString,
      userOid = Some(oppijaOid),
      oppijaOid = oppijaOid,
      opiskeluoikeusOid = opiskeluoikeusOid,
      language = language,
      opiskeluoikeusVersionumero = Some(1),
      oppijaHenkilötiedotHash = Some("test-hash"),
      state = TodistusState.GENERATING_RAW_PDF,
      createdAt = LocalDateTime.now(),
      startedAt = Some(LocalDateTime.now()),
      completedAt = None,
      worker = Some("dead-worker-id"),
      attempts = Some(attempts),
      error = None
    )
    app.todistusRepository.addRawForUnitTests(orphanJob)
  }

  def parsedResponse: TodistusJob = {
    verifyResponseStatusOk()
    val json = JsonMethods.parse(response.body)
    val result = KoskiApplicationForTests.validatingAndResolvingExtractor.extract[TodistusJob](json, strictDeserialization)
    result should not be Left
    result.toOption.get
  }

  def withoutRunningSchedulers[T](f: => T): T = withoutRunningSchedulers(true)(f)

  def withoutRunningSchedulers[T](truncate: Boolean)(f: => T): T =
    try {
      Wait.until { !hasWork }

      app.todistusScheduler.pause(Duration.ofDays(1))
      app.todistusCleanupScheduler.pause(Duration.ofDays(1))

      // Wait for any running tasks to complete
      Wait.until(!app.todistusScheduler.schedulerInstance.exists(_.isTaskRunning))
      Wait.until(!app.todistusCleanupScheduler.schedulerInstance.exists(_.isTaskRunning))

      f
    } finally {
      if (truncate) {
        app.todistusRepository.truncateForLocal()
      }
      app.todistusScheduler.resume()
      app.todistusCleanupScheduler.resume()
    }

  def laskeHenkilötiedotHash(henkilö: OppijaHenkilö): String = {
    val data = s"${henkilö.etunimet}|${henkilö.sukunimi}|${henkilö.syntymäaika.getOrElse("")}"
    val digest = MessageDigest.getInstance("SHA-256")
    digest.digest(data.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
