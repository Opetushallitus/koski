package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat

import java.time.LocalDate

object JotpaValidation {
  def JOTPARAHOITUS_KOODIARVOT = List("14", "15")

  def jotpaRahoitusVoimassaAlkaen(config: Config): LocalDate =
    LocalDate.parse(config.getString("validaatiot.jatkuvaanOppimiseenSuunnatutKoulutusmuodotAstuvatVoimaan"))

  def validateOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus, jotpaRahoituksenRajapäivä: LocalDate): HttpStatus =
    HttpStatus.fold(
      oo.tila.opiskeluoikeusjaksot.map(validateOpiskeluoikeusjaksonRahoitusmuoto)
      :+ validateJaksojenRahoituksenYhtenäisyys(oo.tila.opiskeluoikeusjaksot)
      :+ validateJotpaRahoituksenAlkamispäivä(oo, jotpaRahoituksenRajapäivä)
    )

  def validateOpiskeluoikeusjaksonRahoitusmuoto(jakso: Opiskeluoikeusjakso): HttpStatus = {
    val jotpaRahoitteinen = rahoitusmuoto(jakso).exists(JOTPARAHOITUS_KOODIARVOT.contains)

    // Huom! Tähän lisätään myöhemmin MUKS-JOTPA, kunhan sen tietomalli valmistuu.
    jakso match {
      case j: VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso if j.opintojenRahoitus.isEmpty => KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto()
      case _: Opiskeluoikeusjakso if !jotpaRahoitteinen => HttpStatus.ok
      case _: AmmatillinenOpiskeluoikeusjakso => HttpStatus.ok
      case _: VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso => HttpStatus.ok
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

  def validateJotpaRahoituksenAlkamispäivä(oo: KoskeenTallennettavaOpiskeluoikeus, rajapäivä: LocalDate): HttpStatus =
    if (oo.tila.opiskeluoikeusjaksot
      .collect { case j: KoskiOpiskeluoikeusjakso => j }
      .filter(_.opintojenRahoitus.exists(r => JOTPARAHOITUS_KOODIARVOT.contains(r.koodiarvo)))
      .exists(_.alku.isBefore(rajapäivä))) {
        KoskiErrorCategory.badRequest.validation.date.alkamispäivä(s"Jatkuvaan oppimiseen suunnatun koulutuksen opiskeluoikeuden jakso ei voi alkaa ennen ${finnishDateFormat.format(rajapäivä)}")
      } else {
        HttpStatus.ok
      }

  private def rahoitusmuoto(jakso: Opiskeluoikeusjakso): Option[String] =
    Option(jakso)
      .collect { case j: KoskiOpiskeluoikeusjakso => j }
      .flatMap(_.opintojenRahoitus.map(_.koodiarvo))
}
