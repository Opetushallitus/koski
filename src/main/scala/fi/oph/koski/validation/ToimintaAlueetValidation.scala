package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._

object ToimintaAlueetValidation extends Logging {
  def validateToimintaAlueellinenOpiskeluoikeus(oo: Opiskeluoikeus): HttpStatus =
    HttpStatus.fold(oo match {
      case o: PerusopetuksenOpiskeluoikeus => o.suoritukset.collect {
        case s: PerusopetuksenVuosiluokanSuoritus => validatePäätasonSuoritus(s)
        case s: NuortenPerusopetuksenOppimääränSuoritus => validatePäätasonSuoritus(s)
      }
      case o: PerusopetuksenLisäopetuksenOpiskeluoikeus => o.suoritukset.map(validatePäätasonSuoritus)
      case _ => List.empty
    })

  private def validatePäätasonSuoritus(os: KoskeenTallennettavaPäätasonSuoritus): HttpStatus =
    HttpStatus.fold(os.osasuoritukset.toList.flatten.collect {
      case s: PerusopetuksenToiminta_AlueenSuoritus => validateLaajuus(s)
      case s: PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus => validateLaajuus(s)
    })

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
