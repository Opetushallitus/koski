package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.schedule.Scheduler
import fi.oph.koski.util.Wait

import java.time.Duration

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

        mockTiedotuspalveluClient.sentNotifications.exists(_ == (oppijaOid, opiskeluoikeusOid)) should be(true)
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

        val (ooOid, oOid) = eligible.get
        val job = KielitutkintotodistusTiedoteJob(
          id = java.util.UUID.randomUUID().toString,
          oppijaOid = oOid,
          opiskeluoikeusOid = ooOid,
          state = KielitutkintotodistusTiedoteState.ERROR,
          worker = Some(repository.workerId),
          attempts = 1,
          error = Some("Tiedotuspalvelu ei vastaa")
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

    "Scheduler on oletuksena pois päältä (tiedote.enabled = false)" in {
      KoskiApplication.defaultConfig.getBoolean("tiedote.enabled") should be(false)
    }

    "Scheduler ei käsittele tiedotteita kun se on pysäytetty" in {
      waitForSchedulerIdle()
      Scheduler.pauseForDuration(app.masterDatabase.db, "kielitutkintotodistus-tiedote", Duration.ofDays(1))
      waitForSchedulerIdle()
      app.kielitutkintotodistusTiedoteRepository.truncateForLocal()
      mockTiedotuspalveluClient.reset()

      try {
        Thread.sleep(3000)
        app.kielitutkintotodistusTiedoteRepository.findAll(100, 0) should have length 0
        mockTiedotuspalveluClient.sentNotifications should have length 0
      } finally {
        Scheduler.resume(app.masterDatabase.db, "kielitutkintotodistus-tiedote")
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
