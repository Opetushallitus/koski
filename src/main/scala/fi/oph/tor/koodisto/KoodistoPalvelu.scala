package fi.oph.tor.koodisto

import fi.oph.tor.schema.KoodistoKoodiViite
import fi.vm.sade.utils.slf4j.Logging

class KoodistoPalvelu(lowLevelKoodistoPalvelu: LowLevelKoodistoPalvelu) extends Logging {
  def getKoodistoKoodiViitteet(koodisto: KoodistoViite): Option[List[KoodistoKoodiViite]] = {
    lowLevelKoodistoPalvelu.getKoodistoKoodit(koodisto).map { _.map { koodi => KoodistoKoodiViite(koodi.koodiArvo, koodi.nimi("fi"), koodisto.koodistoUri, Some(koodisto.versio))} }
  }
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite] = lowLevelKoodistoPalvelu.getLatestVersion(koodistoUri)
  def validate(input: KoodistoKoodiViite):Option[KoodistoKoodiViite] = {
    val koodistoViite = input.koodistoViite.orElse(getLatestVersion(input.koodistoUri))

    val viite = koodistoViite.flatMap(getKoodistoKoodiViitteet).toList.flatten.find(_.koodiarvo == input.koodiarvo)

    if (!viite.isDefined) {
      logger.warn("Koodia " + input.koodiarvo + " ei löydy koodistosta " + input.koodistoUri)
    }
    viite
  }
}