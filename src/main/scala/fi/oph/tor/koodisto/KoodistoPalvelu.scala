package fi.oph.tor.koodisto

import com.typesafe.config.Config

trait KoodistoPalvelu {
  def getKoodisto(koodisto: KoodistoViittaus): Option[List[KoodistoKoodi]]
}

object KoodistoPalvelu {
  def apply(config: Config) = {
    if (config.hasPath("koodisto.url")) {
      new RemoteKoodistoPalvelu(config.getString("koodisto.url"))
    }
    else if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("opintopolku.virkailija.url") + "/koodisto-service")
    } else {
      new MockKoodistoPalvelu
    }
  }
}