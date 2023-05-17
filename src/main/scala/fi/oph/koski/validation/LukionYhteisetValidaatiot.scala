package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{LukionOppiaineenOppimääränSuoritus2015, LukionOppiaineidenOppimäärienSuoritus2019, Organisaatio, Suoritus}

object LukionYhteisetValidaatiot {

  // TOR-1921: yksittäisille oppilaitoksille sallittava lukion oppimäärän merkitseminen valmistuneeksi ilman laajuuksien validointia
  private val oppilaitoksetIlmanLaajuudenValidointia = Seq("1.2.246.562.10.73692574509", "1.2.246.562.10.43886782945")

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

  def laajuusValidoitavaOppilaitoksessa(oppilaitosOid: Option[Organisaatio.Oid]): Boolean = {
    oppilaitosOid.isEmpty || oppilaitosOid.exists(oid => !oppilaitoksetIlmanLaajuudenValidointia.contains(oid))
  }
}
