package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, LukionOpiskeluoikeus, LukionOppiaineenOppimääränSuoritus2015, LukionOppiaineidenOppimäärienSuoritus2019, LukionOppimääränSuoritus2015, LukionOppimääränSuoritus2019, Organisaatio, PäätasonSuoritus, Suoritus}

object LukionYhteisetValidaatiot {

  // TOR-1921, TOR-2352: yksittäisille oppilaitoksille sallittava lukion oppimäärän merkitseminen valmistuneeksi ilman laajuuksien validointia
  private val oppilaitoksetIlmanLaajuudenValidointia = Seq("1.2.246.562.10.73692574509", "1.2.246.562.10.43886782945", "1.2.246.562.10.81017043621")

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

  def validateLukioJaAineopiskeluVaihto(oldState: KoskeenTallennettavaOpiskeluoikeus, newState: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = (oldState, newState) match {
    case (o: LukionOpiskeluoikeus, n: LukionOpiskeluoikeus) =>
      def has(code: String)(xs: List[PäätasonSuoritus]) = xs.exists(_.tyyppi.koodiarvo == code)
      val oldHasOppimaara   = has("lukionoppimaara")(o.suoritukset)
      val oldHasAineopinnot = has("lukionaineopinnot")(o.suoritukset)
      val newHasOppimaara   = has("lukionoppimaara")(n.suoritukset)
      val newHasAineopinnot = has("lukionaineopinnot")(n.suoritukset)
      if (oldHasOppimaara && newHasAineopinnot) {
        KoskiErrorCategory.forbidden.kiellettyMuutos("Lukion oppimäärän opiskeluoikeutta ei voi muuttaa aineopiskeluksi.")
      } else if (oldHasAineopinnot && newHasOppimaara) {
        KoskiErrorCategory.forbidden.kiellettyMuutos("Lukion aineopiskelijan opiskeluoikeutta ei voi muuttaa oppimääräksi.")
      } else {
        HttpStatus.ok
      }
    case _ => HttpStatus.ok
  }

  def laajuusValidoitavaOppilaitoksessa(oppilaitosOid: Option[Organisaatio.Oid]): Boolean = {
    oppilaitosOid.isEmpty || oppilaitosOid.exists(oid => !oppilaitoksetIlmanLaajuudenValidointia.contains(oid))
  }

  def validatePäällekkäisetKTjaET(suoritus: Suoritus): HttpStatus = {
    suoritus match {
      case _: LukionOppimääränSuoritus2019 | _: LukionOppimääränSuoritus2015 =>
        val koodiarvot = suoritus.osasuoritukset.getOrElse(Nil).map(_.koulutusmoduuli.tunniste.koodiarvo)
        if (koodiarvot.contains("ET") && koodiarvot.contains("KT")) {
          KoskiErrorCategory.badRequest.validation.rakenne.päällekkäisetETjaKTSuoritukset(
            "Lukion oppimäärän suorituksessa ei voi olla sekä ET- että KT-oppiaineen suorituksia"
          )
        } else {
          HttpStatus.ok
        }

      case _ =>
        HttpStatus.ok
    }
  }
}
