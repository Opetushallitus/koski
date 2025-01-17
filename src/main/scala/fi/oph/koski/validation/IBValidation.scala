package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{IBKurssi, IBKurssinSuoritus, IBOpiskeluoikeus, IBOppiaineenArviointi, IBOppiaineenPredictedArviointi, IBOppiaineenSuoritus, IBPäätasonSuoritus, IBTutkinnonSuoritus, KoskeenTallennettavaOpiskeluoikeus, LaajuusKursseissa, LaajuusOpintopisteissä, LaajuusOsaamispisteissä}
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.util.FinnishDateFormat

import java.time.LocalDate

object IBValidation {
  def validateIbOpiskeluoikeus(config: Config)(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    opiskeluoikeus match {
      case oo: IBOpiskeluoikeus => HttpStatus.fold(
        validateIbTutkinnonSuoritus(oo, config),
        validateIBKurssienLaajuusyksiköt(oo, config),
      )
      case _ => HttpStatus.ok
    }

  private def validateIbTutkinnonSuoritus(opiskeluoikeus: IBOpiskeluoikeus, config: Config): HttpStatus = {
    // Ib-tutkinnolla voi olla 2 päätason suoritusta
    val suoritusHttpStatus: HttpStatus = opiskeluoikeus.suoritukset.foldLeft(HttpStatus.ok) {
      (accStatus, suoritus) =>
        suoritus match {
          case s: IBTutkinnonSuoritus if predictedArvioinninVaatiminenVoimassa(config) =>
            suorituksenVahvistusVaatiiPredictedArvioinnin(s)
          case _ =>
            accStatus
        }
    }

    suoritusHttpStatus
  }

  def suorituksenVahvistusVaatiiPredictedArvioinnin(päätasonSuoritus: IBTutkinnonSuoritus): HttpStatus =
    if (
      päätasonSuoritus.vahvistettu &&
      päätasonSuoritus.vahvistus.exists(!_.päivä.isBefore(LocalDate.of(2024, 1, 1)))
    ) {
      HttpStatus.validate(päätasonSuoritus.osasuoritukset.exists(_.exists(_.predictedArviointi.exists(!_.isEmpty)))) {
        KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(s"Vahvistettu suoritus ${päätasonSuoritus.koulutusmoduuli.tunniste} ei sisällä vähintään yhtä osasuoritusta, jolla on predicted grade")
      }
    } else {
      HttpStatus.ok
    }


  private def validateIBKurssienLaajuusyksiköt(oo: IBOpiskeluoikeus, config: Config): HttpStatus = {
    HttpStatus.fold(
      oo.suoritukset
        .flatMap(_.osasuoritukset.toList.flatten) // oppiaineet
        .flatMap(_.osasuoritukset.toList.flatten) // kurssit
        .collect { case kurssi: IBKurssinSuoritus => kurssi }
        .map { kurssi => validateIBKurssiLaajuusyksikkö(oo, kurssi.koulutusmoduuli, config) }
    )
  }

  private def validateIBKurssiLaajuusyksikkö(oo: IBOpiskeluoikeus, kurssi: IBKurssi, config: Config): HttpStatus = {
    (oo.alkamispäivä, kurssi.laajuus) match {
      case (Some(alkamispäivä), Some(laajuus)) =>
        val rajapäivä = ibKurssinLaajuusOpintopisteissäAlkaen(config)
        laajuus match {
          case _: LaajuusOpintopisteissä if alkamispäivä.isBefore(rajapäivä) =>
            KoskiErrorCategory.badRequest.validation.laajuudet.osauoritusVääräLaajuus(s"Osasuorituksen laajuuden voi ilmoitettaa opintopisteissä vain ${FinnishDateFormat.format(rajapäivä)} tai myöhemmin alkaneille IB-tutkinnon opiskeluoikeuksille")
          case _: LaajuusKursseissa if alkamispäivä.isEqualOrAfter(rajapäivä) =>
            KoskiErrorCategory.badRequest.validation.laajuudet.osauoritusVääräLaajuus(s"Osasuorituksen laajuus on ilmoitettava opintopisteissä ${FinnishDateFormat.format(rajapäivä)} tai myöhemmin alkaneille IB-tutkinnon opiskeluoikeuksille")
          case _ => HttpStatus.ok
        }
      case _ => HttpStatus.ok
    }
  }

  def predictedArvioinninVaatiminenVoimassa(config: Config): Boolean =
    Option(LocalDate.parse(config.getString("validaatiot.ibSuorituksenVahvistusVaatiiPredictedArvosanan")))
      .exists(_.isEqualOrBefore(LocalDate.now()))

  def ibKurssinLaajuusOpintopisteissäAlkaen(config: Config): LocalDate =
    LocalDate.parse(config.getString("validaatiot.ibLaajuudetOpintopisteinäAlkaen"))

}
