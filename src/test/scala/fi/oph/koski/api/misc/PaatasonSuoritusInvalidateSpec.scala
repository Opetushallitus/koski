package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema.{PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PaatasonSuoritusInvalidateSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec with PaatasonSuoritusTestMethods {

  "Päätason suorituksen poistaminen" - {
    "onnistuu" - {
      "kun useampi nuorten perusopetuksen päätason suoritus" in {
        val oid = KoskiSpecificMockOppijat.koululainen.oid
        val oo = oppija(oid).tallennettavatOpiskeluoikeudet.collect { case o: PerusopetuksenOpiskeluoikeus => o }.head

        deletePäätasonSuoritus(oo.oid.get, 1, oo.suoritukset.head) {
          verifyResponseStatusOk()
        }
      }

      "kun useampi aikuisten perusopetuksen päätason suoritus" in {
        val oo = oppija(KoskiSpecificMockOppijat.aikuisOpiskelija.oid).tallennettavatOpiskeluoikeudet.head

        deletePäätasonSuoritus(oo.oid.get, 1, oo.suoritukset.head) {
          verifyResponseStatusOk()
        }
      }

      "kun suoritus on valmis" in {
        val oo = oppija(KoskiSpecificMockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.collect { case o: PerusopetuksenOpiskeluoikeus => o}.head
        val suoritus = oo.suoritukset.head

        assert(suoritus.valmis)

        deletePäätasonSuoritus(oo.oid.get, 2, suoritus) {
          verifyResponseStatusOk()
        }
      }

      "kun suoritus on kesken" in {
        val oo = oppija(KoskiSpecificMockOppijat.ysiluokkalainen.oid).tallennettavatOpiskeluoikeudet.head
        val suoritus = oo.suoritukset.head

        assert(!suoritus.valmis)

        deletePäätasonSuoritus(oo.oid.get, 1, suoritus) {
          verifyResponseStatusOk()
        }
      }

      "kun kaksi saman vuosiluokan suoritusta" in {
        val oo = oppija(KoskiSpecificMockOppijat.luokallejäänyt.oid).tallennettavatOpiskeluoikeudet.head
        val suoritus = oo.suoritukset
          .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
          .find(_.luokka == "7A")
          .get

        deletePäätasonSuoritus(oo.oid.get, 1, suoritus) {
          verifyResponseStatusOk()
        }

        val updated = getOpiskeluoikeus(KoskiSpecificMockOppijat.luokallejäänyt.oid, "perusopetus")

        val poistettuVuosiluokanSuoritus = updated.suoritukset
          .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
          .filter(_.luokka == "7A")

        val jäljelläOlevaVuosiluokanSuoritus = updated.suoritukset
          .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
          .filter(_.luokka == "7C")

        assert(poistettuVuosiluokanSuoritus.isEmpty)
        assert(jäljelläOlevaVuosiluokanSuoritus.length == 1)
      }

      "kun kaksi identtistä suoritusta" in {
        val oo = oppija(KoskiSpecificMockOppijat.suoritusTuplana.oid).tallennettavatOpiskeluoikeudet.head
        val suoritus = oo.suoritukset
          .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
          .find(_.luokka == "8C")
          .get

        deletePäätasonSuoritus(oo.oid.get, 1, suoritus) {
          verifyResponseStatusOk()
        }

        val updated = getOpiskeluoikeus(KoskiSpecificMockOppijat.suoritusTuplana.oid, "perusopetus")

        val jäljelläOlevaVuosiluokanSuoritus = updated.suoritukset
          .collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
          .filter(_.luokka == "8C")
        assert(jäljelläOlevaVuosiluokanSuoritus.length == 1)
      }
    }

    "epäonnistuu" - {
      "kun suoritus on ainoa päätason suoritus" in {
        val oo = oppija(KoskiSpecificMockOppijat.aikuisOpiskelija.oid).tallennettavatOpiskeluoikeudet.head

        assert(oo.suoritukset.length == 1)

        deletePäätasonSuoritus(oo.oid.get, 2, oo.suoritukset.head) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.ainoanPäätasonSuorituksenPoisto())
        }
      }

      "kun suoritus on muu päätason suoritus kuin perusopetuksen päätason suoritus" in {
        val oo = oppija(KoskiSpecificMockOppijat.ammattilainen.oid).tallennettavatOpiskeluoikeudet.head

        deletePäätasonSuoritus(oo.oid.get, 1, oo.suoritukset.head) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.ainoanPäätasonSuorituksenPoisto())
        }
      }

      "vanhalla versionumerolla" in {
        val oo = oppija(KoskiSpecificMockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.collect { case o: PerusopetuksenOpiskeluoikeus => o }.head

        deletePäätasonSuoritus(oo.oid.get, 1, oo.suoritukset.head) {
          verifyResponseStatus(409, KoskiErrorCategory.conflict.versionumero())
        }
      }

      "olemattomalla versionumerolla" in {
        val oo = oppija(KoskiSpecificMockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.collect { case o: PerusopetuksenOpiskeluoikeus => o }.head

        deletePäätasonSuoritus(oo.oid.get, 4, oo.suoritukset.head) {
          verifyResponseStatus(409, KoskiErrorCategory.conflict.versionumero())
        }
      }

      "virheellisellä oid:lla" in {
        val oo = oppija(KoskiSpecificMockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.collect { case o: PerusopetuksenOpiskeluoikeus => o }.head

        deletePäätasonSuoritus("1.2.246.562.24.99999999999", 1, oo.suoritukset.head) {
          verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.queryParam.virheellinenOpiskeluoikeusOid, ".*Virheellinen oid.*".r))
        }
      }

      "suorituksella, jota ei löydy" in {
        val oo = oppija(KoskiSpecificMockOppijat.koululainen.oid).tallennettavatOpiskeluoikeudet.collect { case o: PerusopetuksenOpiskeluoikeus => o }.head
        val suoritus = oppija(KoskiSpecificMockOppijat.ysiluokkalainen.oid).tallennettavatOpiskeluoikeudet.head.suoritukset.head

        deletePäätasonSuoritus(oo.oid.get, oo.versionumero.get, suoritus) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }
    }
  }
}
