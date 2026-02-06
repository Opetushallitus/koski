package fi.oph.koski.todistus

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema.KielitutkinnonOpiskeluoikeus

class TodistusKayttoOikeudetSpec extends TodistusSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  "Generointipyyntö ja statuspyyntö" - {
    "onnistuu kansalaiselta omasta kielitutkinnon opiskeluoikeudesta" in {
      val templateVariant = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          getStatusSuccessfully(todistusJob.id, hetu) { status =>
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }

    "onnistuu kansalaiselta omasta kielitutkinnon opiskeluoikeudesta, joka on tallennettu kansalaisen toisella oppija-oidilla" in {
      val templateVariant = "fi"
      // Master ja slave jakavat saman hetun, slave on linkitetty masteriin
      val masterHetu = KoskiSpecificMockOppijat.master.hetu.get
      val slaveOid = KoskiSpecificMockOppijat.slave.henkilö.oid

      // Luo opiskeluoikeus slave-oppijalle (linkitetty OID)
      val slaveOpiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.slave.henkilö, MockUsers.paakayttaja)
      val opiskeluoikeusOid = slaveOpiskeluoikeus.oid.get

      withoutRunningSchedulers {
        // Kirjaudu master-oppijana
        // Pyydä todistusta slave-oppijan opiskeluoikeudella
        val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

        addGenerateJobSuccessfully(req, masterHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob.oppijaOid should equal(slaveOid)
          todistusJob.opiskeluoikeusOid should equal(opiskeluoikeusOid)
        }
      }
    }

