package fi.oph.koski.todistus.tiedote

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.util.Wait

class TiedoteApiSpec extends KielitutkintotodistusTiedoteSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  "Tiedote Admin API" - {
    "GET /jobs" - {
      "OPH-pääkäyttäjä saa listan tiedotteista" in {
        withoutRunningTiedoteScheduler {
          app.kielitutkintotodistusTiedoteService.processAll()

          get("api/tiedote/jobs", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
            verifyResponseStatusOk()
            response.body should include(KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid)
          }
        }
      }

      "Tavallinen käyttäjä saa 403" in {
        get("api/tiedote/jobs", headers = authHeaders(MockUsers.stadinAmmattiopistoKatselija) ++ jsonContent) {
          verifyResponseStatus(403)
        }
      }

      "Tukee state-suodatusta" in {
        withoutRunningTiedoteScheduler {
          app.kielitutkintotodistusTiedoteService.processAll()

          get("api/tiedote/jobs?state=COMPLETED", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
            verifyResponseStatusOk()
            response.body should include("COMPLETED")
          }

          get("api/tiedote/jobs?state=ERROR", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
            verifyResponseStatusOk()
            response.body should equal("[]")
          }
        }
      }
    }

    "GET /stats" - {
      "OPH-pääkäyttäjä saa tilakohtaiset lukumäärät" in {
        withoutRunningTiedoteScheduler {
          app.kielitutkintotodistusTiedoteService.processAll()

          get("api/tiedote/stats", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
            verifyResponseStatusOk()
            response.body should include("COMPLETED")
          }
        }
      }

      "Tavallinen käyttäjä saa 403" in {
        get("api/tiedote/stats", headers = authHeaders(MockUsers.stadinAmmattiopistoKatselija) ++ jsonContent) {
          verifyResponseStatus(403)
        }
      }
    }

    "GET /run" - {
      "OPH-pääkäyttäjä voi käynnistää batch-ajon heti" in {
        withoutRunningTiedoteScheduler {
          get("api/tiedote/run", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
            verifyResponseStatusOk()
            response.body should include("triggered")
          }

          Wait.until {
            app.kielitutkintotodistusTiedoteRepository.findAll(100, 0).nonEmpty
          }
        }
      }

      "Tavallinen käyttäjä saa 403" in {
        get("api/tiedote/run", headers = authHeaders(MockUsers.stadinAmmattiopistoKatselija) ++ jsonContent) {
          verifyResponseStatus(403)
        }
      }
    }

    "DELETE /jobs/:opiskeluoikeusOid" - {
      "OPH-pääkäyttäjä voi poistaa tiedotejobin" in {
        withoutRunningTiedoteScheduler {
          app.kielitutkintotodistusTiedoteService.processAll()

          val jobs = app.kielitutkintotodistusTiedoteRepository.findAll(1, 0)
          jobs should not be empty

          val job = jobs.head
          delete(s"api/tiedote/jobs/${job.opiskeluoikeusOid}", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
            verifyResponseStatusOk()
            response.body should include(job.opiskeluoikeusOid)
          }

          // Varmistetaan, että jobin tila on DELETED
          val updatedJobs = app.kielitutkintotodistusTiedoteRepository.findAll(100, 0)
          updatedJobs.find(_.opiskeluoikeusOid == job.opiskeluoikeusOid).map(_.state) should equal(Some("DELETED"))
        }
      }

      "Palauttaa 404 jos tiedotetta ei löydy" in {
        delete("api/tiedote/jobs/1.2.246.562.15.00000000000", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
          verifyResponseStatus(404)
        }
      }

      "Tavallinen käyttäjä saa 403" in {
        delete("api/tiedote/jobs/1.2.246.562.15.00000000000", headers = authHeaders(MockUsers.stadinAmmattiopistoKatselija) ++ jsonContent) {
          verifyResponseStatus(403)
        }
      }
    }
  }
}
