package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeusjakso, KoskeenTallennettavaOpiskeluoikeus, KoskiOpiskeluoikeusjakso, Opiskeluoikeusjakso}

object JotpaValidation {
  def JOTPARAHOITUS_KOODIARVOT = List("14", "15")

  def validateOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    HttpStatus.fold(
      oo.tila.opiskeluoikeusjaksot.map(validateOpiskeluoikeusjaksonRahoitusmuoto)
      :+ validateJaksojenRahoituksenYhtenäisyys(oo.tila.opiskeluoikeusjaksot)
    )

  def validateOpiskeluoikeusjaksonRahoitusmuoto(jakso: Opiskeluoikeusjakso): HttpStatus = {
    val jotpaRahoitteinen = rahoitusmuoto(jakso).exists(JOTPARAHOITUS_KOODIARVOT.contains)

    // Huom! Tähän lisätään myöhemmin VST-JOTPA ja MUKS-JOTPA, kunhan niiden tietomallit valmistuvat.
    jakso match {
      case _: Opiskeluoikeusjakso if !jotpaRahoitteinen => HttpStatus.ok
      case _: AmmatillinenOpiskeluoikeusjakso => HttpStatus.ok
      case _ => KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuotoEiSaaOllaJotpa()
    }
  }

  def validateJaksojenRahoituksenYhtenäisyys(jaksot: Seq[Opiskeluoikeusjakso]): HttpStatus = {
    val (jotpaRahoitusmuodot, muutRahoitusmuodot) = jaksot
      .flatMap(rahoitusmuoto)
      .toSet
      .partition(JOTPARAHOITUS_KOODIARVOT.contains)

    if (jotpaRahoitusmuodot.nonEmpty && muutRahoitusmuodot.nonEmpty) {
      KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys("Opiskeluoikeudella, jolla on jatkuvan oppimisen rahoitusta, ei voi olla muita rahoitusmuotoja")
    } else if (jotpaRahoitusmuodot.size > 1) {
      KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys()
    } else {
      HttpStatus.ok
    }
  }

  private def rahoitusmuoto(jakso: Opiskeluoikeusjakso): Option[String] =
    Option(jakso)
      .collect { case j: KoskiOpiskeluoikeusjakso => j }
      .flatMap(_.opintojenRahoitus.map(_.koodiarvo))
}
