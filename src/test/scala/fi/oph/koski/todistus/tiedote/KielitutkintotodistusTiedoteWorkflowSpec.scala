package fi.oph.koski.todistus.tiedote

import com.typesafe.config.ConfigValueFactory
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.log.{AuditLogTester, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.util.Wait
import fi.oph.koski.json.GenericJsonFormats
import org.json4s.jackson.JsonMethods.parse

class KielitutkintotodistusTiedoteWorkflowSpec extends KielitutkintotodistusTiedoteSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  "Kielitutkintotodistuksen tiedote" - {
    "Lähettää tiedotteen vahvistetusta kielitutkinnosta" in {
      withoutRunningTiedoteScheduler {
        val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
        val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeusOid(oppijaOid).get

        app.kielitutkintotodistusTiedoteService.processAll()

        val jobs = app.kielitutkintotodistusTiedoteRepository.findAll(100, 0)
        val job = jobs.find(_.opiskeluoikeusOid == opiskeluoikeusOid)
        job shouldBe defined
        job.get.oppijaOid should equal(oppijaOid)
        job.get.state should equal(KielitutkintotodistusTiedoteState.COMPLETED)
        job.get.completedAt shouldBe defined

        mockTiedotuspalveluClient.sentNotifications.exists(_ == (oppijaOid, s"$opiskeluoikeusOid-initial")) should be(true)
      }
    }

    "Ei luo duplikaattitiedotetta samalle opiskeluoikeudelle" in {
      withoutRunningTiedoteScheduler {
        app.kielitutkintotodistusTiedoteService.processAll()
        val jobsBefore = app.kielitutkintotodistusTiedoteRepository.findAll(100, 0)

        // Toinen processAll ei luo uusia jobeja koska kaikki on jo käsitelty
        app.kielitutkintotodistusTiedoteService.processAll()
        val jobsAfter = app.kielitutkintotodistusTiedoteRepository.findAll(100, 0)

        jobsAfter should have length jobsBefore.length
      }
    }

    "Yrittää epäonnistunutta tiedotetta uudelleen" in {
      withoutRunningTiedoteScheduler {
        val repository = app.kielitutkintotodistusTiedoteRepository
        val eligible = repository.findEligibleBatch(1).headOption
        eligible shouldBe defined

        val (ooOid, oOid, versio) = eligible.get
        val job = KielitutkintotodistusTiedoteJob(
          id = java.util.UUID.randomUUID().toString,
          oppijaOid = oOid,
          opiskeluoikeusOid = ooOid,
          state = KielitutkintotodistusTiedoteState.ERROR,
          worker = Some(repository.workerId),
          attempts = 1,
          error = Some("Tiedotuspalvelu ei vastaa"),
          opiskeluoikeusVersio = versio
        )
        repository.add(job)

        val errorJobs = repository.findAll(10, 0, Some(KielitutkintotodistusTiedoteState.ERROR))
        errorJobs should have length 1

        // Retry käyttää oikeaa mock-clientiä joka onnistuu
        app.kielitutkintotodistusTiedoteService.retryAllFailed()

        val completedJobs = repository.findAll(10, 0, Some(KielitutkintotodistusTiedoteState.COMPLETED))
        completedJobs should have length 1
        mockTiedotuspalveluClient.sentNotifications should have length 1
      }
    }

    "processAll käsittelee useamman batchin kun batchSize on pieni" in {
      withoutRunningTiedoteScheduler {
        val processed = app.kielitutkintotodistusTiedoteService.processAll()
        processed should be > 1

        val jobs = app.kielitutkintotodistusTiedoteRepository.findAll(100, 0)
        jobs should have length processed
        mockTiedotuspalveluClient.sentNotifications should have length processed
      }
    }

    "Kirjaa audit-lokin kun tiedote lähetetään onnistuneesti" in {
      withoutRunningTiedoteScheduler {
        val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
        val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeusOid(oppijaOid).get

        AuditLogTester.clearMessages()
        val processed = app.kielitutkintotodistusTiedoteService.processAll()

        // processAll käsittelee useita opiskeluoikeuksia, joten tarkistetaan kaikki viestit
        implicit val formats = GenericJsonFormats.genericFormats
        val messages = AuditLogTester.getLogMessages.map(s => parse(s))
        messages should have length processed
        messages.foreach { msg =>
          (msg \ "operation").extract[String] should equal(KoskiOperation.TIEDOTE_LAHETETTY.toString)
        }
        // Tarkistetaan että oikea oppija ja opiskeluoikeus on mukana joukossa
        messages.exists { msg =>
          (msg \ "target" \ KoskiAuditLogMessageField.oppijaHenkiloOid.toString).extractOpt[String].contains(oppijaOid) &&
          (msg \ "target" \ KoskiAuditLogMessageField.opiskeluoikeusOid.toString).extractOpt[String].contains(opiskeluoikeusOid) &&
          (msg \ "target" \ KoskiAuditLogMessageField.tiedoteTyyppi.toString).extractOpt[String].contains("kielitodistus")
        } should be(true)
      }
    }

    "Kirjaa audit-lokin kun tiedote lähetetään uudelleenyrityksellä" in {
      withoutRunningTiedoteScheduler {
        val repository = app.kielitutkintotodistusTiedoteRepository
        val (ooOid, oOid, versio) = repository.findEligibleBatch(1).head
        repository.add(KielitutkintotodistusTiedoteJob(
          id = java.util.UUID.randomUUID().toString,
          oppijaOid = oOid,
          opiskeluoikeusOid = ooOid,
          state = KielitutkintotodistusTiedoteState.ERROR,
          worker = Some(repository.workerId),
          attempts = 1,
          error = Some("Tiedotuspalvelu ei vastaa"),
          opiskeluoikeusVersio = versio
        ))

        AuditLogTester.clearMessages()
        app.kielitutkintotodistusTiedoteService.retryAllFailed()

        AuditLogTester.verifyLastAuditLogMessageForOperation(Map(
          "operation" -> KoskiOperation.TIEDOTE_LAHETETTY.toString,
          "target" -> Map(
            KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oOid,
            KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> ooOid,
            KoskiAuditLogMessageField.tiedoteTyyppi.toString -> "kielitodistus"
          )
        ))
      }
    }

    "Ei lähetä tiedotetta valtionhallinnon kielitutkinnosta" in {
      withoutRunningTiedoteScheduler {
        val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
        val eligible = app.kielitutkintotodistusTiedoteRepository.findEligibleBatch(100)
        val oppijaEligible = eligible.filter { case (_, oOid, _) => oOid == oppijaOid }
        // Suorittajalla on 2 yleistä kielitutkintoa ja 3 valtionhallinnon kielitutkintoa,
        // mutta vain yleiset kielitutkinnot pitää olla mukana
        oppijaEligible should have length 2
      }
    }

    "Ei käsittele opiskeluoikeuksia joiden vahvistuspäivä on ennen earliestDate-rajausta" in {
      withoutRunningTiedoteScheduler {
        // Testiympäristön earliestDate on 2010-01-01, fixture-vahvistuspäivät ovat 2011+ joten ne ovat eligible
        val eligibleBefore = app.kielitutkintotodistusTiedoteRepository.findEligibleBatch(100)
        eligibleBefore should not be empty

        // Luodaan uusi repository tulevaisuuden päivämäärärajauksella
        val futureConfig = app.config
          .withValue("tiedote.earliestDate", ConfigValueFactory.fromAnyRef("2099-01-01"))
        val restrictedRepo = new KielitutkintotodistusTiedoteRepository(app.masterDatabase.db, app.instanceId, futureConfig)

        val eligibleAfter = restrictedRepo.findEligibleBatch(100)
        eligibleAfter shouldBe empty
      }
    }

    "Ei käsittele opiskeluoikeuksia jotka ovat luotu grace period -ajan sisällä" in {
      withoutRunningTiedoteScheduler {
        // Testiympäristön gracePeriodHours on 0, joten kaikki fixture-opiskeluoikeudet ovat eligible
        val eligibleBefore = app.kielitutkintotodistusTiedoteRepository.findEligibleBatch(100)
        eligibleBefore should not be empty

        // Luodaan uusi repository erittäin pitkällä grace periodilla
        val strictConfig = app.config
          .withValue("tiedote.gracePeriodHours", ConfigValueFactory.fromAnyRef(999999))
        val strictRepo = new KielitutkintotodistusTiedoteRepository(app.masterDatabase.db, app.instanceId, strictConfig)

        val eligibleAfter = strictRepo.findEligibleBatch(100)
        eligibleAfter shouldBe empty
      }
    }

    "Scheduler on oletuksena pois päältä (tiedote.enabled = false)" in {
      KoskiApplication.defaultConfig.getBoolean("tiedote.enabled") should be(false)
    }

    "Scheduler ei käsittele tiedotteita kun se on pysäytetty" in {
      waitForSchedulerIdle()
      app.kielitutkintotodistusTiedoteScheduler.schedulerInstance.foreach(_.suspend())
      waitForSchedulerIdle()
      app.kielitutkintotodistusTiedoteRepository.truncateForLocal()
      mockTiedotuspalveluClient.reset()

      try {
        Thread.sleep(3000)
        app.kielitutkintotodistusTiedoteRepository.findAll(100, 0) should have length 0
        mockTiedotuspalveluClient.sentNotifications should have length 0
      } finally {
        app.kielitutkintotodistusTiedoteScheduler.schedulerInstance.foreach(_.unsuspend())
      }
    }

    "Scheduler lähettää tiedotteen automaattisesti kun se on päällä" in {
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeusOid(oppijaOid).get

      // Schedulerin pitäisi poimia tiedote automaattisesti
      Wait.until {
        val jobs = app.kielitutkintotodistusTiedoteRepository.findAll(100, 0)
        jobs.exists(j => j.opiskeluoikeusOid == opiskeluoikeusOid && j.state == KielitutkintotodistusTiedoteState.COMPLETED)
      }

      mockTiedotuspalveluClient.sentNotifications.exists(_._1 == oppijaOid) should be(true)
    }
  }
}
