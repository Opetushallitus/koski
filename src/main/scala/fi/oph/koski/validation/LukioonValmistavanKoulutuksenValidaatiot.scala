package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa, LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019, LukioonValmistavanKoulutuksenSuoritus, Suoritus}

object LukioonValmistavanKoulutuksenValidaatiot {
  def validateLukioonValmistava2019(suoritus: Suoritus) = {
    suoritus match {
      case s: LukioonValmistavanKoulutuksenSuoritus => validateLukioonValmistava2019Osasuoritukset(s)
      case _ => HttpStatus.ok
    }
  }

  private def validateLukioonValmistava2019Osasuoritukset(suoritus: LukioonValmistavanKoulutuksenSuoritus) = {
    if (suoritus.osasuoritukset.getOrElse(List()).exists(_.isInstanceOf[LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019]) &&
      suoritus.osasuoritukset.getOrElse(List()).exists(_.isInstanceOf[LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa]))
    {
      KoskiErrorCategory.badRequest.validation.rakenne.lukioonValmistavassaEriLukioOpsienOsasuorituksia()
    }
    else {
      HttpStatus.ok
    }
  }
}

