package fi.oph.koski.todistus

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema.{YleisenKielitutkinnonSuoritus}

import java.time.LocalDateTime

class TodistusWorkflowSpec extends TodistusSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  "Todistuksen uudelleenkäyttö" - {
    "Palauttaa olemassaolevan COMPLETED-todistuksen, jos sama sisältö" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo ensimmäinen todistus ja odota sen valmistumista
      val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedFirstJob = waitForCompletion(firstJob.id, hetu)
      completedFirstJob.state should equal(TodistusState.COMPLETED)

      // Pyydä samaa todistusta uudestaan
      val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob
      }

      // Toisen pyynnön pitäisi palauttaa sama job
      secondJob.id should equal(firstJob.id)
      secondJob.state should equal(TodistusState.COMPLETED)
    }

    "Palauttaa olemassaolevan QUEUED-todistuksen, jos sama sisältö" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Luo ensimmäinen todistus (jää QUEUED-tilaan)
        val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob
        }

        // Pyydä samaa todistusta uudestaan
        val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob
        }

        // Toisen pyynnön pitäisi palauttaa sama job
        secondJob.id should equal(firstJob.id)
        secondJob.state should equal(TodistusState.QUEUED)
      }
    }

    "Luo uuden todistuksen, jos aiempi on ERROR-tilassa" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Luo ensimmäinen todistus
        val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob
        }

        // Merkitse se epäonnistuneeksi
        app.todistusRepository.setJobFailed(firstJob.id, "Testissä luotu virhe")
        val errorJob = app.todistusRepository.getFromDbForUnitTests(firstJob.id).get
        errorJob.state should equal(TodistusState.ERROR)

        // Pyydä samaa todistusta uudestaan
        val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob
        }

        // Toisen pyynnön pitäisi luoda uusi job
        secondJob.id should not equal firstJob.id
        secondJob.state should equal(TodistusState.QUEUED)
      }
    }

    "Luo uuden todistuksen, jos opiskeluoikeus-versionumero on muuttunut" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo ensimmäinen todistus ja odota sen valmistumista
      val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedFirstJob = waitForCompletion(firstJob.id, hetu)
      completedFirstJob.state should equal(TodistusState.COMPLETED)

      // Päivitä opiskeluoikeutta, jolloin versionumero kasvaa
      val oo = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).get
      val paivitettyOo = oo.copy(
        tila = oo.tila.copy(opiskeluoikeusjaksot = oo.tila.opiskeluoikeusjaksot.map(
          j => j.copy(alku = j.alku.plusDays(1))
        )),
        suoritukset = oo.suoritukset.map(s => {
          val ks = s.asInstanceOf[YleisenKielitutkinnonSuoritus]
          ks.copy(vahvistus = ks.vahvistus.map(_.copy(päivä = ks.vahvistus.map(_.päivä).map(_.plusDays(1)).get)))
        })
      )
      putOpiskeluoikeus(paivitettyOo, henkilö = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      // Pyydä todistusta uudestaan
      val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob
      }

      // Toisen pyynnön pitäisi luoda uusi job eri versionumerolla
      secondJob.id should not equal firstJob.id
      val completedSecondJob = waitForCompletion(secondJob.id, hetu)
      completedSecondJob.state should equal(TodistusState.COMPLETED)
    }

    "Luo uuden todistuksen, jos aiempi on EXPIRED-tilassa" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo ensimmäinen todistus ja odota sen valmistumista
      val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedFirstJob = waitForCompletion(firstJob.id, hetu)
      completedFirstJob.state should equal(TodistusState.COMPLETED)

      // Merkitse todistus EXPIRED-tilaan (simuloi vanhenemisprosessia)
      app.todistusRepository.setJobExpiredForUnitTests(firstJob.id)
      val expiredJobFromDb = app.todistusRepository.getFromDbForUnitTests(firstJob.id).get
      expiredJobFromDb.state should equal(TodistusState.EXPIRED)

      // Pyydä samaa todistusta uudestaan
      val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob
      }

      // Toisen pyynnön pitäisi luoda uusi job, koska EXPIRED-tilassa olevaa ei uudelleenkäytetä
      secondJob.id should not equal firstJob.id
      secondJob.state should equal(TodistusState.QUEUED)
    }

    "Luo uuden todistuksen, jos aiempi on QUEUED_FOR_EXPIRE-tilassa" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo ensimmäinen todistus ja odota sen valmistumista
      val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedFirstJob = waitForCompletion(firstJob.id, hetu)
      completedFirstJob.state should equal(TodistusState.COMPLETED)

      // Merkitse todistus QUEUED_FOR_EXPIRE-tilaan (simuloi vanhenemisjonoon merkitsemistä)
      app.todistusRepository.setJobQueuedForExpireForUnitTests(firstJob.id)
      val queuedForExpireJobFromDb = app.todistusRepository.getFromDbForUnitTests(firstJob.id).get
      queuedForExpireJobFromDb.state should equal(TodistusState.QUEUED_FOR_EXPIRE)

      // Pyydä samaa todistusta uudestaan
      val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob
      }

      // Toisen pyynnön pitäisi luoda uusi job, koska QUEUED_FOR_EXPIRE-tilassa olevaa ei uudelleenkäytetä
      secondJob.id should not equal firstJob.id
      secondJob.state should equal(TodistusState.QUEUED)
    }
  }

  "markAllMyJobsInterrupted merkitsee jobin keskeytetyksi" - {
    val lang = "fi"
    val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
    val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

    withoutRunningSchedulers {
      // Luo job joka on käynnissä tällä workerilla
      val activeWorkerId = app.todistusRepository.workerId
      val job = TodistusJob(
        id = java.util.UUID.randomUUID().toString,
        userOid = Some(oppijaOid),
        oppijaOid = oppijaOid,
        opiskeluoikeusOid = opiskeluoikeusOid,
        language = lang,
        opiskeluoikeusVersionumero = Some(1),
        oppijaHenkilötiedotHash = Some("test-hash"),
        state = TodistusState.STAMPING_PDF,
        createdAt = LocalDateTime.now(),
        startedAt = Some(LocalDateTime.now()),
        completedAt = None,
        worker = Some(activeWorkerId),
        attempts = Some(1),
        error = None
      )
      app.todistusRepository.addRawForUnitTests(job)

      // Merkitse kaikki tämän workerin jobit keskeytetyiksi
      app.todistusService.markAllMyJobsInterrupted()

      // Varmista että job on nyt INTERRUPTED tilassa
      val interruptedJob = app.todistusRepository.getFromDbForUnitTests(job.id).get
      interruptedJob.state should equal(TodistusState.INTERRUPTED)
      interruptedJob.worker should equal(Some(activeWorkerId))

      job // Palauta job jotta voidaan käyttää lohkon ulkopuolella
    }
  }

  "Todistusten vanheneminen" - {
    "Merkitsee vanhentuneet todistukset QUEUED_FOR_EXPIRE-tilaan" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo todistus ja odota sen valmistumista
      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Merkitse todistus vanhaksi muuttamalla completed_at-aikaleimaa tietokannassa
      val expirationDuration = app.config.getDuration("todistus.expirationDuration")
      val oldCompletedAt = LocalDateTime.now().minusSeconds(expirationDuration.getSeconds).minusSeconds(1)
      app.todistusRepository.setCompletedAtForUnitTests(todistusJob.id, oldCompletedAt)

      // Odota että cleanup-scheduler käsittelee vanhentuneen todistuksen
      Thread.sleep(3000) // cleanupInterval on 2s

      // Varmista että todistus on nyt QUEUED_FOR_EXPIRE-tilassa
      val expiredJob = app.todistusRepository.getFromDbForUnitTests(todistusJob.id).get
      expiredJob.state should equal(TodistusState.QUEUED_FOR_EXPIRE)
    }

    "Ei merkitse QUEUED_FOR_EXPIRE-tilaan, jos todistus ei ole vielä vanhentunut" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo todistus ja odota sen valmistumista
      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Odota cleanup-schedulerin käynnistymistä
      Thread.sleep(3000) // cleanupInterval on 2s

      // Varmista että todistus on edelleen COMPLETED-tilassa (ei vanhentunut)
      val stillValidJob = app.todistusRepository.getFromDbForUnitTests(todistusJob.id).get
      stillValidJob.state should equal(TodistusState.COMPLETED)
    }

    "Merkitsee useita vanhentuneita todistuksia kerralla" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      // Luo useita todistuksia eri kielillä
      val reqFi = TodistusGenerateRequest(opiskeluoikeusOid, "fi")
      val reqSv = TodistusGenerateRequest(opiskeluoikeusOid, "sv")
      val reqEn = TodistusGenerateRequest(opiskeluoikeusOid, "en")

      val jobFi = addGenerateJobSuccessfully(reqFi, hetu) { todistusJob => todistusJob }
      val completedFi = waitForCompletion(jobFi.id, hetu)

      val jobSv = addGenerateJobSuccessfully(reqSv, hetu) { todistusJob => todistusJob }
      val completedSv = waitForCompletion(jobSv.id, hetu)

      val jobEn = addGenerateJobSuccessfully(reqEn, hetu) { todistusJob => todistusJob }
      val completedEn = waitForCompletion(jobEn.id, hetu)

      // Merkitse kaikki todistukset vanhoiksi
      val expirationDuration = app.config.getDuration("todistus.expirationDuration")
      val oldCompletedAt = LocalDateTime.now().minusSeconds(expirationDuration.getSeconds).minusSeconds(1)

      Seq(jobFi.id, jobSv.id, jobEn.id).foreach { id =>
        app.todistusRepository.setCompletedAtForUnitTests(id, oldCompletedAt)
      }

      // Odota cleanup-schedulerin käynnistymistä
      Thread.sleep(3000)

      // Varmista että kaikki todistukset ovat QUEUED_FOR_EXPIRE-tilassa
      val expiredFi = app.todistusRepository.getFromDbForUnitTests(jobFi.id).get
      val expiredSv = app.todistusRepository.getFromDbForUnitTests(jobSv.id).get
      val expiredEn = app.todistusRepository.getFromDbForUnitTests(jobEn.id).get

      expiredFi.state should equal(TodistusState.QUEUED_FOR_EXPIRE)
      expiredSv.state should equal(TodistusState.QUEUED_FOR_EXPIRE)
      expiredEn.state should equal(TodistusState.QUEUED_FOR_EXPIRE)
    }
  }

  "Orpojen todistusjobien uudelleenkäynnistys" - {

    "Ei koske aktiivisesti ajossa olevaan jobiin" in {
      val lang = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      // Luo job oikealla workerIdllä (tämän instanssin workerId)
      val activeWorkerId = app.todistusRepository.workerId
      val activeJob = TodistusJob(
        id = java.util.UUID.randomUUID().toString,
        userOid = Some(oppijaOid),
        oppijaOid = oppijaOid,
        opiskeluoikeusOid = opiskeluoikeusOid,
        language = lang,
        opiskeluoikeusVersionumero = Some(1),
        oppijaHenkilötiedotHash = Some("test-hash"),
        state = TodistusState.GENERATING_RAW_PDF,
        createdAt = LocalDateTime.now(),
        startedAt = Some(LocalDateTime.now()),
        completedAt = None,
        worker = Some(activeWorkerId),
        attempts = Some(1),
        error = None
      )
      app.todistusRepository.addRawForUnitTests(activeJob)

      try {
        // Odota cleanup-schedulerin käynnistymistä
        Thread.sleep(3000) // cleanupInterval on 2s

        // Varmista että jobin tila ei ole muuttunut
        val jobAfterCleanup = app.todistusRepository.getFromDbForUnitTests(activeJob.id).get
        jobAfterCleanup.state should equal(TodistusState.GENERATING_RAW_PDF)
        jobAfterCleanup.worker should equal(Some(activeWorkerId))
        jobAfterCleanup.attempts should equal(Some(1)) // Ei ole kasvanut
      } finally {
        // Siivoa: merkitse job valmiiksi jotta afterEach ei jää odottamaan
        app.todistusRepository.updateState(activeJob.id, TodistusState.GENERATING_RAW_PDF, TodistusState.COMPLETED)
      }
    }

    "Uudelleenkäynnistää orpo-jobin (attempts < 3) ja ajaa sen loppuun" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val orphanJob = createOrphanJob(oppijaOid, opiskeluoikeusOid, lang, attempts = 1)

      val completedJob = waitForCompletion(orphanJob.id, hetu)
      val completedJobFromDb = app.todistusRepository.getFromDbForUnitTests(orphanJob.id).get

      completedJob.state should equal(TodistusState.COMPLETED)
      completedJobFromDb.attempts.get should be > orphanJob.attempts.get
      completedJob.error should be(None)
    }


    "Uudelleenkäynnistää INTERRUPTED-tilaan merkityn jobin" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val runningJob = withoutRunningSchedulers(truncate = false) {
        // Luo job joka on käynnissä toisella workerilla
        val activeWorkerId = app.todistusRepository.workerId
        val job = TodistusJob(
          id = java.util.UUID.randomUUID().toString,
          userOid = Some(oppijaOid),
          oppijaOid = oppijaOid,
          opiskeluoikeusOid = opiskeluoikeusOid,
          language = lang,
          opiskeluoikeusVersionumero = Some(1),
          oppijaHenkilötiedotHash = Some("test-hash"),
          state = TodistusState.INTERRUPTED,
          createdAt = LocalDateTime.now(),
          startedAt = Some(LocalDateTime.now()),
          completedAt = None,
          worker = Some("dummy-other-worker"),
          attempts = Some(1),
          error = None
        )
        app.todistusRepository.addRawForUnitTests(job)

        // Varmista että job on nyt INTERRUPTED tilassa
        val interruptedJob = app.todistusRepository.getFromDbForUnitTests(job.id).get
        interruptedJob.state should equal(TodistusState.INTERRUPTED)

        job // Palauta job jotta voidaan käyttää lohkon ulkopuolella
      }

      // Cleanup-scheduler käsittelee INTERRUPTED-jobin ja uudelleenkäynnistää sen
      val completedJob = waitForCompletionSkipStateChecks(runningJob.id, hetu)
      val completedJobFromDb = app.todistusRepository.getFromDbForUnitTests(runningJob.id).get

      // Varmista että job valmistui onnistuneesti
      completedJob.state should equal(TodistusState.COMPLETED)
      completedJobFromDb.attempts.get should be > runningJob.attempts.get
      completedJob.error should be(None)
    }

    "Siirtää orpo-jobin (attempts >= 3) ERROR-tilaan" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val orphanJob = createOrphanJob(oppijaOid, opiskeluoikeusOid, lang, attempts = 3)

      val errorJob = waitForError(orphanJob.id, hetu)
      val errorJobFromDb = app.todistusRepository.getFromDbForUnitTests(orphanJob.id).get

      errorJob.state should equal(TodistusState.ERROR)
      errorJobFromDb.error should not be empty
      errorJobFromDb.error.get should include("epäonnistui 3 yrityksen jälkeen")
    }

    "Käsittelee sekä uudelleenkäynnistettävät että ERROR-tilaan siirrettävät orpo-jobit oikein" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val restartableOrphan = createOrphanJob(oppijaOid, opiskeluoikeusOid, lang, attempts = 1)
      val failableOrphan = createOrphanJob(oppijaOid, opiskeluoikeusOid, lang, attempts = 3)

      val completedJob = waitForCompletion(restartableOrphan.id, hetu)
      val errorJob = waitForError(failableOrphan.id, hetu)

      val completedJobFromDb = app.todistusRepository.getFromDbForUnitTests(restartableOrphan.id).get
      val errorJobFromDb = app.todistusRepository.getFromDbForUnitTests(failableOrphan.id).get

      completedJob.state should equal(TodistusState.COMPLETED)
      completedJobFromDb.attempts.get should be > restartableOrphan.attempts.get
      errorJob.state should equal(TodistusState.ERROR)
      errorJobFromDb.error should not be empty
    }
  }

  "Status-endpoint" - {
    "Palauttaa ajantasaisen COMPLETED-todistuksen jos saatavilla" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo todistus ja odota sen valmistumista
      val job = addGenerateJobSuccessfully(req, hetu) { todistusJob => todistusJob }
      val completedJob = waitForCompletion(job.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Kutsu status-endpointia parametreilla
      checkStatusByParametersSuccessfully(req, hetu) { statusJob =>
        statusJob.id should equal(completedJob.id)
        statusJob.state should equal(TodistusState.COMPLETED)
      }
    }

    "Palauttaa ajantasaisen QUEUED-todistuksen jos saatavilla" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Luo todistus joka jää QUEUED-tilaan
        val job = addGenerateJobSuccessfully(req, hetu) { todistusJob => todistusJob }
        job.state should equal(TodistusState.QUEUED)

        // Kutsu status-endpointia parametreilla
        checkStatusByParametersSuccessfully(req, hetu) { statusJob =>
          statusJob.id should equal(job.id)
          statusJob.state should equal(TodistusState.QUEUED)
        }
      }
    }

    "Palauttaa 404 jos ei ajantasaista todistusta" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Kutsu status-endpointia kun ei todistusta olemassa
      checkStatusByParameters(req, hetu) {
        verifyResponseStatus(404)
      }
    }

    "Palauttaa 404 jos vain ERROR-tilassa olevia todistuksia" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Luo todistus ja merkitse se epäonnistuneeksi
        val job = addGenerateJobSuccessfully(req, hetu) { todistusJob => todistusJob }
        app.todistusRepository.setJobFailed(job.id, "Testi-virhe")

        // Kutsu status-endpointia
        checkStatusByParameters(req, hetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "Palauttaa 404 jos versionumero on muuttunut" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo todistus ja odota sen valmistumista
      val job = addGenerateJobSuccessfully(req, hetu) { todistusJob => todistusJob }
      val completedJob = waitForCompletion(job.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Päivitä opiskeluoikeutta, jolloin versionumero kasvaa
      val oo = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).get
      val paivitettyOo = oo.copy(
        tila = oo.tila.copy(opiskeluoikeusjaksot = oo.tila.opiskeluoikeusjaksot.map(
          j => j.copy(alku = j.alku.plusDays(1))
        )),
        suoritukset = oo.suoritukset.map(s => {
          val ks = s.asInstanceOf[YleisenKielitutkinnonSuoritus]
          ks.copy(vahvistus = ks.vahvistus.map(_.copy(päivä = ks.vahvistus.map(_.päivä).map(_.plusDays(1)).get)))
        })
      )
      putOpiskeluoikeus(paivitettyOo, henkilö = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      // Kutsu status-endpointia - pitäisi palauttaa 404 koska versionumero muuttunut
      checkStatusByParameters(req, hetu) {
        verifyResponseStatus(404)
      }
    }

    "Palauttaa viimeisimmän jobin kun useita ajantasaisia jobeja saatavilla" in {
      val lang = "fi"
      val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
      val hetu = oppija.hetu.get
      val oppijaOid = oppija.oid
      val opiskeluoikeus = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid)
      val opiskeluoikeusOid = opiskeluoikeus.flatMap(_.oid).get
      val versionumero = opiskeluoikeus.flatMap(_.versionumero).get
      val henkilotiedotHash = laskeHenkilötiedotHash(oppija)

      withoutRunningSchedulers {
        // Luo kolme identtistä jobia suoraan tietokantaan eri ajanhetkinä (simuloi race conditionia)
        val count = 3
        val now = LocalDateTime.now()
        val minutesApart = 5
        val jobs =
          (0 until count).map { i =>
            val createdAt = now.minusMinutes(minutesApart * (count - 1 - i).toLong)
            val job = TodistusJob(
              id = java.util.UUID.randomUUID().toString,
              userOid = Some(oppijaOid),
              oppijaOid = oppijaOid,
              opiskeluoikeusOid = opiskeluoikeusOid,
              language = lang,
              opiskeluoikeusVersionumero = Some(versionumero),
              oppijaHenkilötiedotHash = Some(henkilotiedotHash),
              state = TodistusState.COMPLETED,
              createdAt = createdAt,
              startedAt = Some(createdAt),
              completedAt = Some(createdAt.plusMinutes(1)),
              worker = Some(app.todistusRepository.workerId),
              attempts = Some(1),
              error = None
            )
            app.todistusRepository.addRawForUnitTests(job)
          }

        val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

        checkStatusByParametersSuccessfully(req, hetu) { statusJob =>
          statusJob.id should equal(jobs.last.id)
          statusJob.state should equal(TodistusState.COMPLETED)
        }
      }
    }
  }

}
