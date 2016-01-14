package fi.oph.tor.api

import java.time.LocalDate
import fi.oph.tor.db.OpiskeluOikeusHistoryRow
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusTestData
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema.{FullHenkilö, OpiskeluOikeus, TorOppija}
import fi.oph.tor.toruser.MockUsers
import org.scalatest.FunSpec

class TorHistoryApiSpec extends FunSpec with OpiskeluOikeusTestMethods {
  SharedJetty.start

  describe("Muutoshistoria") {
    describe("Luotaessa uusi opiskeluoikeus") {
      it("Luodaan historiarivi") {
        val opiskeluOikeus = createOpiskeluOikeus
        verifyHistory(opiskeluOikeus, List(1))
      }
    }
    describe("Päivitettäessä") {
      it("Luodaan uusi versiorivi") {
        val opiskeluOikeus = createOpiskeluOikeus
        val modified: OpiskeluOikeus = createOrUpdate(opiskeluOikeus.copy(päättymispäivä = Some(LocalDate.now)))
        verifyHistory(modified, List(1, 2))
      }

      describe("Kun syötteessä annetaan versionumero") {
        describe("Versionumero sama kuin viimeisin") {
          it("Päivitys hyväksytään") {
            val opiskeluOikeus = createOpiskeluOikeus
            val modified: OpiskeluOikeus = createOrUpdate(opiskeluOikeus.copy(päättymispäivä = Some(LocalDate.now), versionumero = Some(1)))
            verifyHistory(modified, List(1, 2))
          }
        }

        describe("Versionumero ei täsmää") {
          it("Päivitys hylätään") {
            val opiskeluOikeus = createOpiskeluOikeus
            val modified: OpiskeluOikeus = createOrUpdate(opiskeluOikeus.copy(päättymispäivä = Some(LocalDate.now), versionumero = Some(3)), {
              verifyResponseCode(409)
            })
            verifyHistory(modified, List(1))
          }
        }
      }
    }

    describe("Käyttöoikeudet") {
      describe("Kun haetaan historiaa opiskeluoikeudelle, johon käyttäjällä ei oikeuksia") {
        it("Palautetaan 404") {
          val opiskeluOikeus = createOpiskeluOikeus
          authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get, MockUsers.hiiri) {
            verifyResponseCode(404)
          }
        }
      }
    }
  }

  private val oppija: FullHenkilö = MockOppijat.tyhjä

  def createOrUpdate(opiskeluOikeus: OpiskeluOikeus, check: => Unit = { verifyResponseCode(200) }) = {
    putOppija(Json.toJValue(TorOppija(oppija, List(opiskeluOikeus))))(check)
    lastOpiskeluOikeus(oppija.oid)
  }

  def createOpiskeluOikeus = {
    resetFixtures
    val opiskeluOikeus = OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.helsinginAmmattiOpisto.oid, koulutusKoodi = 351161)
    createOrUpdate(opiskeluOikeus)
    lastOpiskeluOikeus(oppija.oid)
  }

  def lastOpiskeluOikeus(oppijaOid: String) = {
    authGet("api/oppija/" + oppijaOid) {
      verifyResponseCode(200)
      Json.read[TorOppija](body).opiskeluoikeudet.last
    }
  }

  def verifyHistory(opiskeluOikeus: OpiskeluOikeus, versions: List[Int]): Unit = {
    authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get) {
      verifyResponseCode(200)
      val historia = Json.read[List[OpiskeluOikeusHistoryRow]](body)
      historia.map(_.versionumero) should equal(versions)

      markup("Validoidaan versiohistoria eheys")

      authGet("api/oppija/validate/" + oppija.oid) {
        verifyResponseCode(200)
      }
    }
  }
}
