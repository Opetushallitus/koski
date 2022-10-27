package fi.oph.koski.koskiuser

import fi.oph.koski.TestEnvironment
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class KaikilleOpiskeluoikeudenTyypeilleOnKayttooikeusSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Kaikille opiskeluoikeuden tyypeille on määritelty Koskessa rooli" in {
    OpiskeluoikeudenTyyppi.kaikkiTyypit.map(_.koodiarvo) should equal(Set(
      Rooli.AIKUISTENPERUSOPETUS,
      Rooli.AMMATILLINENKOULUTUS,
      Rooli.DIATUTKINTO,
      Rooli.ESIOPETUS,
      Rooli.IBTUTKINTO,
      Rooli.INTERNATIONALSCHOOL,
      Rooli.KORKEAKOULUTUS,
      Rooli.LUKIOKOULUTUS,
      Rooli.LUVA,
      Rooli.MUUKUINSAANNELTYKOULUTUS,
      Rooli.PERUSOPETUKSEENVALMISTAVAOPETUS,
      Rooli.PERUSOPETUKSENLISAOPETUS,
      Rooli.PERUSOPETUS,
      Rooli.YLIOPPILASTUTKINTO,
      Rooli.VAPAANSIVISTYSTYONKOULUTUS,
      Rooli.TUVA,
      Rooli.ESH,
    ).map(_.toLowerCase))
  }
}
