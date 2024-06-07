package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.schema._


object SureTuvaOpiskeluoikeus {
  def apply(oo: TutkintokoulutukseenValmentavanOpiskeluoikeus): SureOpiskeluoikeus =
    SureOpiskeluoikeus(
      oo,
      suoritukset = oo.suoritukset.collect {
        case s: TutkintokoulutukseenValmentavanKoulutuksenSuoritus => SureDefaultPäätasonSuoritus(s)
      }
    )
}
