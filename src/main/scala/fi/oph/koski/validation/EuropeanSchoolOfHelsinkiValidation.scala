package fi.oph.koski.validation

import fi.oph.koski.documentation.ExampleData.muutaKauttaRahoitettu
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{EuropeanSchoolOfHelsinkiOpiskeluoikeus, EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso, KoskeenTallennettavaOpiskeluoikeus}

object EuropeanSchoolOfHelsinkiValidation {
  def fillRahoitusmuodot(koodistoPalvelu: KoodistoViitePalvelu)(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case e: EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
        e.copy(tila = e.tila.copy(
          opiskeluoikeusjaksot =
            fillRahoitusmuodot(e.tila.opiskeluoikeusjaksot, koodistoPalvelu)
        ))
      case _ => oo
    }
  }

  private def fillRahoitusmuodot(opiskeluoikeusjaksot: List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso], koodistoPalvelu: KoodistoViitePalvelu): List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso] = {
    opiskeluoikeusjaksot.map {
      case j if rahoitusmuotoTäydennetään(j) =>
        j.copy(opintojenRahoitus = koodistoPalvelu.validate(muutaKauttaRahoitettu))
      case j => j
    }
  }

  private def rahoitusmuotoTäydennetään(opiskeluoikeusjakso: EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso): Boolean =
    opiskeluoikeusjakso.opintojenRahoitus.isEmpty && tilatJoilleRahoitusmuotoTäydennetään.contains(opiskeluoikeusjakso.tila.koodiarvo)

  private lazy val tilatJoilleRahoitusmuotoTäydennetään = List("lasna", "valmistunut")
}

