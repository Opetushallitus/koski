package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeusjakso, KoskeenTallennettavaOpiskeluoikeus, KoskiOpiskeluoikeusjakso, Opiskeluoikeusjakso}

object JotpaValidation {
  def JOTPARAHOITUS_KOODIARVOT = List("14", "15")

  def validateOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    HttpStatus.fold(oo.tila.opiskeluoikeusjaksot.map(validateOpiskeluoikeusjaksonRahoitusmuoto))

  def validateOpiskeluoikeusjaksonRahoitusmuoto(jakso: Opiskeluoikeusjakso): HttpStatus = {
    val jotpaRahoitteinen = Option(jakso)
      .collect { case j: KoskiOpiskeluoikeusjakso => j }
      .flatMap(_.opintojenRahoitus.map(_.koodiarvo))
      .exists(JOTPARAHOITUS_KOODIARVOT.contains)

    // Huom! Tähän lisätään myöhemmin VST-JOTPA ja MUKS-JOTPA, kunhan niiden tietomallit valmistuvat.
    jakso match {
      case _: Opiskeluoikeusjakso if !jotpaRahoitteinen => HttpStatus.ok
      case _: AmmatillinenOpiskeluoikeusjakso => HttpStatus.ok
      case _ => KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuotoEiSaaOllaJotpa()
    }
  }
}
