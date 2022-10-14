package fi.oph.koski.validation

import fi.oph.koski.documentation.ExampleData.muutaKauttaRahoitettu
import fi.oph.koski.schema.{EuropeanSchoolOfHelsinkiOpiskeluoikeus, EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso, EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso1, Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus, Opiskeluoikeus}

object EuropeanSchoolOfHelsinkiValidation {
  def fillRahoitusmuodot(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case e: EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
        e.copy(tila = e.tila.copy(
          opiskeluoikeusjaksot =
            fillRahoitusmuodot(e.tila.opiskeluoikeusjaksot)
        ))
      case _ => oo
    }
  }

  private def fillRahoitusmuodot(opiskeluoikeusjaksot: List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso]): List[EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso] = {
    opiskeluoikeusjaksot.map {
      case j: EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso1 if rahoitusmuotoTäydennetään(j) =>
        j.copy(opintojenRahoitus = Some(muutaKauttaRahoitettu))
      case j => j
    }
  }

  private def rahoitusmuotoTäydennetään(opiskeluoikeusjakso: EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso): Boolean =
    opiskeluoikeusjakso.opintojenRahoitus.isEmpty && tilatJoilleRahoitusmuotoTäydennetään.contains(opiskeluoikeusjakso.tila.koodiarvo)

  private lazy val tilatJoilleRahoitusmuotoTäydennetään = List("lasna", "valmistunut")
}

