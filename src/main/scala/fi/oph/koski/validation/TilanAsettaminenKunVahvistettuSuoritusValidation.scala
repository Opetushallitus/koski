package fi.oph.koski.validation


import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object TilanAsettaminenKunVahvistettuSuoritusValidation {

  def validateOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
     onEronnut(opiskeluoikeus) match {
      case true => {
        opiskeluoikeus match {
          case _:EsiopetuksenOpiskeluoikeus | _:AmmatillinenOpiskeluoikeus | _:PerusopetuksenLisäopetuksenOpiskeluoikeus => {
            validateYleisetSuoritukset(opiskeluoikeus)
          }
          case p:PerusopetuksenOpiskeluoikeus => {
            validatePerusopetuksenSuoritukset(p)
          }
          case l:LukionOpiskeluoikeus => {
            validateLukionSuoritukset(l)
          }
          case a:AikuistenPerusopetuksenOpiskeluoikeus => {
            validateAikuistenPerusopetuksenSuoritukset(a)
          }
          case _ => HttpStatus.ok
        }
      }
      case false => HttpStatus.ok
    }
  }

  private def onEronnut(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean = {
    opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(ooj => List("katsotaaneronneeksi", "eronnut", "keskeytynyt").contains(ooj.tila.koodiarvo))
  }

  private def validateYleisetSuoritukset(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus.suoritukset.exists(suoritus => suoritus.vahvistettu) match {
      case true => KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus()
      case false => HttpStatus.ok
    }
  }

  private def validatePerusopetuksenSuoritukset(opiskeluoikeus: PerusopetuksenOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus.suoritukset.filter(suoritus => suoritus match {
      case _: NuortenPerusopetuksenOppimääränSuoritus => true
      case _ => false
    }).exists(suoritus => suoritus.vahvistettu) match {
      case true => KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus()
      case false => HttpStatus.ok
    }
  }

  private def validateLukionSuoritukset(opiskeluoikeus: LukionOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus.suoritukset.filter(suoritus => suoritus match {
      case _: LukionOppimääränSuoritus2015 => true
      case _: LukionOppimääränSuoritus2019 => true
      case _ => false
    }).exists(suoritus => suoritus.vahvistettu) match {
      case true => KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus()
      case false => HttpStatus.ok
    }
  }

  private def validateAikuistenPerusopetuksenSuoritukset(opiskeluoikeus: AikuistenPerusopetuksenOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus.suoritukset.filter(suoritus => suoritus match {
      case _: AikuistenPerusopetuksenOppimääränSuoritus => true
      case _ => false
    }).exists(suoritus => suoritus.vahvistettu) match {
      case true => KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus()
      case false => HttpStatus.ok
    }
  }
}
