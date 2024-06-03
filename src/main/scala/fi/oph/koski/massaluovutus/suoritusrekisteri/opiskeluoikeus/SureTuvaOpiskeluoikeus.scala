package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureOpiskeluoikeus
import fi.oph.koski.schema._


object SureTuvaOpiskeluoikeus {
  def apply(oo: TutkintokoulutukseenValmentavanOpiskeluoikeus): SureOpiskeluoikeus =
    SureDefaultOpiskeluoikeus(
      oo,
      suoritukset = oo.suoritukset.collect {
        case s: TutkintokoulutukseenValmentavanKoulutuksenSuoritus => SureDefaultPäätasonSuoritus(s)
      }
    )
}
