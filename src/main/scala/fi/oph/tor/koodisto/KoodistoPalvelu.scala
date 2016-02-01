package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.log.TimedProxy

object KoodistoPalvelu {
  def apply(config: Config) = {
    TimedProxy(withoutCache(config))
  }

  def withoutCache(config: Config): KoodistoPalvelu = {
    if (config.hasPath("koodisto.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("authentication-service.username"))
    } else if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("authentication-service.username"))
    } else {
      MockKoodistoPalvelu
    }
  }
}

trait KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]]
  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto]
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite]
}