package fi.oph.koski.massaluovutus.suorituspalvelu

import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus

object SupaUtils {
  def isValmistunut(oo: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    oo.tila
      .opiskeluoikeusjaksot
      .lastOption
      .exists(_.tila.koodiarvo == "valmistunut")
}
