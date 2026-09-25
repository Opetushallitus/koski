package fi.oph.koski.api.misc

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.documentation.{ExamplesLukio, ExamplesPerusopetus}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

class LinkitettyOppijaSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsLukio2015 with DirtiesFixtures with DatabaseTestMethods {
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
        setupOppijaWithOpiskeluoikeus(ExamplesPerusopetus.päättötodistus.tallennettavatOpiskeluoikeudet.head, KoskiSpecificMockOppijat.master) {
          verifyResponseStatusOk()
        }
        val alkuperäinenSlavenOo = setupOppijaWithAndGetOpiskeluoikeus(ExamplesLukio.päättötodistus(), KoskiSpecificMockOppijat.slave.henkilö)

        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(oid = alkuperäinenSlavenOo.oid), KoskiSpecificMockOppijat.master) {
          verifyResponseStatusOk()
          val masterOikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.master.oid)
          masterOikeudet.map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
          masterOikeudet(1).versionumero should equal(Some(2))

          val slaveOikeudet = getOpiskeluoikeudet(KoskiSpecificMockOppijat.slave.henkilö.oid)
          slaveOikeudet.map(_.tyyppi.koodiarvo) should equal(List("lukiokoulutus"))
        }
      }
    }

    "Päivitettäessä lähdejärjestelmän id:llä käyttäen master-oppijaa" - {
      "Slavelle ennen linkitystä tallennettu opiskeluoikeus päivittyy eikä siitä synny duplikaattia" in {
        val slaveOid = KoskiSpecificMockOppijat.slave.henkilö.oid
        val masterOid = KoskiSpecificMockOppijat.master.oid
        val lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("win-linkitetty-oppija"))
        val oo = ExamplesLukio.päättötodistus().copy(lähdejärjestelmänId = lähdejärjestelmänId)

        val slavenOo = setupOppijaWithAndGetOpiskeluoikeus(oo, KoskiSpecificMockOppijat.slave.henkilö, paakayttaja)

        // Linkitys on tehty oppijanumerorekisterissä vasta ensimmäisen siirron jälkeen,
        // eikä se ole vielä päivittynyt Kosken henkilo-tauluun
        runDbSync(sqlu"update henkilo set master_oid = null where oid = $slaveOid")
        try {
          putOpiskeluoikeus(oo, KoskiSpecificMockOppijat.master, authHeaders(paakayttaja) ++ jsonContent) {
            verifyResponseStatusOk()
          }
        } finally {
          runDbSync(sqlu"update henkilo set master_oid = $masterOid where oid = $slaveOid")
        }

        val lähdejärjestelmänOpiskeluoikeudet = getOpiskeluoikeudet(masterOid).filter(_.lähdejärjestelmänId == lähdejärjestelmänId)
        lähdejärjestelmänOpiskeluoikeudet.map(_.oid) should equal(List(slavenOo.oid))
      }
    }
  }
}
