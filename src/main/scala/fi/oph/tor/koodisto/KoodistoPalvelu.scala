package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.schema.KoodistoKoodiViite
import fi.oph.tor.util.{CachingProxy, TimedProxy}
import fi.vm.sade.utils.slf4j.Logging

trait KoodistoPalvelu extends Logging {
  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi)
  def createKoodisto(koodisto: Koodisto)
  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]]
  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto]
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite]
  def validate(input: KoodistoKoodiViite):Option[KoodistoKoodiViite] = {
    val koodistoViite = input.koodistoViite.orElse(getLatestVersion(input.koodistoUri))

    val viite = koodistoViite.flatMap { koodisto => getKoodistoKoodit(koodisto).flatMap { koodit =>
      koodit.find(_.koodiArvo == input.koodiarvo).map {
        koodi => KoodistoKoodiViite(koodi.koodiArvo, koodi.nimi("fi"), input.koodistoUri, Some(koodisto.versio))
      }
    }}

    if (!viite.isDefined) {
      logger.warn("Koodia " + input.koodiarvo + " ei löydy koodistosta " + input.koodistoUri)
    }
    viite
  }
}

object KoodistoPalvelu extends Logging {
  def apply(config: Config) = {
    CachingProxy(config, TimedProxy(withoutCache(config)))
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