package fi.oph.koski.massaluovutus.suoritusrekisteri.opiskeluoikeus

import fi.oph.koski.massaluovutus.suoritusrekisteri.SureOpiskeluoikeus
import fi.oph.koski.schema._

object SureAmmatillinenTutkinto {
  def apply(oo: AmmatillinenOpiskeluoikeus): SureOpiskeluoikeus =
    SureDefaultOpiskeluoikeus(
      oo,
      suoritukset = oo.suoritukset.collect {
        case s: AmmatillisenTutkinnonSuoritus => SureDefaultPäätasonSuoritus(s, None)
        case s: TelmaKoulutuksenSuoritus => SureDefaultPäätasonSuoritus(s)
      }
    )
}