    "onnistuu huoltajalta huollettavan kielitutkinnon opiskeluoikeudesta" in {
      val templateVariant = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val kirjautujanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJobSuccessfully(req, kirjautujanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          getStatusSuccessfully(todistusJob.id, kirjautujanHetu) { status =>
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }

    "Onnistuu virkailijapääkäyttäjältä kielitutkinnon opiskeluoikeuteen" in {
      val templateVariant = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJobSuccessfullyAsVirkailijaPääkäyttäjä(req) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob.oppijaOid should equal(oppijaOid)
          todistusJob.opiskeluoikeusOid should equal(opiskeluoikeusOid)
        }
      }
    }
  }

  "Generointipyyntö ei onnistu" - {
    "kansalaiselta muuntyyppisten opintojen opiskeluoikeuteen" in {
      val templateVariant = "fi"
      val kirjautujanHetu = KoskiSpecificMockOppijat.lukiolainen.hetu.get
      val muidenOpintojenOpiskeluoikeus = getVahvistettuOpiskeluoikeus(KoskiSpecificMockOppijat.lukiolainen.oid)

      muidenOpintojenOpiskeluoikeus.get.isInstanceOf[KielitutkinnonOpiskeluoikeus] should be(false)

      val muidenOpintojenOpiskeluoikeusOid = muidenOpintojenOpiskeluoikeus.flatMap(_.oid).get

      val req = TodistusGenerateRequest(muidenOpintojenOpiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJob(req, kirjautujanHetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "kansalaiselta mitätöidystä opiskeluoikeudesta" in {
      val templateVariant = "fi"
      val oo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.eskari)
      val hetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val opiskeluoikeusOid = oo.oid.get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJob(req, hetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "kansalaiselta toisen oppijan kielitutkinnon opiskeluoikeuteen" in {
      val templateVariant = "fi"
      val kirjautujanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val toisenOpiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(toisenOpiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJob(req, kirjautujanHetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "huoltajalta toisen kansalaisen kielitutkinnon opiskeluoikeuteen" in {
      val templateVariant = "fi"
      val huoltajaKirjautujanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val toisenOpiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(toisenOpiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJob(req, huoltajaKirjautujanHetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "Virkailijakäyttäjältä (ei pääkäyttäjä)" in {
      val templateVariant = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        // Tavallinen virkailijä (ei pääkäyttäjä) ei pysty luomaan todistusta
        get(s"api/todistus/generate/${req.toPathParams}", headers = authHeaders(MockUsers.kalle) ++ jsonContent) {
          verifyResponseStatus(403)
        }
      }
    }
  }

  "Statuspyyntö onnistuu" - {

    "Huoltajalta huollettavan luomaan generointi-jobiin" in {
      val templateVariant = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJobSuccessfully(req, huollettavanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          getStatus(todistusJob.id, huoltajanHetu) {
            verifyResponseStatus(200)
          }
        }
      }
    }

    "Virkailijapääkäyttäjältä kansalaisen itsensä luomaan generointi-jobiin" in {
      val templateVariant = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        // Kansalainen luo todistuspyynnön
        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          // Virkailijapääkäyttäjä hakee statuksen
          getStatusSuccessfullyAsVirkailijaPääkäyttäjä(todistusJob.id) { status =>
            status.id should equal(todistusJob.id)
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }

    "Virkailijapääkäyttäjältä virkailijapääkäyttäjän luomaan generointi-jobiin" in {
      val templateVariant = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        // Virkailijapääkäyttäjä luo todistuspyynnön
        addGenerateJobSuccessfullyAsVirkailijaPääkäyttäjä(req) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          // Virkailijapääkäyttäjä hakee statuksen
          getStatusSuccessfullyAsVirkailijaPääkäyttäjä(todistusJob.id) { status =>
            status.id should equal(todistusJob.id)
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }
  }

  "Statuspyyntö ei onnistu" - {
    "oppijalta toisen oppijan luomaan generointi-jobiin" in {
      val templateVariant = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val toisenKansalaisenHetu = KoskiSpecificMockOppijat.eskari.hetu.get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      withoutRunningSchedulers {
        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          getStatus(todistusJob.id, toisenKansalaisenHetu) {
            verifyResponseStatus(404)
          }
        }
      }
    }
  }

  "Huoltajan luoma todistus ja oppijan omat oikeudet" - {
    "Oppija pääsee omiin todistuksiin, vaikka huoltaja olisi luonut pyynnön" - {
      "Status by id onnistuu" in {
        val templateVariant = "fi"
        val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
        val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
        val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

        val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, templateVariant)

        withoutRunningSchedulers {
          // Huoltaja luo todistuspyynnön
          val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
            todistusJob.state should equal(TodistusState.QUEUED)
            todistusJob
          }

          // Huollettava itse yrittää hakea statuksen ID:llä (ERILLINEN HTTP-kutsu)
          getStatusSuccessfully(todistusJob.id, huollettavanHetu) { status =>
            status.id should equal(todistusJob.id)
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }

      "Status parametreilla onnistuu" in {
        val templateVariant = "fi"
        val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
        val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
        val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

        val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, templateVariant)

        withoutRunningSchedulers {
          // Huoltaja luo todistuspyynnön
          val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
            todistusJob.state should equal(TodistusState.QUEUED)
            todistusJob
          }

          // Huollettava itse yrittää hakea statuksen parametreilla (ERILLINEN HTTP-kutsu)
          checkStatusByParametersSuccessfully(req, huollettavanHetu) { status =>
            status.id should equal(todistusJob.id)
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }

      "Download onnistuu kun todistus on valmis" in {
        val templateVariant = "fi"
        val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
        val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
        val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

        val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, templateVariant)

        // Huoltaja luo todistuspyynnön
        val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob
        }

        // Odotetaan todistuksen valmistumista (käyttäen huoltajan hetua pollaamaan)
        val completedJob = waitForCompletion(todistusJob.id, huoltajanHetu)
        completedJob.state should equal(TodistusState.COMPLETED)

        // Huollettava itse yrittää ladata todistuksen
        verifyDownloadResult(s"/todistus/download/${todistusJob.id}", huollettavanHetu)
      }
    }
  }

  "Mitätöidyn opiskeluoikeuden todistuksen lataus" - {
    "estyy jos opiskeluoikeus on mitätöity todistuksen luomisen jälkeen" in {
      val templateVariant = "fi"
      val oppija = KoskiSpecificMockOppijat.eskari
      val hetu = oppija.hetu.get

      // Luo opiskeluoikeus ja todistus
      val oo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, oppija, MockUsers.paakayttaja)
      val opiskeluoikeusOid = oo.oid.get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Mitätöi opiskeluoikeus todistuksen luomisen jälkeen
      mitätöiOppijanKaikkiOpiskeluoikeudet(oppija)

      // Yritä ladata todistus - pitäisi epäonnistua
      getResult(s"/todistus/download/${todistusJob.id}", hetu) {
        verifyResponseStatus(503)
      }
    }
  }

  "Todistuksen latauksen käyttöoikeudet" - {
    "Kansalainen ei voi käyttää presigned URL endpointtiä" in {
      val templateVariant = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Kansalainen yrittää käyttää presigned URL endpointtiä - pitäisi estää
      getResult(s"/todistus/download/presigned/${todistusJob.id}", hetu) {
        verifyResponseStatus(403) // Forbidden
      }

      // Varmista että normaali download toimii
      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", hetu)
    }

    "Kansalainen ei pääse lataamaan toisen oppijan todistusta" in {
      val templateVariant = "fi"
      val todistuksenOmistajaHetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val todistuksenOmistajaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val toinenKansalainenHetu = KoskiSpecificMockOppijat.eskari.hetu.get

      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(todistuksenOmistajaOid).flatMap(_.oid).get
      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      // Todistuksen omistaja luo todistuksen
      val todistusJob = addGenerateJobSuccessfully(req, todistuksenOmistajaHetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, todistuksenOmistajaHetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", todistuksenOmistajaHetu)

      // Toinen kansalainen yrittää ladata todistuksen
      getResult(s"/todistus/download/${todistusJob.id}", toinenKansalainenHetu) {
        verifyResponseStatus(404)
      }
    }

    "Huoltaja ei pääse lataamaan muiden kuin huollettaviensa todistuksia" in {
      val templateVariant = "fi"
      val todistuksenOmistajaHetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val todistuksenOmistajaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get

      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(todistuksenOmistajaOid).flatMap(_.oid).get
      val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)

      // Todistuksen omistaja luo todistuksen
      val todistusJob = addGenerateJobSuccessfully(req, todistuksenOmistajaHetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, todistuksenOmistajaHetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Huoltaja (joka ei ole tämän oppijan huoltaja) yrittää ladata todistuksen
      getResult(s"/todistus/download/${todistusJob.id}", huoltajanHetu) {
        verifyResponseStatus(404)
      }
    }
  }


  "HTML preview endpoint" - {
    "onnistuu OPH-pääkäyttäjältä" in {
      val templateVariant = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      get(s"todistus/preview/$templateVariant/$opiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatusOk()
        response.header("Content-Type") should include("text/html")

        val html = response.body

        // Tarkista että HTML sisältää oikeat tiedot
        html should include("Kielitutkinto Suorittaja")
        html should include("1.1.2007") // syntymäaika
        html should include("suomen kielen keskitason")
        html should include("Varsinais-Suomen kansanopisto")
        html should include("Tekstin ymmärtäminen")
        html should include("Kirjoittaminen")
        html should include("Puheen ymmärtäminen")
        html should include("Puhuminen")
      }
    }

    "ei onnistu kansalaiselta" in {
      val templateVariant = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      get(s"todistus/preview/$templateVariant/$opiskeluoikeusOid", headers = kansalainenLoginHeaders(hetu)) {
        verifyResponseStatus(403) // Forbidden
      }
    }

    "ei onnistu tavalliselta virkailijalta (ei pääkäyttäjä)" in {
      val templateVariant = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      get(s"todistus/preview/$templateVariant/$opiskeluoikeusOid", headers = authHeaders(MockUsers.kalle)) {
        verifyResponseStatus(403) // Forbidden
      }
    }

    "palauttaa 404 jos opiskeluoikeus ei ole kielitutkinto" in {
      val templateVariant = "fi"
      val muidenOpintojenOpiskeluoikeus = getVahvistettuOpiskeluoikeus(KoskiSpecificMockOppijat.lukiolainen.oid)
      val muidenOpintojenOpiskeluoikeusOid = muidenOpintojenOpiskeluoikeus.flatMap(_.oid).get

      get(s"todistus/preview/$templateVariant/$muidenOpintojenOpiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatus(404)
      }
    }

    "palauttaa 404 jos opiskeluoikeus on mitätöity" in {
      val templateVariant = "fi"
      val oo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.eskari)
      val opiskeluoikeusOid = oo.oid.get

      get(s"todistus/preview/$templateVariant/$opiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatus(404)
      }
    }
  }

}
