package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object Lukio2019ArvosanaValidation {

  def validatePäätasonSuoritus(suoritus: Suoritus): HttpStatus = {
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

  private def validateVieraatKielet(suoritus: Suoritus): HttpStatus = {
    val statuses = suoritus.osasuoritukset.toList.flatten.map({
      case k:LukionOppiaineenSuoritus2019
        if vieraatKielet.contains(k.koulutusmoduuli.tunniste.koodiarvo) &&
          k.koulutusmoduuli.pakollinen &&
          k.arviointi.toList.flatten.exists(a => kirjainarvosanat.contains(a.arvosana.koodiarvo)) =>
        KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainValinnaiselle(s"Pakollisen vieraan kielen oppiaineen ${suorituksenTunniste(k)} arvosanan pitää olla numero")
      case k:LukionOppiaineenPreIBSuoritus2019
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
      case k:LukionOppiaineenPreIBSuoritus2019
        if vieraatKielet.contains(k.koulutusmoduuli.tunniste.koodiarvo) &&
          !k.koulutusmoduuli.pakollinen &&
          k.koulutusmoduuli.laajuusArvo(0.0) > 4 &&
          k.viimeisinArvosana.exists(kirjainarvosanat.contains) =>
        KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle(s"Valinnaisen vieraan kielen oppiaineen ${suorituksenTunniste(k)} arvosanan pitää olla numero, jos oppiaineen laajuus on yli 4 op")
      case _ => HttpStatus.ok
    })

    HttpStatus.fold(statuses)
  }

  private def validateLiikunta(suoritus: Suoritus): HttpStatus = {
    val statuses = suoritus.osasuoritukset.toList.flatten.map(s => s match {
      case _:LukionOppiaineenSuoritus2019 | _: LukionOppiaineenPreIBSuoritus2019
        if s.koulutusmoduuli.tunniste.koodiarvo == "LI" &&
          s.koulutusmoduuli.laajuusArvo(0.0) > 2 &&
          s.viimeisinArvosana.contains("H") =>
        KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle(s"Oppiaineen ${suorituksenTunniste(s)} arvosanan pitää olla numero, jos oppiaineen laajuus on yli 2 op")
      case _ => HttpStatus.ok
    })

    HttpStatus.fold(statuses)
  }

  private def validateValtakunnallinenModuuli(suoritus: Suoritus): HttpStatus = (suoritus) match {
    case _: LukionModuulinSuoritus2019 | _: PreIBLukionModuulinSuoritus2019
      if !opintoOhjausModuulit.contains(suoritus.koulutusmoduuli.tunniste.koodiarvo) &&
        suoritus.arviointi.toList.flatten.exists(a => kirjainarvosanat.contains(a.arvosana.koodiarvo)) =>
      KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(s"Valtakunnallisen moduulin ${suorituksenTunniste(suoritus)} arvosanan on oltava numero")
    case _: LukionModuulinSuoritus2019 | _: PreIBLukionModuulinSuoritus2019
      if opintoOhjausModuulit.contains(suoritus.koulutusmoduuli.tunniste.koodiarvo) &&
        !suoritus.arviointi.toList.flatten.exists(a => kirjainarvosanat.contains(a.arvosana.koodiarvo)) =>
      KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(s"Opinto-ohjauksen moduulin ${suorituksenTunniste(suoritus)} arvosanan on oltava S tai H")
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
