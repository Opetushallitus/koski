package fi.oph.koski.api.misc

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

class LinkitettyOppijaSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio2015 with DirtiesFixtures {
  "Linkitetyt oppijat" - {
    "Kun haetaan masterilla" - {
      "Näytetään myös slaveen kytketyt opiskeluoikeudet" in {
        getOpiskeluoikeudet(KoskiSpecificMockOppijat.master.oid).map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
      }
    }

    "Kun haetaan slavella" - {
      "Näytetään vain slaveen kytketyt opiskeluoikeudet" in {
        getOpiskeluoikeudet(KoskiSpecificMockOppijat.slave.henkilö.oid).map(_.tyyppi.koodiarvo) should equal(List("lukiokoulutus"))
      }

      "Kun haetaan opintotiedot näytetään myös masteriin kytketyt opiskeluoikeudet" in {
        val opiskeluoikeudet = authGet(s"api/oppija/${KoskiSpecificMockOppijat.slave.henkilö.oid}/opintotiedot-json")(readOppija).opiskeluoikeudet
        opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
      }
    }

    "Päivitettäessä slaveen liittyvä opiskeluoikeus käyttäen master-oppijaa" - {
      "Opiskeluoikeus päivittyy ja säilyy linkitettynä slaveen" in {
        putOpiskeluoikeus(defaultOpiskeluoikeus, KoskiSpecificMockOppijat.master) {
          verifyResponseStatusOk()
          val masterOikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.master.oid)
          masterOikeudet.map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
          masterOikeudet(1).versionumero should equal(Some(2))

          val slaveOikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.slave.henkilö.oid)
          slaveOikeudet.map(_.tyyppi.koodiarvo) should equal(List("lukiokoulutus"))
        }
      }
    }
  }
}
