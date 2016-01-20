package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.log.TimedProxy

object KoodistoPalvelu {
  def apply(config: Config) = {
    KoodistoCacheWarmer(TimedProxy(withoutCache(config)))
  }

  def withoutCache(config: Config): KoodistoPalvelu = {
    if (config.hasPath("koodisto.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("koodisto.virkailija.url"))
    } else if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"))
    } else {
      MockKoodistoPalvelu
    }
  }
}

trait KoodistoPalvelu {
  def removeKoodistoRyhmä(toInt: Int)
  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi)
  def createKoodisto(koodisto: Koodisto)
  def createKoodistoRyhmä(ryhmä: KoodistoRyhmä)
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]]
  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto]
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite]
}