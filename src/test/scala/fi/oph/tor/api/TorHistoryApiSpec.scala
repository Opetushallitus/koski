package fi.oph.tor.api

import java.time.LocalDate
import fi.oph.tor.db.OpiskeluOikeusHistoryRow
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusTestData
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema.{OpiskeluOikeus, TorOppija}
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

        val modified: OpiskeluOikeus = opiskeluOikeus.copy(päättymispäivä = Some(LocalDate.now))
        createOrUpdate(modified)

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


  def createOrUpdate(opiskeluOikeus: OpiskeluOikeus) {
    putOppija(Json.toJValue(TorOppija(MockOppijat.tyhjä, List(opiskeluOikeus)))) {
      verifyResponseCode(200)
    }
  }

  def createOpiskeluOikeus = {
    resetFixtures
    val opiskeluOikeus = OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.helsinginAmmattiOpisto.oid, koulutusKoodi = 351161)
    createOrUpdate(opiskeluOikeus)
    opiskeluOikeus.copy(id = Some(lastOpiskeluOikeus(MockOppijat.tyhjä.oid)))
  }

  def lastOpiskeluOikeus(oppijaOid: String): Int = {
    authGet("api/oppija/" + oppijaOid) {
      verifyResponseCode(200)
      Json.read[TorOppija](body).opiskeluoikeudet.last.id.get
    }
  }

  def verifyHistory(opiskeluOikeus: OpiskeluOikeus, versions: List[Int]): Unit = {
    authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get) {
      verifyResponseCode(200)
      val historia = Json.read[List[OpiskeluOikeusHistoryRow]](body)
      historia.map(_.versionumero) should equal(versions) // First one was inserted in fixtures already

      val latestVersion = authGet("api/opiskeluoikeus/historia/" + opiskeluOikeus.id.get + "/" + historia.last.versionumero) { Json.read[OpiskeluOikeus](body) }
      latestVersion should equal(opiskeluOikeus)
    }
  }
}
