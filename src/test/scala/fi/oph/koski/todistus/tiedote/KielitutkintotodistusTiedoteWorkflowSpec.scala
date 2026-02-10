package fi.oph.koski.todistus.tiedote

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.util.Wait

class KielitutkintotodistusTiedoteWorkflowSpec extends KielitutkintotodistusTiedoteSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  "Kielitutkintotodistuksen tiedote" - {
    "Lähettää tiedotteen vahvistetusta kielitutkinnosta (v1)" in {
      withoutRunningTiedoteScheduler {
        val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
        val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeusOid(oppijaOid).get

        app.kielitutkintotodistusTiedoteService.processNext()

        val jobs = app.kielitutkintotodistusTiedoteRepository.findAll(10, 0)
        jobs should have length 1
        jobs.head.oppijaOid should equal(oppijaOid)
        jobs.head.opiskeluoikeusOid should equal(opiskeluoikeusOid)
        jobs.head.state should equal(KielitutkintotodistusTiedoteState.COMPLETED)

        mockTiedotuspalveluClient.sentNotifications should have length 1
        mockTiedotuspalveluClient.sentNotifications.head should equal((oppijaOid, opiskeluoikeusOid))
      }
    }

    "Ei luo duplikaattitiedotetta samalle opiskeluoikeudelle" in {
      withoutRunningTiedoteScheduler {
        app.kielitutkintotodistusTiedoteService.processNext()
        val jobsBefore = app.kielitutkintotodistusTiedoteRepository.findAll(10, 0)
        val firstJobOoOid = jobsBefore.head.opiskeluoikeusOid

        // processNext käsittelee seuraavan eligible-opiskeluoikeuden, ei samaa uudelleen
        app.kielitutkintotodistusTiedoteService.processNext()
        val jobsAfter = app.kielitutkintotodistusTiedoteRepository.findAll(10, 0)

        // Toinen kutsu luo uuden jobin *eri* opiskeluoikeudelle, ei duplikaattia
        jobsAfter.count(_.opiskeluoikeusOid == firstJobOoOid) should equal(1)
      }
    }

    "Yrittää epäonnistunutta tiedotetta uudelleen" in {
      withoutRunningTiedoteScheduler {
        val repository = app.kielitutkintotodistusTiedoteRepository
        val eligible = repository.findNextEligible
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
        app.kielitutkintotodistusTiedoteService.retryFailed()

        val completedJobs = repository.findAll(10, 0, Some(KielitutkintotodistusTiedoteState.COMPLETED))
        completedJobs should have length 1
        mockTiedotuspalveluClient.sentNotifications should have length 1
      }
    }

    "Scheduler lähettää tiedotteen automaattisesti" in {
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
