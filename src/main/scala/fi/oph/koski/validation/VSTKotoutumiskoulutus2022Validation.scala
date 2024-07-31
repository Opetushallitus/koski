package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.util.ChainingSyntax._

import java.time.LocalDate

object VSTKotoutumiskoulutus2022Validation {
  def validate(opiskeluoikeus: Opiskeluoikeus): HttpStatus =
    opiskeluoikeus match {
      case opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus =>
        validateAlkamispäivä(opiskeluoikeus)
      case _ =>
        HttpStatus.ok
    }

  def validate(suoritus: Suoritus): HttpStatus = {
    suoritus match {
      case päätasonSuoritus: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
        if päätasonSuoritus.vahvistettu && päätasonSuoritus.koulutusmoduuli.laajuusArvo(0.0) < 53.0 =>
        KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus()
      case _ => HttpStatus.ok
    }
  }

  def validateAlkamispäivä(opiskeluoikeus: VapaanSivistystyönOpiskeluoikeus): HttpStatus = {
    val opsinVaihtumispäivä = LocalDate.of(2022, 8, 1)

    val onRajapäivänJälkeen = opiskeluoikeus.alkamispäivä.map(_.isEqualOrAfter(opsinVaihtumispäivä))

    HttpStatus.fold(opiskeluoikeus.suoritukset.map(päätasonSuoritus => {
      val pitääOllaRajapäivänJälkeen = päätasonSuoritus match {
        case _: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 => Some(true)
        case _: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus     => Some(false)
        case _ => None
      }

      (onRajapäivänJälkeen, pitääOllaRajapäivänJälkeen) match {
        case (Some(true), Some(false))  => KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoAlkamispäivä2012()
        case (Some(false), Some(true))  => KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoAlkamispäivä2022()
        case _ => HttpStatus.ok
      }
    }))
  }
}
