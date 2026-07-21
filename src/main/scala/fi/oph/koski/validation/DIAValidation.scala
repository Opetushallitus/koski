package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.util.ChainingSyntax._
import fi.oph.koski.util.FinnishDateFormat

import java.time.LocalDate

object DIAValidation {
  def validateOpiskeluoikeus(config: Config)(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    opiskeluoikeus match {
      case oo: DIAOpiskeluoikeus =>
        oo.alkamispäivä match {
          case Some(alkamispäivä) => validateLaajuusyksiköt(oo, alkamispäivä, rajapäivä(config))
          case None => HttpStatus.ok
        }
      case _ => HttpStatus.ok
    }

  private def rajapäivä(config: Config): LocalDate =
    LocalDate.parse(config.getString("validaatiot.diaLaajuudetOpintopisteinäAlkaen"))

  private def validateLaajuusyksiköt(oo: DIAOpiskeluoikeus, alkamispäivä: LocalDate, rajapäivä: LocalDate): HttpStatus =
    HttpStatus.fold(kaikkiLaajuudet(oo).map(validateLaajuusyksikkö(_, alkamispäivä, rajapäivä)))

  private def kaikkiLaajuudet(oo: DIAOpiskeluoikeus): List[Laajuus] =
    oo.suoritukset.flatMap(s => s :: s.rekursiivisetOsasuoritukset).flatMap(_.koulutusmoduuli.getLaajuus)

  private def validateLaajuusyksikkö(laajuus: Laajuus, alkamispäivä: LocalDate, rajapäivä: LocalDate): HttpStatus =
    laajuus match {
      case _: LaajuusVuosiviikkotunneissa if alkamispäivä.isEqualOrAfter(rajapäivä) =>
        KoskiErrorCategory.badRequest.validation.laajuudet.osauoritusVääräLaajuus(
          s"DIA-tutkinnon laajuus on ilmoitettava opintopisteissä ${FinnishDateFormat.format(rajapäivä)} tai myöhemmin alkaneille opiskeluoikeuksille"
        )
      case _: LaajuusOpintopisteissä if alkamispäivä.isBefore(rajapäivä) =>
        KoskiErrorCategory.badRequest.validation.laajuudet.osauoritusVääräLaajuus(
          s"DIA-tutkinnon laajuuden voi ilmoittaa opintopisteissä vain ${FinnishDateFormat.format(rajapäivä)} tai myöhemmin alkaneille opiskeluoikeuksille"
        )
      case _ => HttpStatus.ok
    }
}
