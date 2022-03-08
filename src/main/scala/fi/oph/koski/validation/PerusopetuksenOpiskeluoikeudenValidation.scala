package fi.oph.koski.validation

import fi.oph.koski.documentation.PerusopetusExampleData.suoritustapaErityinenTutkinto
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AikuistenPerusopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, NuortenPerusopetuksenOppiaineenOppimääränSuoritus, NuortenPerusopetuksenOppimääränSuoritus, Opiskeluoikeus, PerusopetuksenLisäopetuksenOpiskeluoikeus, PerusopetuksenLisäopetuksenSuoritus, PerusopetuksenLisäopetus, PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus}

object PerusopetuksenOpiskeluoikeusValidation {
  def validatePerusopetuksenOpiskeluoikeus(oo: Opiskeluoikeus) = {
    oo match {
      case s: PerusopetuksenOpiskeluoikeus => HttpStatus.fold(
        List(validateNuortenPerusopetuksenOpiskeluoikeudenTila(s),
          validateVuosiluokanAlkamispäivät(s)
        ))
      case _ => HttpStatus.ok
    }
  }

  private def validateVuosiluokanAlkamispäivät(oo: PerusopetuksenOpiskeluoikeus): HttpStatus = {
    oo.päättymispäivä match {
      case Some(päättymispäivä) =>
        oo.suoritukset.exists{
          case vuosi: PerusopetuksenVuosiluokanSuoritus => vuosi.alkamispäivä match {
            case Some(alkamispäivä) => alkamispäivä.isAfter(päättymispäivä)
            case None => false
            }
        case _:Any => false
      } match {
          case true => KoskiErrorCategory.badRequest.validation.date.päättymisPäiväEnnenAlkamispäivää("Perusopetuksen opiskeluoikeuden päättymispäivä ei voi olla vuosiluokan suorituksen alkamispäivää ennen")
          case false => HttpStatus.ok
        }
      case None => HttpStatus.ok
    }
  }

  private def validateNuortenPerusopetuksenOpiskeluoikeudenTila(oo: PerusopetuksenOpiskeluoikeus) = {
    if (oo.tila.opiskeluoikeusjaksot.last.tila.koodiarvo == "valmistunut") {
      HttpStatus.fold(List(
        if (oo.suoritukset.filter(_.isInstanceOf[NuortenPerusopetuksenOppimääränSuoritus]).exists(_.vahvistettu) ||
            oo.suoritukset.filter(_.isInstanceOf[NuortenPerusopetuksenOppiaineenOppimääränSuoritus]).nonEmpty) {
          HttpStatus.ok
        }
        else {
          KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenValmistunutTilaIlmanVahvistettuaPäättötodistusta()
        },
        validateValmistuneellaOpiskeluoikeudellaYhdeksäsLuokkaTaiSitäEiTarvita(oo)
      ))
    } else {
      HttpStatus.ok
    }
  }

  private def validateValmistuneellaOpiskeluoikeudellaYhdeksäsLuokkaTaiSitäEiTarvita(oo: PerusopetuksenOpiskeluoikeus) = {
    val ysiluokanSuoritusOlemassa = oo.suoritukset.exists {
      case vuosi: PerusopetuksenVuosiluokanSuoritus => vuosi.koulutusmoduuli.tunniste.koodiarvo == "9"
      case _: Any => false
    }

    val vuosiluokkiinSitoutumatonOpetus = oo.lisätiedot.exists(_.vuosiluokkiinSitoutumatonOpetus)

    val kotiopetusVoimassaPäättötodistuksenVahvistuspäivänä = oo.suoritukset.exists {
      case päättö: NuortenPerusopetuksenOppimääränSuoritus => {
        println(päättö.vahvistus)

        päättö.vahvistus.exists(vahvistus => {
          println(oo.kotiopetuksessa(vahvistus.päivä))
          oo.kotiopetuksessa(vahvistus.päivä)
        })
      }
      case _: Any => false
    }

    val erityinenTutkinto = oo.suoritukset.exists {
      case päättö: NuortenPerusopetuksenOppimääränSuoritus => päättö.suoritustapa == suoritustapaErityinenTutkinto
      case _: Any => false
    }

    if (ysiluokanSuoritusOlemassa || vuosiluokkiinSitoutumatonOpetus || kotiopetusVoimassaPäättötodistuksenVahvistuspäivänä || erityinenTutkinto) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenValmistunutTilaIlmanYsiluokanSuoritusta()
    }
  }

  def filterDeprekoidutKentät(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case perus: PerusopetuksenOpiskeluoikeus =>
        filterNuortenOpiskeluoikeudenKentät(perus)
      case lisä: PerusopetuksenLisäopetuksenOpiskeluoikeus =>
        filterLisäopetukseenOpiskeluoikeudenKentät(lisä)
      case aikuis: AikuistenPerusopetuksenOpiskeluoikeus =>
        filterAikuistenOpiskeluoikeudenKentät(aikuis)
      case _ => oo
    }
  }

  def filterNuortenOpiskeluoikeudenKentät(perus: PerusopetuksenOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    val filtteröityLisätieto = perus.lisätiedot.map(lisätieto => {
      lisätieto.copy(
        perusopetuksenAloittamistaLykätty = None,
        tehostetunTuenPäätökset = None
      )
    })

    val filtteröidytSuoritukset = perus.suoritukset.map {
      case vuosiluokka: PerusopetuksenVuosiluokanSuoritus =>
        vuosiluokka.copy(
          osaAikainenErityisopetus = None
        )
      case muu: Any => muu
    }

    perus.withLisätiedot(filtteröityLisätieto).withSuoritukset(filtteröidytSuoritukset)
  }

  def filterLisäopetukseenOpiskeluoikeudenKentät(lisä: PerusopetuksenLisäopetuksenOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    val filtteröityLisätieto = lisä.lisätiedot.map(lisätieto => {
      lisätieto.copy(
        perusopetuksenAloittamistaLykätty = None,
        tehostetunTuenPäätökset = None
      )
    })

    val filtteröidytSuoritukset = lisä.suoritukset.map(
      _.copy(
        osaAikainenErityisopetus = None
      )
    )
    lisä.withLisätiedot(filtteröityLisätieto).withSuoritukset(filtteröidytSuoritukset)
  }

  def filterAikuistenOpiskeluoikeudenKentät(aikuinen: AikuistenPerusopetuksenOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    val filtteröityLisätieto = aikuinen.lisätiedot.map(lisätieto => {
      lisätieto.copy(
        tehostetunTuenPäätökset = None
      )
    })

    aikuinen.withLisätiedot(filtteröityLisätieto)
  }
}

