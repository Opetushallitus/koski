package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.db.OpiskeluOikeusHistoryRow
import fi.oph.koski.documentation.AmmatillinenOldExamples
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.{Opiskeluoikeus, TäydellisetHenkilötiedot}
import org.scalatest.FunSpec

class HistoryApiSpec extends FunSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val uusiOpiskeluOikeus = defaultOpiskeluoikeus
  val oppija: TäydellisetHenkilötiedot = MockOppijat.tyhjä

  describe("Muutoshistoria") {
    describe("Luotaessa uusi opiskeluoikeus") {
      it("Luodaan historiarivi") {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
        verifyHistory(oppija, opiskeluOikeus, List(1))
      }

      it("osasuorituksilla") {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, AmmatillinenOldExamples.full.opiskeluoikeudet(0))
        verifyHistory(oppija, opiskeluOikeus, List(1))
      }
    }
    describe("Päivitettäessä") {
      it("Luodaan uusi versiorivi") {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
        val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluOikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now)))
        verifyHistory(oppija, modified, List(1, 2))
      }

      describe("Jos mikään ei ole muuttunut") {
        it("Ei luoda uutta versioriviä") {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
          val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluOikeus)
          verifyHistory(oppija, modified, List(1))
        }
      }

      describe("Kun syötteessä annetaan versionumero") {
        describe("Versionumero sama kuin viimeisin") {
          it("Päivitys hyväksytään") {
            val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
            val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluOikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now), versionumero = Some(1)))
            verifyHistory(oppija, modified, List(1, 2))
          }
        }

        describe("Versionumero ei täsmää") {
          it("Päivitys hylätään") {
            val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
            val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluOikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now), versionumero = Some(3)), {
              verifyResponseStatus(409, KoskiErrorCategory.conflict.versionumero("Annettu versionumero 3 <> 1"))
            })
            verifyHistory(oppija, modified, List(1))
          }
        }
      }
    }

    describe("Käyttöoikeudet") {
      describe("Kun haetaan historiaa opiskeluoikeudelle, johon käyttäjällä ei oikeuksia") {
        it("Palautetaan 404") {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
          authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get, MockUsers.omniaPalvelukäyttäjä) {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
          }
        }
      }
    }

    describe("Virheellinen id") {
      it("Palautetaan HTTP 400") {
        authGet("api/opiskeluoikeus/historia/asdf") {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.number("Invalid id : asdf"))
        }
      }
    }

    describe("Tuntematon id") {
      it("Palautetaan HTTP 400") {
        authGet("api/opiskeluoikeus/historia/123456789") {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla id:llä tai käyttäjällä ei ole siihen oikeuksia"))
        }
      }
    }

    describe("Versiohistorian hakeminen") {
      it("Onnistuu ja tuottaa auditlog-merkinnän") {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
        authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get) {
          getHistory(opiskeluOikeus.id.get)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "MUUTOSHISTORIA_KATSOMINEN"))
        }
      }
    }

    describe("Yksittäisen version hakeminen") {
      it("Onnistuu ja tuottaa auditlog-merkinnän") {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
        authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get + "/1") {
          verifyResponseStatus(200)
          val versio = Json.read[Opiskeluoikeus](body);
          versio should equal(opiskeluOikeus)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "MUUTOSHISTORIA_KATSOMINEN"))
        }
      }
      describe("Tuntematon versionumero") {
        it("Palautetaan 404") {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
          authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get + "/2") {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.versiotaEiLöydy("Versiota 2 ei löydy opiskeluoikeuden \\d+ historiasta.".r))
          }
        }
      }
    }
  }

  def getHistory(opiskeluOikeusId: Int): List[OpiskeluOikeusHistoryRow] = {
    authGet("api/opiskeluoikeus/historia/" + opiskeluOikeusId) {
      verifyResponseStatus(200)
      Json.read[List[OpiskeluOikeusHistoryRow]](body)
    }
  }

  def verifyHistory(oppija: TäydellisetHenkilötiedot, opiskeluOikeus: Opiskeluoikeus, versions: List[Int]): Unit = {
    val historia: List[OpiskeluOikeusHistoryRow] = getHistory(opiskeluOikeus.id.get)
    historia.map(_.versionumero) should equal(versions)

    markup("Validoidaan versiohistorian eheys")

    authGet("api/opiskeluoikeus/validate/" + opiskeluOikeus.id.get) {
      // Validates version history integrity by applying all history patches on top of first version and comparing to stored final value.
      verifyResponseStatus(200)
    }
  }
}
