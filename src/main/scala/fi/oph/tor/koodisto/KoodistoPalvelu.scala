package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.schema.KoodistoKoodiViite
import fi.oph.tor.util.{CachingProxy, TimedProxy}
import fi.vm.sade.utils.slf4j.Logging

trait KoodistoPalvelu {
  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi)
  def createKoodisto(koodisto: Koodisto)
  def getKoodistoKoodit(koodisto: KoodistoViittaus): Option[List[KoodistoKoodi]]
  def getKoodisto(koodisto: KoodistoViittaus): Option[Koodisto]
  def getAlakoodit(koodiarvo: String): List[Alakoodi]
  def getLatestVersion(koodisto: String): Option[Int]
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

  def validate(palvelu: KoodistoPalvelu, viite: KoodistoKoodiViite):Option[KoodistoKoodiViite] = {
    getKoodistoKoodiViite(palvelu, viite.koodistoUri, viite.koodiarvo, viite.koodistoVersio)
  }

  def koodisto(palvelu: KoodistoPalvelu, viite: KoodistoKoodiViite): Option[KoodistoViittaus] = {
    viite.koodistoVersio.orElse(palvelu.getLatestVersion(viite.koodistoUri)).map { versio =>
      KoodistoViittaus(viite.koodistoUri, versio)
    }
  }

  def getKoodistoKoodiViite(palvelu: KoodistoPalvelu, koodistoUri: String, koodiarvo: String, koodistoVersio: Option[Int] = None): Option[KoodistoKoodiViite] = {
    val versio = koodistoVersio.orElse(palvelu.getLatestVersion(koodistoUri))

    val viite = versio.flatMap { versio => palvelu.getKoodistoKoodit(KoodistoViittaus(koodistoUri, versio)).flatMap { koodit =>
      koodit.find(_.koodiArvo == koodiarvo).map {
        koodi => KoodistoKoodiViite(koodi.koodiArvo, koodi.nimi("fi"), koodistoUri, Some(versio))
      }
    }}

    if (!viite.isDefined) {
      logger.warn("Koodia " + koodiarvo + " ei löydy koodistosta " + koodistoUri)
    }
    viite
  }
}