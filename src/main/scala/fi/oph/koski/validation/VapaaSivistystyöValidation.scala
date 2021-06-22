package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object VapaaSivistystyöValidation {
  def validateVapaanSivistystyönPäätasonSuoritus(suoritus: VapaanSivistystyönPäätasonSuoritus): HttpStatus = {
    suoritus match {
      case kops: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus if suoritus.vahvistettu => {
        HttpStatus.fold(List(
          validateVapaanSivistystyönPäätasonKOPSSuorituksenLaajuus(kops),
          validateVapaanSivistystyönPäätasonKOPSSuorituksenOsaamiskokonaisuuksienLaajuudet(kops)
        ))
      }
      case koto: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus if suoritus.vahvistettu => {
        HttpStatus.fold(List(
          validateVapaaSivistystyöKOTOVahvistetunPäätasonOsaSuoritukset(koto)
        ))
      }
      case vapaa: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus if suoritus.vahvistettu => {
        HttpStatus.fold(List(
          validateVapaaSivistystyöVapaatavoitteinenVahvistetunPäätasonOsaSuoritukset(vapaa)
        ))
      }
      case _ => {
        HttpStatus.ok
      }
    }
  }

  private def validateVapaanSivistystyönPäätasonKOPSSuorituksenLaajuus(suoritus: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus): HttpStatus = {
    if (hyväksytystiArvioidutOsasuoritukset(suoritus.osasuoritusLista.map(_.osasuoritusLista).flatten).map(_.koulutusmoduuli.laajuusArvo(0)).sum < 53.0) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus("Päätason suoritus " + suorituksenTunniste(suoritus) + " on vahvistettu, mutta sillä ei ole 53 opintopisteen edestä hyväksytyksi arvioituja suorituksia")
    }
    else {
      HttpStatus.ok
    }
  }

  private def validateVapaanSivistystyönPäätasonKOPSSuorituksenOsaamiskokonaisuuksienLaajuudet(suoritus: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus): HttpStatus = {
    if (suoritus.osasuoritusLista.filter(_ match {
      case _:OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus => true
      case _ => false
    })
      .exists(s => hyväksytystiArvioidutOsasuoritukset(s.osasuoritusLista).map(_.koulutusmoduuli.laajuusArvo(0)).sum < 4.0)) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus("Päätason suoritus " + suorituksenTunniste(suoritus) + " on vahvistettu, mutta sillä on hyväksytyksi arvioituja osaamiskokonaisuuksia, joiden laajuus on alle 4 opintopistettä")
    }
    else {
      HttpStatus.ok
    }
  }

  private def hyväksytystiArvioidutOsasuoritukset(suoritukset: List[Suoritus]): List[Suoritus] = {
    suoritukset.filter(_.viimeisinArviointi match {
      case Some(arviointi) if arviointi.hyväksytty => true
      case _ => false
    })
  }

  private def validateVapaaSivistystyöKOTOVahvistetunPäätasonOsaSuoritukset(suoritus: OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus): HttpStatus = {
    if (hyväksytystiArvioidutOsasuoritukset(suoritus.osasuoritusLista).length == suoritus.osasuoritusLista.length) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönKOTOVahvistettuPäätasoHylätyilläOsasuorituksilla()
    }
  }

  private def validateVapaaSivistystyöVapaatavoitteinenVahvistetunPäätasonOsaSuoritukset(suoritus: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus): HttpStatus = {
    suoritus.osasuoritusLista.exists(_.arviointi.isEmpty) match {
      case true => KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVapaatavoitteisenKoulutuksenVahvistettuPäätasoArvioimattomillaOsasuorituksilla()
      case false => HttpStatus.ok
    }
  }

  private def suorituksenTunniste(suoritus: Suoritus): KoodiViite = {
    suoritus.koulutusmoduuli.tunniste
  }
}
