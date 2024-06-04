package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus

object SureUtils {
  def isValmistunut(oo: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    oo.tila
      .opiskeluoikeusjaksot
      .lastOption
      .exists(_.tila.koodiarvo == "valmistunut")
}
