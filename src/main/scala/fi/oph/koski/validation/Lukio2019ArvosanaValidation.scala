package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object Lukio2019ArvosanaValidation {

  def validatePäätasonSuoritus(suoritus: Suoritus): HttpStatus = {
    validateOppiaineidenArvosanat(suoritus)
  }

  def validateOsasuoritus(suoritus: Suoritus): HttpStatus = {
    HttpStatus.fold(List(
      validateValtakunnallinenModuuli(suoritus)
    ))
  }

  private def validateOppiaineidenArvosanat(suoritus: Suoritus): HttpStatus = {
    HttpStatus.fold(suoritus.osasuoritusLista.flatMap { oppiaine =>
      oppiaine.viimeisinArviointi.map(_.arvosana.koodiarvo).map {
        case "S" =>
          oppiaine.koulutusmoduuli match {
            case o: LukionOppiaine2019 if List("OP", "LI").contains(o.tunniste.koodiarvo) =>
              HttpStatus.ok
            case k: VierasTaiToinenKotimainenKieli2019 if !k.pakollinen =>
              HttpStatus.validate(k.laajuusArvo(0.0) <= 4.0) {
                KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle(s"Valinnaisen vieraan kielen oppiaineen ${suorituksenTunniste(oppiaine)} arvosanan pitää olla numero, jos oppiaineen laajuus on yli 4 op")
              }
            case o: LukionOppiaine2019 if !o.isInstanceOf[PaikallinenLukionOppiaine2019] =>
              HttpStatus.validate(o.laajuusArvo(0.0) <= 2.0) {
                KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle(s"Oppiaineen ${suorituksenTunniste(oppiaine)} arvosanan pitää olla numero, jos oppiaineen laajuus on yli 2 op")
              }
            case _ => HttpStatus.ok
          }
        case "H" =>
          oppiaine.koulutusmoduuli match {
            case o: LukionOppiaine2019 if o.tunniste.koodiarvo == "OP" =>
              HttpStatus.ok
            case o: LukionOppiaine2019 if o.tunniste.koodiarvo == "LI" && o.laajuusArvo(0.0) <= 2 =>
              HttpStatus.ok
            case k: VierasTaiToinenKotimainenKieli2019 if !k.pakollinen =>
              HttpStatus.validate(k.laajuusArvo(0.0) <= 4.0) {
                KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle(s"Valinnaisen vieraan kielen oppiaineen ${suorituksenTunniste(oppiaine)} arvosanan pitää olla numero, jos oppiaineen laajuus on yli 4 op")
              }
            case _: LukionOppiaine2019 =>
              KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainSuppealle(s"Oppiaineen ${suorituksenTunniste(oppiaine)} arvosanan pitää olla numero")
          }
        case numeroArvosana =>
          if (oppiaine.koulutusmoduuli.tunniste.koodiarvo == "OP") {
            KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(s"Opinto-ohjauksen oppiaineen ${suorituksenTunniste(suoritus)} arvosanan on oltava S tai H")
          } else {
            HttpStatus.ok
          }
      }
    })
  }

  private def validateValtakunnallinenModuuli(suoritus: Suoritus): HttpStatus = (suoritus) match {
    case _: LukionModuulinSuoritus2019 | _: PreIBLukionModuulinSuoritus2019
      if !opintoOhjausModuulit.contains(suoritus.koulutusmoduuli.tunniste.koodiarvo) &&
        suoritus.sortedArviointi.exists(a => kirjainarvosanat.contains(a.arvosana.koodiarvo)) =>
      KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(s"Valtakunnallisen moduulin ${suorituksenTunniste(suoritus)} arvosanan on oltava numero")
    case _: LukionModuulinSuoritus2019 | _: PreIBLukionModuulinSuoritus2019
      if opintoOhjausModuulit.contains(suoritus.koulutusmoduuli.tunniste.koodiarvo) &&
        !suoritus.sortedArviointi.exists(a => kirjainarvosanat.contains(a.arvosana.koodiarvo)) =>
      KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(s"Opinto-ohjauksen moduulin ${suorituksenTunniste(suoritus)} arvosanan on oltava S tai H")
    case _ =>
      HttpStatus.ok
  }

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }

  private val kirjainarvosanat = List("H", "S")

  private val opintoOhjausModuulit = List("OP1", "OP2")
}
