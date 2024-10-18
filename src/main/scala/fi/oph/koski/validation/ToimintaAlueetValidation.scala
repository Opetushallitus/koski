package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.util.ChainingSyntax.localDateOps

import java.time.LocalDate

object ToimintaAlueetValidation extends Logging {
  def validateToimintaAlueellinenOpiskeluoikeus(config: Config)(oo: Opiskeluoikeus): HttpStatus = {
    val rajapäivä = LocalDate.parse(config.getString("validaatiot.toimintaAlueenLaajuudenValidaatioVoimassaJosVahvistusAikaisintaan"))

    HttpStatus.fold(oo match {
      case o: PerusopetuksenOpiskeluoikeus => o.suoritukset.collect {
        case s: PerusopetuksenVuosiluokanSuoritus => validatePäätasonSuoritus(rajapäivä, s)
        case s: NuortenPerusopetuksenOppimääränSuoritus => validatePäätasonSuoritus(rajapäivä, s)
      }
      case _ => List.empty
    })
  }

  private def laajuusValidaatioVoimassa(rajapäivä: LocalDate, suoritus: Suoritus): Boolean =
    suoritus.vahvistus.exists(_.päivä.isEqualOrAfter(rajapäivä))

  private def validatePäätasonSuoritus(rajapäivä: LocalDate, os: KoskeenTallennettavaPäätasonSuoritus): HttpStatus =
    if (laajuusValidaatioVoimassa(rajapäivä, os)) {
      HttpStatus.fold(os.osasuoritukset.toList.flatten.collect {
        case s: PerusopetuksenToiminta_AlueenSuoritus => validateLaajuus(s)
        case s: PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus => validateLaajuus(s)
      })
    } else {
      HttpStatus.ok
    }

  private def validateLaajuus(s: Suoritus): HttpStatus =
    HttpStatus.validate(s.koulutusmoduuli.getLaajuus.isDefined) {
      val oppiaineenNimi = s.koulutusmoduuli.tunniste
        .getNimi
        .map(_.get("fi"))
        .getOrElse(s.koulutusmoduuli.tunniste.koodiarvo)
      KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu(
        s"Oppiaineen ${oppiaineenNimi} laajuus puuttuu"
      )
    }
}
