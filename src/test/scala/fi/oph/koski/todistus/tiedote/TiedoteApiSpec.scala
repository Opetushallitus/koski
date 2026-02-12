package fi.oph.koski.todistus.tiedote

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers

class TiedoteApiSpec extends KielitutkintotodistusTiedoteSpecHelpers {

  override protected def afterEach(): Unit = {
    cleanup()
  }

  "Tiedote Admin API" - {
    "GET /jobs" - {
      "OPH-pääkäyttäjä saa listan tiedotteista" in {
        withoutRunningTiedoteScheduler {
          app.kielitutkintotodistusTiedoteService.processNext()

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
          app.kielitutkintotodistusTiedoteService.processNext()

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
          app.kielitutkintotodistusTiedoteService.processNext()

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

    "POST /run" - {
      "OPH-pääkäyttäjä voi käynnistää batch-ajon heti" in {
        withoutRunningTiedoteScheduler {
          post("api/tiedote/run", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
            verifyResponseStatusOk()
            response.body should include("processed")
          }

          val jobs = app.kielitutkintotodistusTiedoteRepository.findAll(100, 0)
          jobs.length should be > 0
        }
      }

      "Tavallinen käyttäjä saa 403" in {
        post("api/tiedote/run", headers = authHeaders(MockUsers.stadinAmmattiopistoKatselija) ++ jsonContent) {
          verifyResponseStatus(403)
        }
      }
    }
  }
}
