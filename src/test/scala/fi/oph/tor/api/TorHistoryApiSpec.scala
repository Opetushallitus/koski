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

  def createOrUpdate(opiskeluOikeus: OpiskeluOikeus) = {
    putOppija(Json.toJValue(TorOppija(oppija, List(opiskeluOikeus)))) {
      verifyResponseCode(200)
    }
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

      markup("Viimeisin versiohistorian versio on sama kuin tallennettu opiskeluoikeus")

      val latestVersion = authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get + "/" + historia.last.versionumero) { Json.read[OpiskeluOikeus](body) }
      latestVersion should equal(opiskeluOikeus)
    }
  }
}
