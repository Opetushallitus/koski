package fi.oph.koski.koskiuser

import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import org.scalatest.{FreeSpec, Matchers}

class KaikilleOpiskeluoikeudenTyypeilleOnKayttooikeusSpec extends FreeSpec with Matchers {
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
      Rooli.PERUSOPETUKSEENVALMISTAVAOPETUS,
      Rooli.PERUSOPETUKSENLISAOPETUS,
      Rooli.PERUSOPETUS,
      Rooli.YLIOPPILASTUTKINTO,
      Rooli.VAPAANSIVISTYSTYONKOULUTUS
    ).map(_.toLowerCase))
  }
}
