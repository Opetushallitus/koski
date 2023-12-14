package fi.oph.koski.validation

import fi.oph.koski.documentation.PerusopetusExampleData.suoritustapaErityinenTutkinto
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AikuistenPerusopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, NuortenPerusopetuksenOppiaineenOppimääränSuoritus, NuortenPerusopetuksenOppimääränSuoritus, Opiskeluoikeus, PerusopetuksenLisäopetuksenOpiskeluoikeus, PerusopetuksenLisäopetuksenSuoritus, PerusopetuksenLisäopetus, PerusopetuksenOpiskeluoikeus, PerusopetuksenPäätasonSuoritus, PerusopetuksenVuosiluokanSuoritus}

object PerusopetuksenOpiskeluoikeusValidation {
  def validatePerusopetuksenOpiskeluoikeus(oo: Opiskeluoikeus) = {
    oo match {
      case s: PerusopetuksenOpiskeluoikeus => HttpStatus.fold(
        List(validateNuortenPerusopetuksenOpiskeluoikeudenTila(s),
          validateVuosiluokanAlkamispäivät(s),
          validatePäätasonSuoritus(s)
        ))
      case _ => HttpStatus.ok
    }
  }

  private def validateVuosiluokanAlkamispäivät(oo: PerusopetuksenOpiskeluoikeus): HttpStatus = {
    oo.päättymispäivä match {
      case Some(päättymispäivä) =>
        oo.suoritukset.find{
          case vuosi: PerusopetuksenVuosiluokanSuoritus => vuosi.alkamispäivä match {
            case Some(alkamispäivä) => alkamispäivä.isAfter(päättymispäivä)
            case None => false
            }
        case _:Any => false
      } match {
          case Some(suoritus) => KoskiErrorCategory.badRequest.validation.date.päättymisPäiväEnnenAlkamispäivää(s"Vuosiluokan ${suoritus.asInstanceOf[PerusopetuksenVuosiluokanSuoritus].koulutusmoduuli.tunniste.koodiarvo} suoritus ei voi alkaa opiskeluoikeuden päättymisen jälkeen")
          case None => HttpStatus.ok
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
        }
      ))
    } else {
      HttpStatus.ok
    }
  }

  private def validatePäätasonSuoritus(oo: PerusopetuksenOpiskeluoikeus): HttpStatus = {
    HttpStatus.fold(
      oo.suoritukset.map {
        case suoritus: NuortenPerusopetuksenOppimääränSuoritus if suoritus.vahvistettu =>
          validateValmistuneellaOpiskeluoikeudellaYhdeksäsLuokkaTaiSitäEiTarvita(oo)
        case _ => HttpStatus.ok
      } ++
      oo.suoritukset.map(validateEtJaKt)
    )
  }

  private def validateValmistuneellaOpiskeluoikeudellaYhdeksäsLuokkaTaiSitäEiTarvita(oo: PerusopetuksenOpiskeluoikeus) = {
    val aineopiskelija = oo.suoritukset.filter(_.isInstanceOf[NuortenPerusopetuksenOppiaineenOppimääränSuoritus]).nonEmpty

    val ysiluokanSuoritusOlemassa = oo.suoritukset.exists {
      case vuosi: PerusopetuksenVuosiluokanSuoritus => vuosi.koulutusmoduuli.tunniste.koodiarvo == "9"
      case _: Any => false
    }

    val vuosiluokkiinSitoutumatonOpetus = onVuosiluokkiinSitoutumatonOpetus(oo)

    val kotiopetusVoimassaPäättötodistuksenVahvistuspäivänä = oo.suoritukset.exists {
      case päättö: NuortenPerusopetuksenOppimääränSuoritus => päättö.vahvistus.exists(vahvistus => {
          oo.kotiopetuksessa(vahvistus.päivä)
        })
      case _: Any => false
    }

    val erityinenTutkinto = oo.suoritukset.exists {
      case päättö: NuortenPerusopetuksenOppimääränSuoritus => päättö.suoritustapa == suoritustapaErityinenTutkinto
      case _: Any => false
    }

    if (aineopiskelija || ysiluokanSuoritusOlemassa || vuosiluokkiinSitoutumatonOpetus || kotiopetusVoimassaPäättötodistuksenVahvistuspäivänä || erityinenTutkinto) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenValmistunutTilaIlmanYsiluokanSuoritusta()
    }
  }

  def onVuosiluokkiinSitoutumatonOpetus(oo: KoskeenTallennettavaOpiskeluoikeus): Boolean = oo match {
    case p: PerusopetuksenOpiskeluoikeus => p.lisätiedot.exists(_.vuosiluokkiinSitoutumatonOpetus)
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

  private def filterNuortenOpiskeluoikeudenKentät(perus: PerusopetuksenOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
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

  private def filterLisäopetukseenOpiskeluoikeudenKentät(lisä: PerusopetuksenLisäopetuksenOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
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

  private def filterAikuistenOpiskeluoikeudenKentät(aikuinen: AikuistenPerusopetuksenOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    val filtteröityLisätieto = aikuinen.lisätiedot.map(lisätieto => {
      lisätieto.copy(
        tehostetunTuenPäätökset = None
      )
    })

    aikuinen.withLisätiedot(filtteröityLisätieto)
  }

  private def validateEtJaKt(suoritus: PerusopetuksenPäätasonSuoritus): HttpStatus = {
    val ktJaEt = suoritus.osasuoritukset.toList.flatten
      .map(_.koulutusmoduuli.tunniste.koodiarvo)
      .filter(List("KT", "ET").contains)

    HttpStatus.validate(ktJaEt.toSet.size <= 1) {
      KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Samassa perusopetuksen suorituksessa ei voi esiintyä oppiaineita KT- ja ET-koodiarvoilla")
    }
  }
}

