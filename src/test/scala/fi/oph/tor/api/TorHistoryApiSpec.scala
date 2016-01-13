package fi.oph.tor.api

import java.time.LocalDate
import fi.oph.tor.db.OpiskeluOikeusHistoryRow
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.json.Json
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusTestData
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema.TorOppija
import org.scalatest.FunSpec

class TorHistoryApiSpec extends FunSpec with OpiskeluOikeusTestMethods {
  SharedJetty.start

  describe("Muutoshistoria") {
    describe("Luotaessa uusi opiskeluoikeus") {
      it("Luodaan historiarivi") {
        resetFixtures {
          val opiskeluOikeus = OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.omnomnia.oid, koulutusKoodi = 351161)
          putOppijaAjax(Json.toJValue(TorOppija(MockOppijat.tyhjä, List(opiskeluOikeus)))) {
          verifyHistory(lastOpiskeluOikeus(MockOppijat.tyhjä.oid), List(1))
        }}
      }
    }
    describe("Päivitettäessä") {
      it("Luodaan uusi versiorivi") {
        resetFixtures {
          val opiskeluOikeus = OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.omnomnia.oid, koulutusKoodi = 351161)
          putOppijaAjax(Json.toJValue(TorOppija(MockOppijat.tyhjä, List(opiskeluOikeus)))) {
            putOppijaAjax(Json.toJValue(TorOppija(MockOppijat.tyhjä, List(opiskeluOikeus.copy(päättymispäivä = Some(LocalDate.now)))))) {
              verifyHistory(lastOpiskeluOikeus(MockOppijat.tyhjä.oid), List(1, 2))
            }
          }
        }
      }
    }
  }

  def lastOpiskeluOikeus(oppijaOid: String): Int = {
    authGet("api/oppija/" + oppijaOid) {
      verifyResponseCode(200)
      Json.read[TorOppija](body).opiskeluoikeudet.last.id.get
    }
  }

  def verifyHistory(opiskeluOikeusId: Int, versions: List[Int]): Unit = {
    authGet("api/opiskeluoikeus/historia/" + opiskeluOikeusId) {
      val historia = Json.read[List[OpiskeluOikeusHistoryRow]](body)
      historia.map(_.versionumero) should equal(versions) // First one was inserted in fixtures already
    }
  }
}
