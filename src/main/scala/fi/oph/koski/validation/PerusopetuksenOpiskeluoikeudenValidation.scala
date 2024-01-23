package fi.oph.koski.validation

import fi.oph.koski.documentation.PerusopetusExampleData.suoritustapaErityinenTutkinto
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.schema.{Aikajakso, AikuistenPerusopetuksenOpiskeluoikeus, Henkilö, HenkilöWithOid, KoskeenTallennettavaOpiskeluoikeus, NuortenPerusopetuksenOppiaineenOppimääränSuoritus, NuortenPerusopetuksenOppimääränSuoritus, Opiskeluoikeus, PerusopetuksenLisäopetuksenOpiskeluoikeus, PerusopetuksenLisäopetuksenSuoritus, PerusopetuksenLisäopetus, PerusopetuksenOpiskeluoikeus, PerusopetuksenPäätasonSuoritus, PerusopetuksenVuosiluokanSuoritus, UusiHenkilö}

object PerusopetuksenOpiskeluoikeusValidation {
  def validatePerusopetuksenOpiskeluoikeus(
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository,
    henkilö: Option[Henkilö],
    oo: Opiskeluoikeus
  ): HttpStatus = {
    oo match {
      case s: PerusopetuksenOpiskeluoikeus => HttpStatus.fold(
        List(validateNuortenPerusopetuksenOpiskeluoikeudenTila(s),
          validateVuosiluokanAlkamispäivät(s),
          validatePäätasonSuoritus(s),
          validateDuplikaatit(henkilöRepository, koskiOpiskeluoikeudet, henkilö, oo)
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

  private def validateDuplikaatit(
    henkilöRepository: HenkilöRepository,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository,
    henkilö: Option[Henkilö],
    oo: Opiskeluoikeus
  ): HttpStatus = {
    def samaOo(toinenOo: Opiskeluoikeus) = {
      val samaOid = toinenOo.oid.isDefined && toinenOo.oid == oo.oid
      val samaLähdejärjestelmänId = toinenOo.lähdejärjestelmänId.isDefined && toinenOo.lähdejärjestelmänId == oo.lähdejärjestelmänId

      samaOid || samaLähdejärjestelmänId
    }

    def oppijallaOnDuplikaatti(oppijaOidit: List[Henkilö.Oid]): Boolean = {
      val vertailtavatOot = koskiOpiskeluoikeudet.findByOppijaOids(oppijaOidit)(KoskiSpecificSession.systemUser)
        .filterNot(samaOo)
        .filter(_.oppilaitos.map(_.oid) == oo.oppilaitos.map(_.oid))
        .filter(_.tyyppi == oo.tyyppi)
        .filter(sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(_) == sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(oo))


      if (sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(oo)) {
        // Oppimäärän opinnot, vain aikajaksoltaan kokonaan erillisiä saa duplikoida
        val jakso = Aikajakso(oo.alkamispäivä, oo.päättymispäivä)
        vertailtavatOot.exists { vertailtavaOo =>
          val muuJakso = Aikajakso(vertailtavaOo.alkamispäivä, vertailtavaOo.päättymispäivä)
          val result = jakso.overlaps(muuJakso)
          result
        }
      } else {
        // aineopinnot, vain päättyneitä saa duplikoida
        vertailtavatOot.exists(_.päättymispäivä.isEmpty)
      }
    }

    val henkilöOid = henkilö match {
      case Some(h: HenkilöWithOid) => Some(h.oid)
      case Some(h: UusiHenkilö) => henkilöRepository.opintopolku.findByHetu(h.hetu) match {
        case Some(henkilö) => Some(henkilö.oid)
        case _ => None
      }
      case _ => None
    }

    henkilöOid
      .flatMap(henkilöOid => henkilöRepository.findByOid(henkilöOid, findMasterIfSlaveOid = true))
      .map(hlö => oppijallaOnDuplikaatti(hlö.kaikkiOidit))
    match {
      case Some(true) => KoskiErrorCategory.conflict.exists()
      case _ => HttpStatus.ok
    }
  }

  def sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(oo: Opiskeluoikeus): Boolean = {
    oo match {
      case poo: PerusopetuksenOpiskeluoikeus
      => poo.suoritukset.map(_.tyyppi.koodiarvo).exists(Set("perusopetuksenoppimaara", "perusopetuksenvuosiluokka").contains)
      case _
      => false
    }
  }
}

