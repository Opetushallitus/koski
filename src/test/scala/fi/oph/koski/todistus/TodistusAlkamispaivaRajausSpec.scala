package fi.oph.koski.todistus

import fi.oph.koski.documentation.ExamplesKielitutkinto
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers

import java.time.{LocalDate, LocalDateTime}

/**
 * Todistuksen lataus on sallittu vain kokeille, joiden opiskeluoikeuden alkamispäivä
 * (YKI:ssä tutkintopäivä) on aikaisintaan konfiguroitu rajapäivä
 * todistus.yleinenKielitutkinto.earliestDate
 * (testeissä 2010-01-01). Rajoitus koskee kaikkia käyttäjärooleja.
 */
class TodistusAlkamispaivaRajausSpec extends TodistusSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  private val ennenRajapäivääOpiskeluoikeus =
    ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2009, 6, 1), "FI", "kt")

  private val rajapäivänäOpiskeluoikeus =
    ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2010, 1, 1), "FI", "kt")

  "Ennen rajapäivää alkanut kielitutkinnon opiskeluoikeus" - {
    "generointipyyntö ei onnistu kansalaiselta" in {
      withoutRunningSchedulers {
        val oo = setupOppijaWithAndGetOpiskeluoikeus(ennenRajapäivääOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val req = TodistusGenerateRequest(oo.oid.get, "fi")

        addGenerateJob(req, KoskiSpecificMockOppijat.eskari.hetu.get) {
          verifyResponseStatus(404)
        }
      }
    }

    "generointipyyntö ei onnistu OPH-pääkäyttäjältä" in {
      withoutRunningSchedulers {
        val oo = setupOppijaWithAndGetOpiskeluoikeus(ennenRajapäivääOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val req = TodistusGenerateRequest(oo.oid.get, "fi")

        addGenerateJobAsVirkailijaPääkäyttäjä(req) {
          verifyResponseStatus(404)
        }
      }
    }

    "generointipyyntö ei onnistu yleisen kielitutkinnon katselijalta" in {
      withoutRunningSchedulers {
        val oo = setupOppijaWithAndGetOpiskeluoikeus(ennenRajapäivääOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val req = TodistusGenerateRequest(oo.oid.get, "fi")

        get(s"api/todistus/generate/${req.toPathParams}", headers = authHeaders(MockUsers.yleisenKielitutkinnonKäyttäjä) ++ jsonContent) {
          verifyResponseStatus(404)
        }
      }
    }

    "statuspyyntö parametreilla ei onnistu, vaikka job olisi olemassa" in {
      withoutRunningSchedulers {
        val oppija = KoskiSpecificMockOppijat.eskari
        val oo = setupOppijaWithAndGetOpiskeluoikeus(ennenRajapäivääOpiskeluoikeus, oppija, MockUsers.paakayttaja)
        val job = lisääValmisTodistusJob(oppija.oid, oo.oid.get, oo.versionumero.get, "fi")
        val req = TodistusGenerateRequest(oo.oid.get, job.templateVariant)

        checkStatusByParameters(req, oppija.hetu.get) {
          verifyResponseStatus(404)
        }
      }
    }

    "HTML-esikatselu ei onnistu OPH-pääkäyttäjältä" in {
      withoutRunningSchedulers {
        val oo = setupOppijaWithAndGetOpiskeluoikeus(ennenRajapäivääOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)

        get(s"todistus/preview/fi/${oo.oid.get}", headers = authHeaders(MockUsers.paakayttaja)) {
          verifyResponseStatus(404)
        }
      }
    }

    "olemassa olevan valmiin todistuksen lataus ei onnistu" in {
      withoutRunningSchedulers {
        val oppija = KoskiSpecificMockOppijat.eskari
        val oo = setupOppijaWithAndGetOpiskeluoikeus(ennenRajapäivääOpiskeluoikeus, oppija, MockUsers.paakayttaja)
        val job = lisääValmisTodistusJob(oppija.oid, oo.oid.get, oo.versionumero.get, "fi")

        getResult(s"/todistus/download/${job.id}", oppija.hetu.get) {
          verifyResponseStatus(404)
        }
      }
    }
  }

  "Täsmälleen rajapäivänä alkanut kielitutkinnon opiskeluoikeus" - {
    "generointipyyntö onnistuu kansalaiselta" in {
      withoutRunningSchedulers {
        val oo = setupOppijaWithAndGetOpiskeluoikeus(rajapäivänäOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val req = TodistusGenerateRequest(oo.oid.get, "fi")

        addGenerateJobSuccessfully(req, KoskiSpecificMockOppijat.eskari.hetu.get) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
        }
      }
    }
  }

  /**
   * Lisää COMPLETED-tilaisen todistus-jobin suoraan kantaan ohi generointipyynnön
   * rajapäivätarkistuksen. Vastaa tilannetta, jossa job on syntynyt ennen rajoituksen
   * käyttöönottoa. PDF:ää ei tallenneta S3:een: latauksen pitää estyä jo ennen S3-hakua.
   */
  private def lisääValmisTodistusJob(oppijaOid: String, opiskeluoikeusOid: String, versionumero: Int, templateVariant: String): TodistusJob = {
    val henkilö = app.henkilöRepository.findByOid(oppijaOid).get
    val job = TodistusJob(
      id = java.util.UUID.randomUUID().toString,
      userOid = Some(oppijaOid),
      oppijaOid = oppijaOid,
      opiskeluoikeusOid = opiskeluoikeusOid,
      templateVariant = templateVariant,
      opiskeluoikeusVersionumero = Some(versionumero),
      oppijaHenkilötiedotHash = Some(laskeHenkilötiedotHash(henkilö)),
      isStamped = !TodistusTemplateVariant.printVariants.contains(templateVariant),
      state = TodistusState.COMPLETED,
      createdAt = LocalDateTime.now(),
      startedAt = Some(LocalDateTime.now()),
      completedAt = Some(LocalDateTime.now()),
      worker = Some("test-worker"),
      attempts = Some(1),
      error = None
    )
    app.todistusRepository.addRawForUnitTests(job)
  }
}
