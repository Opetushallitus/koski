package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.db.OpiskeluoikeusHistoryRow
import fi.oph.koski.documentation.AmmatillinenOldExamples
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.{Opiskeluoikeus, TäydellisetHenkilötiedot}
import org.scalatest.FunSpec

class HistoryApiSpec extends FunSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val uusiOpiskeluoikeus = defaultOpiskeluoikeus
  val oppija: TäydellisetHenkilötiedot = MockOppijat.tyhjä

  describe("Muutoshistoria") {
    describe("Luotaessa uusi opiskeluoikeus") {
      it("Luodaan historiarivi") {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
        verifyHistory(oppija, opiskeluoikeus, List(1))
      }

      it("osasuorituksilla") {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, AmmatillinenOldExamples.full.opiskeluoikeudet(0))
        verifyHistory(oppija, opiskeluoikeus, List(1))
      }
    }
    describe("Päivitettäessä") {
      it("Luodaan uusi versiorivi") {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
        val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now)))
        verifyHistory(oppija, modified, List(1, 2))
      }

      describe("Jos mikään ei ole muuttunut") {
        it("Ei luoda uutta versioriviä") {
          val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
          val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluoikeus)
          verifyHistory(oppija, modified, List(1))
        }
      }

      describe("Kun syötteessä annetaan versionumero") {
        describe("Versionumero sama kuin viimeisin") {
          it("Päivitys hyväksytään") {
            val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
            val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now), versionumero = Some(1)))
            verifyHistory(oppija, modified, List(1, 2))
          }
        }

        describe("Versionumero ei täsmää") {
          it("Päivitys hylätään") {
            val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
            val modified: Opiskeluoikeus = createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now), versionumero = Some(3)), {
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
          val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
          authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.id.get, MockUsers.omniaPalvelukäyttäjä) {
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
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
        authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.id.get) {
          getHistory(opiskeluoikeus.id.get)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "MUUTOSHISTORIA_KATSOMINEN"))
        }
      }
    }

    describe("Yksittäisen version hakeminen") {
      it("Onnistuu ja tuottaa auditlog-merkinnän") {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
        authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.id.get + "/1") {
          verifyResponseStatus(200)
          val versio = Json.read[Opiskeluoikeus](body);
          versio should equal(opiskeluoikeus)
          AuditLogTester.verifyAuditLogMessage(Map("operaatio" -> "MUUTOSHISTORIA_KATSOMINEN"))
        }
      }
      describe("Tuntematon versionumero") {
        it("Palautetaan 404") {
          val opiskeluoikeus = createOpiskeluoikeus(oppija, uusiOpiskeluoikeus)
          authGet("api/opiskeluoikeus/historia/" + opiskeluoikeus.id.get + "/2") {
            verifyResponseStatus(404, KoskiErrorCategory.notFound.versiotaEiLöydy("Versiota 2 ei löydy opiskeluoikeuden \\d+ historiasta.".r))
          }
        }
      }
    }
  }

  def getHistory(opiskeluoikeusId: Int): List[OpiskeluoikeusHistoryRow] = {
    authGet("api/opiskeluoikeus/historia/" + opiskeluoikeusId) {
      verifyResponseStatus(200)
      Json.read[List[OpiskeluoikeusHistoryRow]](body)
    }
  }

  def verifyHistory(oppija: TäydellisetHenkilötiedot, opiskeluoikeus: Opiskeluoikeus, versions: List[Int]): Unit = {
    val historia: List[OpiskeluoikeusHistoryRow] = getHistory(opiskeluoikeus.id.get)
    historia.map(_.versionumero) should equal(versions)

    markup("Validoidaan versiohistorian eheys")

    authGet("api/opiskeluoikeus/validate/" + opiskeluoikeus.id.get) {
      // Validates version history integrity by applying all history patches on top of first version and comparing to stored final value.
      verifyResponseStatus(200)
    }
  }
}
