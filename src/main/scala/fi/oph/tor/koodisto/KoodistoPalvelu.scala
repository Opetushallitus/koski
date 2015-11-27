package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.schema.KoodistoKoodiViite
import fi.oph.tor.util.{CachingProxy, TimedProxy}
import fi.vm.sade.utils.slf4j.Logging

trait KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViittaus): Option[List[KoodistoKoodi]]
  def getKoodisto(koodisto: KoodistoViittaus): Option[Koodisto]
  def getAlakoodit(koodiarvo: String): List[Alakoodi]
  def getLatestVersion(koodisto: String): Int
}

object KoodistoPalvelu extends Logging {
  def apply(config: Config) = {
    CachingProxy(config, TimedProxy(if (config.hasPath("koodisto.url")) {
      new RemoteKoodistoPalvelu(config.getString("koodisto.url"))
    }
    else if (config.hasPath("opintopolku.virkailija.url")) {
      new RemoteKoodistoPalvelu(config.getString("opintopolku.virkailija.url") + "/koodisto-service")
    } else {
      new MockKoodistoPalvelu
    }))
  }

  def validate(palvelu: KoodistoPalvelu, viite: KoodistoKoodiViite):Option[KoodistoKoodiViite] = {
    getKoodistoKoodiViite(palvelu, viite.koodistoUri, viite.koodiarvo, viite.koodistoVersio)
  }

  def getKoodistoKoodiViite(palvelu: KoodistoPalvelu, koodistoUri: String, koodiarvo: String, koodistoVersio: Option[Int] = None): Option[KoodistoKoodiViite] = {
    val versio = koodistoVersio.getOrElse(palvelu.getLatestVersion(koodistoUri))
    val viite = palvelu.getKoodistoKoodit(KoodistoViittaus(koodistoUri, versio)).flatMap { koodit =>
      koodit.find(_.koodiArvo == koodiarvo).map { koodi => KoodistoKoodiViite(koodi.koodiArvo, koodi.nimi("fi"), koodistoUri, Some(versio))}
    }
    if (!viite.isDefined) {
      logger.warn("Koodia " + koodiarvo + " ei löydy koodistosta " + koodistoUri)
    }
    viite
  }
}