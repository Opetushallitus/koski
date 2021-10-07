package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object VapaaSivistystyöValidation {
  def validateVapaanSivistystyönPäätasonSuoritus(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    suoritus match {
      case suoritus:VapaanSivistystyönPäätasonSuoritus => {
        HttpStatus.fold(List(
          validateTilanKoodiarvot(suoritus, opiskeluoikeus),
          suoritus match {
            case kops: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus if suoritus.vahvistettu => {
              HttpStatus.fold(List(
                validateVapaanSivistystyönPäätasonKOPSSuorituksenLaajuus(kops),
                validateVapaanSivistystyönPäätasonKOPSSuorituksenOsaamiskokonaisuuksienLaajuudet(kops)
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
        ))
      }
      case _ => HttpStatus.ok
    }
  }

  private def validateTilanKoodiarvot(suoritus: VapaanSivistystyönPäätasonSuoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    suoritus match {
      case _: VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus => {
        if (opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(jakso => !List("hyvaksytystisuoritettu", "keskeytynyt").contains(jakso.tila.koodiarvo))) {
          KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönOpiskeluoikeudellaVääräTila()
        } else {
          HttpStatus.ok
        }
      }
      case _ => {
        if (opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(jakso => List("hyvaksytystisuoritettu", "keskeytynyt").contains(jakso.tila.koodiarvo))) {
          KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönOpiskeluoikeudellaVääräTila()
        } else {
          HttpStatus.ok
        }
      }
    }
  }

  private def validateVapaanSivistystyönPäätasonKOPSSuorituksenLaajuus(suoritus: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus): HttpStatus = {
    if (suoritus.osasuoritusLista.map(_.osasuoritusLista).flatten.map(_.koulutusmoduuli.laajuusArvo(0)).sum < 53.0) {
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
      .exists(s => s.osasuoritusLista.map(_.koulutusmoduuli.laajuusArvo(0)).sum < 4.0)) {
      KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus("Päätason suoritus " + suorituksenTunniste(suoritus) + " on vahvistettu, mutta sillä on hyväksytyksi arvioituja osaamiskokonaisuuksia, joiden laajuus on alle 4 opintopistettä")
    }
    else {
      HttpStatus.ok
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
