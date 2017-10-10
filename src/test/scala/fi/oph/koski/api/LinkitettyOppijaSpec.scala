package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.FreeSpec

class LinkitettyOppijaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  "Linkitetyt oppijat" - {
    "Kun haetaan masterilla" - {
      "Näytetään myös slaveen kytketyt opiskeluoikeudet" in {
        getOpiskeluoikeudet(MockOppijat.master.oid).map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
      }
    }

    "Päivitettäessä slaveen liittyvä opiskeluoikeus käyttäen master-oppijaa" - {
      "Opiskeluoikeus päivittyy ja säilyy linkitettynä slaveen" in {
        // TODO
      }
    }
  }
}
