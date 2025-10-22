package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.documentation.PerusopetusExampleData.suoritustapaErityinenTutkinto
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.util.ChainingSyntax.localDateOps
import fi.oph.koski.util.DateOrdering.localDateOrdering
import fi.oph.koski.util.FinnishDateFormat
import fi.oph.koski.validation.PidennetynOppivelvollisuudenMuutoksenValidaatio.validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin

import java.time.LocalDate

object PerusopetuksenOpiskeluoikeusValidation extends Logging {
  def validatePerusopetuksenOpiskeluoikeus(config: Config)(
    oo: Opiskeluoikeus
  ): HttpStatus = {
    oo match {
      case poo: PerusopetuksenOpiskeluoikeus => HttpStatus.fold(
        List(
          validateNuortenPerusopetuksenOpiskeluoikeudenTila(poo),
          validateVuosiluokanAlkamispäivät(poo),
          validatePäätasonSuoritus(poo),
          validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin(config, poo.alkamispäivä, poo.päättymispäivä, poo.lisätiedot),
          validateTavoitekokonaisuuksittainOpiskeleva(config,poo),
          validateVuosiluokkiinSitoutumatonOpetusEiSallittu(config,poo)
        ) ++ poo.lisätiedot.toList.flatMap(lisätiedot => List(
          validateTuenJaksojenPäällekkäisyys(lisätiedot),
          validateOppivelvollisuudenPidennysjaksojenPäällekkäisyys(lisätiedot),
        ))
      )
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

  def tavoitekokonaisuuksittainOpiskeluVoimassa(oo: KoskeenTallennettavaOpiskeluoikeus, pvm: LocalDate): Boolean = oo match {
    case oo: PerusopetuksenOpiskeluoikeus =>
      oo.lisätiedot.flatMap(_.tavoitekokonaisuuksittainOpiskelu).getOrElse(Seq.empty).exists(_.contains(pvm))
  }

  def fillPerusopetuksenVuosiluokkiinSitoutumatonOpetus(config: Config)(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    val lastAllowed = LocalDate.parse(config.getString("validaatiot.VSOPKentänViimeinenKäyttöpäivä"))
    oo match {
      case p: PerusopetuksenOpiskeluoikeus =>
        val current = p.lisätiedot.flatMap(_.vuosiluokkiinSitoutumatonOpetus)
        val normalized: Option[Boolean] = (p.alkamispäivä, current) match {
          case (Some(d), Some(false)) if d.isAfter(lastAllowed) => None
          case _                                                => current
        }
        p.copy(lisätiedot = p.lisätiedot.map(_.copy(
          vuosiluokkiinSitoutumatonOpetus = normalized
        )))
      case other => other
    }
  }

  private def validateTavoitekokonaisuuksittainOpiskeleva(config: Config, oo: PerusopetuksenOpiskeluoikeus): HttpStatus = {
    val cutoff = LocalDate.parse(config.getString("validaatiot.VSOPKentänViimeinenKäyttöpäivä"))
    val vsopOn = oo.lisätiedot.flatMap(_.vuosiluokkiinSitoutumatonOpetus).contains(true)
    val errors: Seq[HttpStatus] = oo.suoritukset.collect {
      case vls: PerusopetuksenVuosiluokanSuoritus =>
        val vuosiluokka = vls.koulutusmoduuli.tunniste
        val vahvistuspäivä = vls.vahvistus.map(_.päivä)
        vls.osasuoritukset.getOrElse(Seq.empty).flatMap {
          case os: NuortenPerusopetuksenOppiaineenSuoritus =>
            val vuosiluokka = vls.koulutusmoduuli.tunniste
            os.luokkaAste match {
              case Some(la) =>
                val paivat = Seq(vahvistuspäivä, os.ensimmäinenArviointiPäivä).flatten
                val osuuJaksolle = paivat.exists(d => tavoitekokonaisuuksittainOpiskeluVoimassa(oo, d))
                if (vsopOn && paivat.exists(p => !p.isAfter(cutoff))) {
                  None
                } else if (!osuuJaksolle)
                {Some(KoskiErrorCategory.badRequest.validation.date(s"Perusopetuksen oppiaineen suorituksella on tavoitekokonaisuuksittain opiskeluun liittyvä tieto luokkaAste (${la.koodiarvo}) mutta ei tavoitekokonaisuuksittain opiskelun aikajaksoa, joka kattaisi vuosiluokan vahvistuspäivän tai suorituksen arviointipäivän."))
                } else if (la == vuosiluokka)
                {Some(KoskiErrorCategory.badRequest.validation.date(s"Perusopetuksen oppiaineen suorituksen tavoitekokonaisuuksittain opiskeluun liittyvä kenttä luokkaAste ei saa olla sama kuin vuosiluokka (${vuosiluokka.koodiarvo})"))
                } else {
                  None
                }
              case None => None
            }
          case _ => None
        }
    }.flatten
    HttpStatus.fold(errors)
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
    case p: PerusopetuksenOpiskeluoikeus =>
      p.lisätiedot.flatMap(_.vuosiluokkiinSitoutumatonOpetus).contains(true)
    case _ => false
  }

  def validateVuosiluokkiinSitoutumatonOpetusEiSallittu(config: Config, oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    val lastAllowed = LocalDate.parse(config.getString("validaatiot.VSOPKentänViimeinenKäyttöpäivä"))
    oo match {
      case p: PerusopetuksenOpiskeluoikeus =>
        if (
          p.alkamispäivä.exists(_.isAfter(lastAllowed)) && p.lisätiedot.flatMap(_.vuosiluokkiinSitoutumatonOpetus).isDefined
        ) {
          KoskiErrorCategory.badRequest.validation.rakenne.vsopVirheelliselläpäivämäärällä()
        }else{
          HttpStatus.ok
        }
      case _ => HttpStatus.ok
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

  def sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(oo: Opiskeluoikeus): Boolean = {
    oo match {
      case poo: PerusopetuksenOpiskeluoikeus
      => poo.suoritukset.map(_.tyyppi.koodiarvo).exists(Set("perusopetuksenoppimaara", "perusopetuksenvuosiluokka").contains)
      case _
      => false
    }
  }

  private def validateTuenJaksojenPäällekkäisyys(tiedot: PerusopetuksenOpiskeluoikeudenLisätiedot): HttpStatus = {
    val erityisenTuenPäätökset = tiedot.erityisenTuenPäätökset.toList.flatten
    val tuenPäätöksenJaksot = tiedot.tuenPäätöksenJaksot.toList.flatten

    if (erityisenTuenPäätökset.nonEmpty && tuenPäätöksenJaksot.nonEmpty) {
      HttpStatus.validateNot(MahdollisestiAlkupäivällinenJakso.overlap(erityisenTuenPäätökset, tuenPäätöksenJaksot))(
        KoskiErrorCategory.badRequest.validation.date.erityisenTuenPäätös(s"Erityisen tuen päätöksen jakso ja tuen päätöksen jakso eivät saa olla päällekkäin")
      )
    } else {
      HttpStatus.ok
    }
  }

  private def validateOppivelvollisuudenPidennysjaksojenPäällekkäisyys(tiedot: PerusopetuksenOpiskeluoikeudenLisätiedot): HttpStatus = {
    val pidennettyOppivelvollisuus = tiedot.pidennettyOppivelvollisuus.toList
    val jaksot = tiedot.opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella.toList.flatten

    if (pidennettyOppivelvollisuus.nonEmpty && jaksot.nonEmpty) {
      HttpStatus.validateNot(Aikajakso.overlap(pidennettyOppivelvollisuus, jaksot))(
        KoskiErrorCategory.badRequest.validation.date.erityisenTuenPäätös(s"Pidennetyn oppivelvollisuus ja opetuksen järjestäminen vamman, sairauden tai rajoitteen perusteella eivät saa olla ajallisesti päällekkäin")
      )
    } else {
      HttpStatus.ok
    }
  }
}

