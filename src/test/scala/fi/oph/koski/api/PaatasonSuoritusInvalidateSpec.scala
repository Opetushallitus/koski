package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{ErrorMatcher, HttpTester, KoskiErrorCategory}
import fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus
import org.scalatest.{FreeSpec, Matchers}

class PaatasonSuoritusInvalidateSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with PaatasonSuoritusTestMethods with HttpTester {
  resetFixtures

  "Päätason suorituksen poistaminen" - {
    "onnistuu" - {
      "kun useampi nuorten perusopetuksen päätason suoritus" in {
        val oid = MockOppijat.koululainen.oid
        val oo = oppija(oid).tallennettavatOpiskeluoikeudet.head

        deletePäätasonSuoritus(oo.oid.get, 1, oo.suoritukset.head) {
          verifyResponseStatusOk()
        }
      }

      "kun useampi aikuisten perusopetuksen päätason suoritus" in {
        val oo = oppija(MockOppijat.aikuisOpiskelija.oid).tallennettavatOpiskeluoikeudet.head

        deletePäätasonSuoritus(oo.oid.get, 1, oo.suoritukset.head) {
          verifyResponseStatusOk()
        }
      }

      "kun suoritus on valmis" in {
        val oo = oppija(MockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.head
        val suoritus = oo.suoritukset.head

        assert(suoritus.valmis)

        deletePäätasonSuoritus(oo.oid.get, 2, suoritus) {
          verifyResponseStatusOk()
        }
      }

      "kun suoritus on kesken" in {
        val oo = oppija(MockOppijat.ysiluokkalainen.oid).tallennettavatOpiskeluoikeudet.head
        val suoritus = oo.suoritukset.head

        assert(!suoritus.valmis)

        deletePäätasonSuoritus(oo.oid.get, 1, suoritus) {
          verifyResponseStatusOk()
        }
      }

      "kun kaksi saman vuosiluokan suoritusta" in {
        val oo = oppija(MockOppijat.luokallejäänyt.oid).tallennettavatOpiskeluoikeudet.head
        val suoritus = oo.suoritukset
          .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
          .find(_.luokka == "7A")
          .get

        deletePäätasonSuoritus(oo.oid.get, 1, suoritus) {
          verifyResponseStatusOk()
        }

        val updated = getOpiskeluoikeus(MockOppijat.luokallejäänyt.oid, "perusopetus")

        val poistettuVuosiluokanSuoritus = updated.suoritukset
          .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
          .filter(_.luokka == "7A")

        val jäljelläOlevaVuosiluokanSuoritus = updated.suoritukset
          .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
          .filter(_.luokka == "7C")

        assert(poistettuVuosiluokanSuoritus.isEmpty)
        assert(jäljelläOlevaVuosiluokanSuoritus.length == 1)
      }
    }

    "epäonnistuu" - {
      "kun suoritus on ainoa päätason suoritus" in {
        val oo = oppija(MockOppijat.aikuisOpiskelija.oid).tallennettavatOpiskeluoikeudet.head

        assert(oo.suoritukset.length == 1)

        deletePäätasonSuoritus(oo.oid.get, 2, oo.suoritukset.head) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.ainoanPäätasonSuorituksenPoisto())
        }
      }

      "kun suoritus on muu päätason suoritus kuin perusopetuksen päätason suoritus" in {
        val oo = oppija(MockOppijat.ammattilainen.oid).tallennettavatOpiskeluoikeudet.head

        deletePäätasonSuoritus(oo.oid.get, 1, oo.suoritukset.head) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.ainoanPäätasonSuorituksenPoisto())
        }
      }

      "vanhalla versionumerolla" in {
        val oo = oppija(MockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.head

        deletePäätasonSuoritus(oo.oid.get, 1, oo.suoritukset.head) {
          verifyResponseStatus(409, KoskiErrorCategory.conflict.versionumero())
        }
      }

      "olemattomalla versionumerolla" in {
        val oo = oppija(MockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.head

        deletePäätasonSuoritus(oo.oid.get, 4, oo.suoritukset.head) {
          verifyResponseStatus(409, KoskiErrorCategory.conflict.versionumero())
        }
      }

      "virheellisellä oid:lla" in {
        val oo = oppija(MockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.head

        deletePäätasonSuoritus("1.2.246.562.24.99999999999", 1, oo.suoritukset.head) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.queryParam.virheellinenOpiskeluoikeusOid, ".*Virheellinen oid.*".r))
        }
      }

      "suorituksella, jota ei löydy" in {
        val oo = oppija(MockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.head
        val suoritus = oppija(MockOppijat.ysiluokkalainen.oid).tallennettavatOpiskeluoikeudet.head.suoritukset.head

        deletePäätasonSuoritus(oo.oid.get, oo.versionumero.get, suoritus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }
    }
  }
}
