package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{LukionOppiaineenOppimääränSuoritus2015, LukionOppiaineidenOppimäärienSuoritus2019, LukionPäätasonSuoritus, Suoritus}

object LukionYhteisetValidaatiot {
  def validateLukionPäätasonSuoritus(suoritus: Suoritus): HttpStatus = {
    suoritus match {
      case aine2019: LukionOppiaineidenOppimäärienSuoritus2019 => virheviestiJosLukionOppimääräSuoritettuKenttäMääritelty(aine2019.lukionOppimääräSuoritettu)
      case aine2015: LukionOppiaineenOppimääränSuoritus2015 => virheviestiJosLukionOppimääräSuoritettuKenttäMääritelty(aine2015.lukionOppimääräSuoritettu)
      case _ => HttpStatus.ok
    }
  }

  def virheviestiJosLukionOppimääräSuoritettuKenttäMääritelty(lukionOppimääräSuoritettu: Option[Boolean]) = {
    if (lukionOppimääräSuoritettu.isDefined) {
      KoskiErrorCategory.badRequest.validation.rakenne.deprekoituLukionAineopintojenPäätasonSuorituksenKenttä()
    } else {
      HttpStatus.ok
    }
  }
}
