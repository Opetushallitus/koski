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
          validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin(config, poo),
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

  def sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(oo: Opiskeluoikeus): Boolean = {
    oo match {
      case poo: PerusopetuksenOpiskeluoikeus
      => poo.suoritukset.map(_.tyyppi.koodiarvo).exists(Set("perusopetuksenoppimaara", "perusopetuksenvuosiluokka").contains)
      case _
      => false
    }
  }

  def sisältääAikuistenPerusopetuksenOppimääränSuorituksen(oo: Opiskeluoikeus): Boolean = {
    oo match {
      case aipe: AikuistenPerusopetuksenOpiskeluoikeus
      => aipe.suoritukset.map(_.tyyppi.koodiarvo).exists(Set("aikuistenperusopetuksenoppimaara").contains)
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

  private def validateVanhojenJaksokenttienPäättyminenSiirryttäessäUusiin(config: Config, oo: PerusopetuksenOpiskeluoikeus): HttpStatus = {
    val rajapäivä = LocalDate.parse(config.getString("validaatiot.varhennettuOppivelvollisuusVoimaan"))

    val validaatioVoimassa = if (Environment.isServerEnvironment(config)) {
      LocalDate.now().isEqualOrAfter(rajapäivä)
    } else {
      true
    }

    if (validaatioVoimassa) {
      val alkanutEnnenRajapäivää = oo.alkamispäivä.exists(_.isBefore(rajapäivä))
      val jatkuuRajapäivänJälkeen = oo.päättymispäivä.exists(_.isEqualOrAfter(rajapäivä)) || oo.päättymispäivä.isEmpty

      if (alkanutEnnenRajapäivää && jatkuuRajapäivänJälkeen) {
        lazy val viimeinenPvmStr = FinnishDateFormat.format(rajapäivä.minusDays(1))
        val oppivelvollisuudenPidennys = oo.lisätiedot.flatMap(_.pidennettyOppivelvollisuus)
        val viimeisinVammaisuusjakso = oo.lisätiedot.toList.flatMap(_.vammainen.toList.flatten).sortBy(_.alku).lastOption
        val viimeisinVaikeaVammaisuusjakso = oo.lisätiedot.toList.flatMap(_.vaikeastiVammainen.toList.flatten).sortBy(_.alku).lastOption

        HttpStatus.fold(
          HttpStatus.validateNot(oppivelvollisuudenPidennys.exists(_.contains(rajapäivä)))(
            KoskiErrorCategory.badRequest.validation.date.pidennettyOppivelvollisuus(s"Pidennetyn oppivelvollisuuden viimeinen mahdollinen päättymispäivä on $viimeinenPvmStr. Merkitse kyseisen päivän jälkeiset jaksot 'opetuksen järjestämiseen vamman, sairauden tai rajoitteen perusteella' tai 'opiskelee toiminta-alueittain'.")
          ),
          HttpStatus.validateNot(viimeisinVammaisuusjakso.exists(_.contains(rajapäivä)))(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vammaisuuden jakson viimeinen mahdollinen päättymispäivä on $viimeinenPvmStr.")
          ),
          HttpStatus.validateNot(viimeisinVaikeaVammaisuusjakso.exists(_.contains(rajapäivä)))(
            KoskiErrorCategory.badRequest.validation.date.vammaisuusjakso(s"Vaikeasti vammaisuuden jakson viimeinen mahdollinen päättymispäivä on $viimeinenPvmStr.")
          ),
        )
      } else {
        HttpStatus.ok
      }
    } else {
      HttpStatus.ok
    }
  }
}

