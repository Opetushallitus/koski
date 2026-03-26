package fi.oph.koski.todistus

import com.typesafe.config.Config
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA

class TodistusFeatureFlags(config: Config) {
  val enabledForAll: Boolean = config.getBoolean("todistus.enabledForAll")
  val enabledForPääkäyttäjä: Boolean = config.getBoolean("todistus.enabledForPääkäyttäjä")

  val isServiceEnabled: Boolean = enabledForAll || enabledForPääkäyttäjä

  def isEnabledForUser(session: KoskiSpecificSession): Boolean =
    enabledForAll || (enabledForPääkäyttäjä && session.hasRole(OPHPAAKAYTTAJA))
}
