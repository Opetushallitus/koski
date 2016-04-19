package fi.oph.tor.api

import fi.oph.tor.localization.LocalizedString
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema.{Koodistokoodiviite, Oppilaitos, TaydellisetHenkilötiedot}
import org.scalatest.FreeSpec

class TorOppijaUpdateSpec extends FreeSpec with OpiskeluoikeusTestMethodsAmmatillinen {
  val uusiOpiskeluOikeus = defaultOpiskeluoikeus
  val oppija: TaydellisetHenkilötiedot = MockOppijat.tyhjä

  "Opiskeluoikeuden lisääminen" - {
    "Puuttuvien tietojen täyttäminen" - {
      "Oppilaitoksen tiedot" - {
        "Ilman nimeä -> haetaan nimi" in {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
          opiskeluOikeus.oppilaitos.nimi.get.get("fi") should equal("Stadin ammattiopisto")
          opiskeluOikeus.oppilaitos.oppilaitosnumero.get.koodiarvo should equal("10105")
        }
        "Väärällä nimellä -> korvataan nimi" in {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus.copy(oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, nimi = Some(LocalizedString.finnish("Läppäkoulu")))))
          opiskeluOikeus.oppilaitos.nimi.get.get("fi") should equal("Stadin ammattiopisto")
        }
      }
      "Koodistojen tiedot" - {
        "Ilman nimeä -> haetaan nimi" in {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
          opiskeluOikeus.suoritukset(0).koulutusmoduuli.tunniste.nimi.get.get("fi") should equal("Autoalan perustutkinto")
          opiskeluOikeus.suoritukset(0).koulutusmoduuli.tunniste.nimi.get.get("sv") should equal("Grundexamen inom bilbranschen")
        }
        "Väärällä nimellä -> korvataan nimi" in {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus.copy(suoritukset = uusiOpiskeluOikeus.suoritukset.map(suoritus =>
            suoritus.copy(koulutusmoduuli = suoritus.koulutusmoduuli.copy(tunniste = Koodistokoodiviite(koodiarvo = "351301", nimi=Some(LocalizedString.finnish("Läppätutkinto")), koodistoUri = "koulutus")))
          )))
          opiskeluOikeus.suoritukset(0).koulutusmoduuli.tunniste.nimi.get.get("fi") should equal("Autoalan perustutkinto")
        }
      }

      "Koulutustoimijan tiedot" in {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, uusiOpiskeluOikeus)
        opiskeluOikeus.koulutustoimija.map(_.oid) should equal(Some("1.2.246.562.10.346830761110"))
      }
    }
  }
}
