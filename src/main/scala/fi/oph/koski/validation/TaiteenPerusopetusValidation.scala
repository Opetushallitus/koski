package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.suostumus.SuostumuksenPeruutusService

object TaiteenPerusopetusValidation {

  def validateOpiskeluoikeus(config: Config)(
    oo: KoskeenTallennettavaOpiskeluoikeus,
    suostumuksenPeruutusService: SuostumuksenPeruutusService
  ): HttpStatus = {
    oo match {
      case oo: TaiteenPerusopetuksenOpiskeluoikeus =>
        HttpStatus.fold(
          validateOpintotasot(oo),
          validateTaiteenalat(oo),
          validateHyväksytystiSuoritettuPäätasonSuoritus(oo),
          validateSuoritustenLaajuus(oo),
          validateSuostumusPeruttuSuoritukselta(oo, suostumuksenPeruutusService),
        )
      case _ => HttpStatus.ok
    }
  }

  def validateOpintotasot(oo: TaiteenPerusopetuksenOpiskeluoikeus): HttpStatus = {
    val yleisenOppimääränOpintotasojenSuoritustyypit = List(
      "taiteenperusopetuksenyleisenoppimaaranyhteisetopinnot",
      "taiteenperusopetuksenyleisenoppimaaranteemaopinnot"
    )
    val laajanOppimääränOpintotasojenSuoritustyypit = List(
      "taiteenperusopetuksenlaajanoppimaaranperusopinnot",
      "taiteenperusopetuksenlaajanoppimaaransyventavatopinnot"
    )

    oo.oppimäärä.koodiarvo match {
      case "yleinenoppimaara" => HttpStatus.validate(
        oo.suoritukset.map(_.tyyppi.koodiarvo).forall(t => yleisenOppimääränOpintotasojenSuoritustyypit.contains(t))
      )(
        KoskiErrorCategory.badRequest.validation.rakenne.tpoVääräOpintotaso()
      )
      case "laajaoppimaara" => HttpStatus.validate(
        oo.suoritukset.map(_.tyyppi.koodiarvo).forall(t => laajanOppimääränOpintotasojenSuoritustyypit.contains(t))
      )(
        KoskiErrorCategory.badRequest.validation.rakenne.tpoVääräOpintotaso()
      )
      case _ => KoskiErrorCategory.badRequest.validation.rakenne.tpoVääräOpintotaso("Opiskeluoikeuden oppimäärä on tuntematon.")
    }
  }

  def validateTaiteenalat(oo: TaiteenPerusopetuksenOpiskeluoikeus): HttpStatus = {
    HttpStatus.validate(
      oo.suoritukset.map(_.koulutusmoduuli.taiteenala.koodiarvo).distinct.size == 1
    )(
      KoskiErrorCategory.badRequest.validation.rakenne.tpoEriTaiteenalat()
    )
  }

  def validateHyväksytystiSuoritettuPäätasonSuoritus(oo: TaiteenPerusopetuksenOpiskeluoikeus): HttpStatus = {
    def valmiitOppimäärätLöytyvät: Boolean = oo.suoritukset.size == 2 && oo.suoritukset.forall(s => s.valmis)

    if (oo.tila.opiskeluoikeusjaksot.last.hyväksytystiSuoritettu && !valmiitOppimäärätLöytyvät) {
      KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu("Suoritukselta puuttuu vahvistus, vaikka opiskeluoikeus on tilassa hyväksytysti suoritettu")
    } else {
      HttpStatus.ok
    }
  }

  def validateSuoritustenLaajuus(oo: TaiteenPerusopetuksenOpiskeluoikeus): HttpStatus = {
    def laajuusVähintään(min: Double, k: Koulutusmoduuli, virheIlmoitus: HttpStatus): HttpStatus =
      HttpStatus.validate(min - 0.0001 < k.laajuusArvo(default = 0.0)) {
        virheIlmoitus
      }

    // Jos päivität näitä arvoja, päivitä myös tpoCommon.ts -> minimilaajuudet
    HttpStatus.fold(
      oo.suoritukset.filter(_.vahvistettu).map {
        case s: TaiteenPerusopetuksenYleisenOppimääränYhteistenOpintojenSuoritus => laajuusVähintään(
          min = 11.1,
          k = s.koulutusmoduuli,
          KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus("Yleisen oppimäärän yhteisten opintojen laajuus on oltava vähintään 11.1 opintopistettä.")
        )
        case s: TaiteenPerusopetuksenYleisenOppimääränTeemaopintojenSuoritus => laajuusVähintään(
          min = 7.4,
          k = s.koulutusmoduuli,
          KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus("Yleisen oppimäärän teemaopintojen laajuus on oltava vähintään 7.4 opintopistettä.")
        )
        case s: TaiteenPerusopetuksenLaajanOppimääränPerusopintojenSuoritus => laajuusVähintään(
          min = 29.6,
          k = s.koulutusmoduuli,
          KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus("Laajan oppimäärän perusopintojen laajuus on oltava vähintään 29.6 opintopistettä.")
        )
        case s: TaiteenPerusopetuksenLaajanOppimääränSyventävienOpintojenSuoritus => laajuusVähintään(
          min = 18.5,
          k = s.koulutusmoduuli,
          KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus("Laajan oppimäärän syventävien opintojen laajuus on oltava vähintään 18.5 opintopistettä.")
        )
      }
    )
  }

  def validateSuostumusPeruttuSuoritukselta(
    oo: TaiteenPerusopetuksenOpiskeluoikeus,
    suostumuksenPeruutusService: SuostumuksenPeruutusService
  ): HttpStatus = {
    val onPoistettuSuoritustyyppi = oo.oid match {
      case Some(oid) => suostumuksenPeruutusService.etsiPoistetut(Seq(oid)).exists { poistettuOo =>
        val suoritusTyypit = oo.suoritukset.map(_.tyyppi.koodiarvo)
        poistettuOo.suoritustyypit.exists(poistettuSuoritusTyyppi => suoritusTyypit.contains(poistettuSuoritusTyyppi))
      }
      case None => false
    }

    HttpStatus.validate(!onPoistettuSuoritustyyppi){
      KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
    }
  }

  def validateHankintakoulutusEiMuuttunut(
    oldState: KoskeenTallennettavaOpiskeluoikeus,
    newState: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    (oldState, newState) match {
      case (oldOo: TaiteenPerusopetuksenOpiskeluoikeus, newOo: TaiteenPerusopetuksenOpiskeluoikeus) =>
        HttpStatus.validate(oldOo.koulutuksenToteutustapa.koodiarvo == newOo.koulutuksenToteutustapa.koodiarvo) {
          KoskiErrorCategory.badRequest(
            "Koulutuksen toteutustapaa ei voi muuttaa opiskeluoikeuden luonnin jälkeen"
          )
        }
      case _ => HttpStatus.ok
    }
  }
}
