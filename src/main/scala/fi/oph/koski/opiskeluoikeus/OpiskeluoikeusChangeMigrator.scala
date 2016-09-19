package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, PäätasonSuoritus}

object OpiskeluoikeusChangeMigrator {
  def kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    val puuttuvatSuorituksetUudessa = vanhaOpiskeluoikeus.suoritukset.filter { vanhaSuoritus =>
      vanhaSuoritus.valmis && !uusiOpiskeluoikeus.suoritukset.exists(_.koulutusmoduuli.tunniste == vanhaSuoritus.koulutusmoduuli.tunniste)
    }
    uusiOpiskeluoikeus.withSuoritukset(puuttuvatSuorituksetUudessa ++ uusiOpiskeluoikeus.suoritukset)
  }
}
