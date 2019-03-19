package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.schema._

object OpiskeluoikeusChangeMigrator {
  def kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    val puuttuvatSuorituksetUudessa = vanhaOpiskeluoikeus.suoritukset
      .filter(kopioitavaPäätasonSuoritus)
      .filter { vanhaSuoritus =>
        vanhaSuoritus.valmis && !uusiOpiskeluoikeus.suoritukset.exists(_.koulutusmoduuli.tunniste == vanhaSuoritus.koulutusmoduuli.tunniste)
      }
    uusiOpiskeluoikeus.withSuoritukset(puuttuvatSuorituksetUudessa ++ uusiOpiskeluoikeus.suoritukset)
  }

  private def kopioitavaPäätasonSuoritus(suoritus: KoskeenTallennettavaPäätasonSuoritus) = suoritus match {
    case _: LukionOppiaineenOppimääränSuoritus |
         _: NuortenPerusopetuksenOppiaineenOppimääränSuoritus |
         _: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus => false
    case _ => true
  }
}
