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
      case Some(s: IBTutkinnonSuoritus) if predictedJaPäättöarvioinninVaatimisenVoimassa(config) =>
        suorituksenVahvistusVaatiiPredictedJaPäättöarvosanan(s)
      case _ =>
        HttpStatus.ok
    }

  def suorituksenVahvistusVaatiiPredictedJaPäättöarvosanan(päätasonSuoritus: IBTutkinnonSuoritus): HttpStatus =
    if (päätasonSuoritus.vahvistettu) {
      HttpStatus.fold(
        päätasonSuoritus.osasuoritukset
          .getOrElse(List.empty)
          .map(vaadiPredictedJaPäättöarvosana(päätasonSuoritus))
      )
    } else {
      HttpStatus.ok
    }

  def vaadiPredictedJaPäättöarvosana(päätasonSuoritus: IBTutkinnonSuoritus)(osasuoritus: IBOppiaineenSuoritus): HttpStatus = {
    val (päättöarvioinnit, predictedGrades) = ibOppiaineenArvioinnit(osasuoritus)
    if (päättöarvioinnit.isEmpty) {
      KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(s"Vahvistetun suorituksen ${päätasonSuoritus.koulutusmoduuli.tunniste} osasuoritukselta ${osasuoritus.koulutusmoduuli.tunniste} puuttuu päättöarvosana")
    } else if (predictedGrades.isEmpty) {
      KoskiErrorCategory.badRequest.validation.arviointi.arviointiPuuttuu(s"Vahvistetun suorituksen ${päätasonSuoritus.koulutusmoduuli.tunniste} osasuoritukselta ${osasuoritus.koulutusmoduuli.tunniste} puuttuu predicted grade")
    } else {
      HttpStatus.ok
    }
  }

  // Tuetaan vanhempaa tietomallin tapaa tallentaa päättöarvosanat ja predicted gradet samaan listaan (nykyään eritelty kahteen eri listaan)
  def ibOppiaineenArvioinnit(osasuoritus: IBOppiaineenSuoritus): (List[IBOppiaineenArviointi], List[IBOppiaineenPredictedArviointi]) = (
    osasuoritus.arviointi
      .getOrElse(List.empty)
      .filterNot(_.predicted),
    osasuoritus.predictedArviointi
      .getOrElse(osasuoritus.arviointi
        .getOrElse(List.empty)
        .filter(_.predicted)
        .map(IBOppiaineenPredictedArviointi.apply)
      )
  )

  def predictedJaPäättöarvioinninVaatimisenVoimassa(config: Config): Boolean =
    Option(LocalDate.parse(config.getString("validaatiot.ibSuorituksenVahvistusVaatiiPredictedJaPäättöarvosanan")))
      .exists(_.isEqualOrBefore(LocalDate.now()))
}
