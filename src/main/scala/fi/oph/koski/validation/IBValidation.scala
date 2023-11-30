package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{IBOpiskeluoikeus, IBOppiaineenArviointi, IBOppiaineenPredictedArviointi, IBOppiaineenSuoritus, IBPäätasonSuoritus, IBTutkinnonSuoritus, KoskeenTallennettavaOpiskeluoikeus}
import fi.oph.koski.util.ChainingSyntax._

import java.time.LocalDate

object IBValidation {
  def validateIbOpiskeluoikeus(config: Config)(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    opiskeluoikeus match {
      case oo: IBOpiskeluoikeus => validateIbTutkinnonSuoritus(oo, config)
      case _ => HttpStatus.ok
    }

  private def validateIbTutkinnonSuoritus(opiskeluoikeus: IBOpiskeluoikeus, config: Config): HttpStatus =
    opiskeluoikeus.suoritukset.headOption match {
      case Some(s: IBTutkinnonSuoritus) if predictedArvioinninVaatiminenVoimassa(config) =>
        suorituksenVahvistusVaatiiPredictedArvioinnin(s)
      case _ =>
        HttpStatus.ok
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

  def predictedArvioinninVaatiminenVoimassa(config: Config): Boolean =
    Option(LocalDate.parse(config.getString("validaatiot.ibSuorituksenVahvistusVaatiiPredictedArvosanan")))
      .exists(_.isEqualOrBefore(LocalDate.now()))
}
