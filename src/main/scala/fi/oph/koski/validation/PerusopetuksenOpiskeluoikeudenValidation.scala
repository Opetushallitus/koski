package fi.oph.koski.validation

import fi.oph.koski.documentation.PerusopetusExampleData.suoritustapaErityinenTutkinto
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.{CompositeOpiskeluoikeusRepository}
import fi.oph.koski.schema.{Aikajakso, AikuistenPerusopetuksenOpiskeluoikeus, EsiopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, NuortenPerusopetuksenOppiaineenOppimääränSuoritus, NuortenPerusopetuksenOppimääränSuoritus, Opiskeluoikeus, PerusopetukseenValmistavanOpetuksenOpiskeluoikeus, PerusopetuksenLisäopetuksenOpiskeluoikeus, PerusopetuksenOpiskeluoikeus, PerusopetuksenPäätasonSuoritus, PerusopetuksenVuosiluokanSuoritus}

object PerusopetuksenOpiskeluoikeusValidation {
  def validatePerusopetuksenOpiskeluoikeus(
    oo: Opiskeluoikeus
  ): HttpStatus = {
    oo match {
      case poo: PerusopetuksenOpiskeluoikeus => HttpStatus.fold(
        List(validateNuortenPerusopetuksenOpiskeluoikeudenTila(poo),
          validateVuosiluokanAlkamispäivät(poo),
          validatePäätasonSuoritus(poo)
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

  def validateDuplikaatit(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    oppijanHenkilötiedot: Option[LaajatOppijaHenkilöTiedot],
    opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository
  ): HttpStatus = {
    def samaOidTaiLähdejärjestelmänId(oo: KoskeenTallennettavaOpiskeluoikeus)(toinenOo: Opiskeluoikeus): Boolean = {
      val samaOid = toinenOo.oid.isDefined && toinenOo.oid == oo.oid
      val samaLähdejärjestelmänId = toinenOo.lähdejärjestelmänId.isDefined && toinenOo.lähdejärjestelmänId == oo.lähdejärjestelmänId
      samaOid || samaLähdejärjestelmänId
    }

    def oppijanOpiskeluoikeudet(oppijanHenkilötiedot: LaajatOppijaHenkilöTiedot): Either[HttpStatus, Seq[Opiskeluoikeus]] = opiskeluoikeusRepository.findByOppija(
      tunnisteet = oppijanHenkilötiedot,
      useVirta = false,
      useYtr = false
    )(KoskiSpecificSession.systemUser)
    .warningsToLeft

    def samaOppilaitosJaTyyppi(oo: KoskeenTallennettavaOpiskeluoikeus)(oppijanHenkilötiedot: LaajatOppijaHenkilöTiedot): Either[HttpStatus, Seq[Opiskeluoikeus]] = oppijanOpiskeluoikeudet(oppijanHenkilötiedot)
      .map(_
        .filterNot(samaOidTaiLähdejärjestelmänId(oo))
        .filter(_.oppilaitos.map(_.oid) == oo.oppilaitos.map(_.oid))
        .filter(_.tyyppi == oo.tyyppi)
      )

    def päällekkäinenAikajakso(oo: KoskeenTallennettavaOpiskeluoikeus)(vertailtavatOot: Seq[Opiskeluoikeus]): Boolean = {
      val jakso = Aikajakso(oo.alkamispäivä, oo.päättymispäivä)
      vertailtavatOot.exists { vertailtavaOo =>
        val muuJakso = Aikajakso(vertailtavaOo.alkamispäivä, vertailtavaOo.päättymispäivä)
        jakso.overlaps(muuJakso)
      }
    }

    def oppijallaOnDuplikaatti(oppijanHenkilötiedot: LaajatOppijaHenkilöTiedot, oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, Boolean] = {
      val vertailtavatOot = samaOppilaitosJaTyyppi(oo)(oppijanHenkilötiedot)
      vertailtavatOot.map(päällekkäinenAikajakso(oo))
    }

    def oppijallaOnDuplikaattiPerusopetus(oppijanHenkilötiedot: LaajatOppijaHenkilöTiedot, oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, Boolean] = {
      val vertailtavatOot: Either[HttpStatus, Seq[Opiskeluoikeus]] = samaOppilaitosJaTyyppi(oo)(oppijanHenkilötiedot).map(_
        .filter(sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(_) == sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(oo))
      )

      if (sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(oo)) {
        // Oppimäärän opinnot, vain aikajaksoltaan kokonaan erillisiä saa duplikoida
        vertailtavatOot.map(päällekkäinenAikajakso(oo))
      } else {
        // aineopinnot, vain päättyneitä saa duplikoida
        vertailtavatOot.map(_.exists(_.päättymispäivä.isEmpty))
      }
    }

    def handleDuplikaattivalidaatio(validaatio: (LaajatOppijaHenkilöTiedot, KoskeenTallennettavaOpiskeluoikeus) => Either[HttpStatus, Boolean])(hlö: LaajatOppijaHenkilöTiedot, oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
      validaatio(hlö, oo) match {
        case Right(true) => KoskiErrorCategory.conflict.exists()
        case Right(false) => HttpStatus.ok
        case Left(error) => error
      }
    }

    opiskeluoikeus match {
      case oo: EsiopetuksenOpiskeluoikeus =>
        oppijanHenkilötiedot match {
          case Some(h) => handleDuplikaattivalidaatio(oppijallaOnDuplikaatti)(h, oo)
          case _ => HttpStatus.ok
        }

      case oo: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus =>
        oppijanHenkilötiedot match {
          case Some(h) => handleDuplikaattivalidaatio(oppijallaOnDuplikaatti)(h, oo)
          case _ => HttpStatus.ok
        }

      case oo: AikuistenPerusopetuksenOpiskeluoikeus =>
        oppijanHenkilötiedot match {
          case Some(h) => handleDuplikaattivalidaatio(oppijallaOnDuplikaatti)(h, oo)
          case _ => HttpStatus.ok
        }

      case oo: PerusopetuksenOpiskeluoikeus =>
        oppijanHenkilötiedot match {
          case Some(h) => handleDuplikaattivalidaatio(oppijallaOnDuplikaattiPerusopetus)(h, oo)
          case _ => HttpStatus.ok
        }
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

