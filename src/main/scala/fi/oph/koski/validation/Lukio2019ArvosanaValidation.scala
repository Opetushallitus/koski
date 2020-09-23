package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object Lukio2019ArvosanaValidation {

  def validatePäätasonSuoritus(suoritus: LukionPäätasonSuoritus2019): HttpStatus = {
    HttpStatus.fold(List(
      validateVieraatKielet(suoritus),
      validateLiikunta(suoritus)
    ))
  }

  def validateOsasuoritus(suoritus: Suoritus): HttpStatus = {
    HttpStatus.fold(List(
      validateValtakunnallinenModuuli(suoritus)
    ))
  }

  private def validateVieraatKielet(suoritus: LukionPäätasonSuoritus2019): HttpStatus = {
    val statii = suoritus.osasuoritukset.toList.flatten.map({
      case k:LukionOppiaineenSuoritus2019
        if vieraatKielet.contains(k.koulutusmoduuli.tunniste.koodiarvo) &&
          k.koulutusmoduuli.pakollinen &&
          k.arviointi.toList.flatten.exists(a => kirjainarvosanat.contains(a.arvosana.koodiarvo)) =>
        KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainValinnaiselle(s"Pakollisen vieraan kielen oppiaineen ${suorituksenTunniste(k)} arvosanan pitää olla numero")
      case k:LukionOppiaineenSuoritus2019
        if vieraatKielet.contains(k.koulutusmoduuli.tunniste.koodiarvo) &&
          !k.koulutusmoduuli.pakollinen &&
          k.koulutusmoduuli.laajuusArvo(0.0) > 4 &&
          k.viimeisinArvosana.exists(kirjainarvosanat.contains) =>
        KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle(s"Valinnaisen vieraan kielen oppiaineen ${suorituksenTunniste(k)} arvosanan pitää olla numero, jos oppiaineen laajuus on yli 4 op")
      case _ => HttpStatus.ok
    })

    HttpStatus.fold(statii)
  }

  private def validateLiikunta(suoritus: LukionPäätasonSuoritus2019): HttpStatus = {
    val statii = suoritus.osasuoritukset.toList.flatten.map({
      case l:LukionOppiaineenSuoritus2019
        if l.koulutusmoduuli.tunniste.koodiarvo == "LI" &&
          l.koulutusmoduuli.laajuusArvo(0.0) > 2 &&
          l.viimeisinArvosana.exists(kirjainarvosanat.contains) =>
        KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle(s"Oppiaineen ${suorituksenTunniste(l)} arvosanan pitää olla numero, jos oppiaineen laajuus on yli 2 op")
      case _ => HttpStatus.ok
    })

    HttpStatus.fold(statii)
  }

  private def validateValtakunnallinenModuuli(suoritus: Suoritus): HttpStatus = (suoritus) match {
    case (s: LukionModuulinSuoritus2019)
      if !opintoOhjausModuulit.contains(s.koulutusmoduuli.tunniste.koodiarvo) &&
        s.arviointi.toList.flatten.exists(a => kirjainarvosanat.contains(a.arvosana.koodiarvo)) =>
      KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(s"Valtakunnallisen moduulin ${suorituksenTunniste(s)} arvosanan on oltava numero")
    case (s: LukionModuulinSuoritus2019)
      if opintoOhjausModuulit.contains(s.koulutusmoduuli.tunniste.koodiarvo) &&
        !s.arviointi.toList.flatten.exists(a => kirjainarvosanat.contains(a.arvosana.koodiarvo)) =>
      KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(s"Opinto-ohjauksen moduulin ${suorituksenTunniste(s)} arvosanan on oltava S tai H")
    case _ =>
      HttpStatus.ok
  }

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }

  private val vieraatKielet = List("A", "B1", "B2", "B3", "AOM")
  private val kirjainarvosanat = List("H", "S")

  private val opintoOhjausModuulit = List("OP1", "OP2")
}
