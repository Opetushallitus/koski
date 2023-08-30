package fi.oph.koski.schema

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsYlioppilastutkinto
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import org.scalatest.freespec.AnyFreeSpec

class KoulusivistyskieliYlioppilastutkintoSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsYlioppilastutkinto {

  "Koulusivistyskieli - Ylioppilastutkinnon suoritus" - {

    "Koulusivistyskieliä voi olla useita" in {
      verifyKoulusivityskieli(KoskiSpecificMockOppijat.montaKoulusivityskieltäYlioppilas, Some(List(
        Koodistokoodiviite("FI", "kieli"),
        Koodistokoodiviite("SV", "kieli")
      )))
    }

    "Koulusivistyskieleksi ei tulkita hylättyjä suorituksia" in {
      verifyKoulusivityskieli(KoskiSpecificMockOppijat.koulusivistyskieliYlioppilas, Some(List(
        Koodistokoodiviite("SV", "kieli")
      )))
    }

    def verifyKoulusivityskieli(oppija: LaajatOppijaHenkilöTiedot, expected: Option[List[Koodistokoodiviite]]) = {
      val koulusivistyskielet = getOpiskeluoikeudet(oppija.oid).flatMap(_.suoritukset).collect {
        case x: YlioppilastutkinnonSuoritus => x.koulusivistyskieli
      }
      koulusivistyskielet.length should equal(1)
      koulusivistyskielet.head should equal(expected)
    }
  }
}
