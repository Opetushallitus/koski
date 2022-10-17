package fi.oph.koski.validation

import fi.oph.koski.documentation.ExampleData.muutaKauttaRahoitettu
import fi.oph.koski.schema.{EuropeanSchoolOfHelsinkiOpiskeluoikeus, EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso, KoskeenTallennettavaOpiskeluoikeus}

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
      case j if rahoitusmuotoTäydennetään(j) =>
        j.copy(opintojenRahoitus = Some(muutaKauttaRahoitettu))
      case j => j
    }
  }

  private def rahoitusmuotoTäydennetään(opiskeluoikeusjakso: EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso): Boolean =
    opiskeluoikeusjakso.opintojenRahoitus.isEmpty && tilatJoilleRahoitusmuotoTäydennetään.contains(opiskeluoikeusjakso.tila.koodiarvo)

  private lazy val tilatJoilleRahoitusmuotoTäydennetään = List("lasna", "valmistunut")

  def fillSynteettisetArvosanat(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
//      case e: EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
//        e.copy(suoritukset = fillSynteettisetArvosanat(e.suoritukset))
      case _ => oo
    }
  }

//  private def fillSynteettisetArvosanat(suoritukset: List[EuropeanSchoolOfHelsinkiVuosiluokanSuoritus]): List[EuropeanSchoolOfHelsinkiVuosiluokanSuoritus] = {
//    suoritukset.map {
//      case s: SecondaryLowerVuosiluokanSuoritus =>
//        s.copy(osasuoritukset = s.osasuoritukset.map(fillSynteettisetArvosanat))
//      case s: SecondaryUpperVuosiluokanSuoritus => ???
//      case s => s
//    }
//  }
//
//  private def fillSynteettisetArvosanat(osasuoritukset: List[SecondaryLowerOppiaineenSuoritus]): List[SecondaryLowerOppiaineenSuoritus]  = {
//    osasuoritukset.map {
//      case o: SecondaryLowerOppiaineenSuoritus =>
//          o.copy(
//            arviointi = o.arviointi.map(fillSynteettisetArvosanat)
//          )
////      case o: SecondaryUpperOppiaineenSuoritus => ???
//      case o => o
//    }
//
//  }
//
//  private def fillSynteettisetArvosanat(arvioinnit: List[SecondaryArviointi]): List[SecondaryArviointi] = {
//    arvioinnit.map {
//      case a: SecondaryNumericalMarkArviointi =>
//        a.copy(arvosana = a.arvosana.copy(
//          koodistoUri = Some("esh/numericalmark")
//        ))
//      case a: SecondaryS7PreliminaryMarkArviointi => ???
//      case a: SecondaryS7FinalMarkArviointi => ???
//      case a => a
//    }
//  }


}

