package fi.oph.koski.schema

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsYlioppilastutkinto}
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, MockOppijat}
import org.scalatest.FreeSpec

class KoulusivistyskieliYlioppilastutkintoSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsYlioppilastutkinto {

  "Koulusivistyskieli - Ylioppilastutkinnon suoritus" - {

    "Koulusivistyskieliä voi olla useita" in {
      verifyKoulusivityskieli(MockOppijat.montaKoulusivityskieltäYlioppilas, Some(List(
        Koodistokoodiviite("FI", "kieli"),
        Koodistokoodiviite("SV", "kieli")
      )))
    }

    "Koulusivistyskieleksi ei tulkita hylättyjä suorituksia" in {
      verifyKoulusivityskieli(MockOppijat.koulusivistyskieliYlioppilas, Some(List(
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
