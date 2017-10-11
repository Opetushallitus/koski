package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import org.scalatest.FreeSpec

class LinkitettyOppijaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsLukio {
  "Linkitetyt oppijat" - {
    "Kun haetaan masterilla" - {
      "Näytetään myös slaveen kytketyt opiskeluoikeudet" in {
        getOpiskeluoikeudet(MockOppijat.master.oid).map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
      }
    }

    "Kun haetaan slavella" - {
      "Näytetään vain slaveen kytketyt opiskeluoikeudet" in {
        getOpiskeluoikeudet(MockOppijat.slave.oid).map(_.tyyppi.koodiarvo) should equal(List("lukiokoulutus"))
      }
    }

    "Päivitettäessä slaveen liittyvä opiskeluoikeus käyttäen master-oppijaa" - {
      "Opiskeluoikeus päivittyy ja säilyy linkitettynä slaveen" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus, MockOppijat.master) {
          verifyResponseStatus(200)
          val masterOikeudet = getOpiskeluoikeudet(MockOppijat.master.oid)
          masterOikeudet.map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
          masterOikeudet(1).versionumero should equal(Some(2))

          val slaveOikeudet = getOpiskeluoikeudet(MockOppijat.slave.oid)
          slaveOikeudet.map(_.tyyppi.koodiarvo) should equal(List("lukiokoulutus"))
        }
      }
    }

    // TODO: historian tarkastelu masterin kautta
    // TODO: käyttäjän omat opiskeluoikeudet (master/slave)
  }
}
