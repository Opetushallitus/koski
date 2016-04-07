package fi.oph.tor.api

import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusTestData
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema.TaydellisetHenkilötiedot
import org.scalatest.FreeSpec

class TorOppijaUpdateSpec extends FreeSpec with OpiskeluOikeusTestMethods {
  val uusiOpiskeluOikeus = OpiskeluOikeusTestData.opiskeluOikeus(MockOrganisaatiot.stadinAmmattiopisto, koulutusKoodi = 351161)
  val oppija: TaydellisetHenkilötiedot = MockOppijat.tyhjä

  "Opiskeluoikeuden lisääminen" - {
    "Puuttuvien tietojen täyttäminen" - {
      "Oppilaitoksen tiedot" in {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
        opiskeluOikeus.oppilaitos.nimi.get should equal("Stadin ammattiopisto")
        opiskeluOikeus.oppilaitos.oppilaitosnumero.get.koodiarvo should equal("10105")
      }
      "Koodistojen tiedot" in {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
        opiskeluOikeus.suoritukset(0).koulutusmoduuli.tunniste.nimi should equal(Some("Laitosasentaja"))
      }
    }
  }
}
