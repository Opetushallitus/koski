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
      :+ validateLaajuudet(oo)
    )

  private def validateLaajuudet(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    HttpStatus.fold(
      oo.suoritukset.map {
        case s: VapaanSivistystyönJotpaKoulutuksenSuoritus if s.vahvistettu => validateSuorituksenLaajuus(s)
        case _ => HttpStatus.ok
      }
    )
  }

  private def validateSuorituksenLaajuus(s: VapaanSivistystyönJotpaKoulutuksenSuoritus): HttpStatus = {
    HttpStatus.fold(
      HttpStatus.validate(s.koulutusmoduuli.getLaajuus.isDefined)(
        KoskiErrorCategory.badRequest.validation.laajuudet(s"Vahvistetulta jatkuvaan oppimiseen suunnatulta vapaan sivistystyön koulutuksen suoritukselta ${suorituksenTunniste(s)} puuttuu laajuus")
      ),
      HttpStatus.fold(s.rekursiivisetOsasuoritukset.map{ os =>
        HttpStatus.validate(os.koulutusmoduuli.getLaajuus.isDefined)(
          KoskiErrorCategory.badRequest.validation.laajuudet(s"Vahvistetulta jatkuvaan oppimiseen suunnatulta vapaan sivistystyön koulutuksen osasuoritukselta ${suorituksenTunniste(os)} puuttuu laajuus")
        )
      })
    )
  }

  def validateOpiskeluoikeusjaksonRahoitusmuoto(jakso: Opiskeluoikeusjakso): HttpStatus = {
    val jotpaRahoitteinen = rahoitusmuoto(jakso).exists(JOTPARAHOITUS_KOODIARVOT.contains)
    val vstJotpanRahoitustiedonVaativatTilat = List("lasna", "hyvaksytystisuoritettu")

    jakso match {
      case j: VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso if j.opintojenRahoitus.isEmpty && vstJotpanRahoitustiedonVaativatTilat.contains(j.tila.koodiarvo) =>
        KoskiErrorCategory.badRequest.validation.tila.tilaltaPuuttuuRahoitusmuoto()
      case _: Opiskeluoikeusjakso if !jotpaRahoitteinen => HttpStatus.ok
      case _: AmmatillinenOpiskeluoikeusjakso => HttpStatus.ok
      case _: VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso => HttpStatus.ok
      case _: MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso => HttpStatus.ok
      case _ => KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuotoEiSaaOllaJotpa()
    }
  }

  def validateJaksojenRahoituksenYhtenäisyys(jaksot: Seq[Opiskeluoikeusjakso]): HttpStatus = {
    val (jotpaRahoitusmuodot, muutRahoitusmuodot) = jaksot
      .flatMap(rahoitusmuoto)
      .toSet
      .partition(JOTPARAHOITUS_KOODIARVOT.contains)

    if (jotpaRahoitusmuodot.nonEmpty && muutRahoitusmuodot.nonEmpty) {
      KoskiErrorCategory.badRequest.validation.tila.tilanRahoitusmuodonYhtenäisyys()
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

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }
}
