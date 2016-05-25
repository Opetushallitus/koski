package fi.oph.koski.koodisto

import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Koodistokoodiviite

case class KoodistoViitePalvelu(koodistoPalvelu: KoodistoPalvelu) extends Logging {
  def getKoodistoKoodiViitteet(koodisto: KoodistoViite): Option[List[Koodistokoodiviite]] = {
    koodistoPalvelu.getKoodistoKoodit(koodisto).map { _.map { koodi => Koodistokoodiviite(koodi.koodiArvo, koodi.nimi, koodi.lyhytNimi, koodisto.koodistoUri, Some(koodisto.versio))} }
  }
  def getLatestVersion(koodistoUri: String): Option[KoodistoViite] = koodistoPalvelu.getLatestVersion(koodistoUri)

  def getKoodistoKoodiViite(koodistoUri: String, koodiArvo: String): Option[Koodistokoodiviite] = getLatestVersion(koodistoUri).flatMap(koodisto => getKoodistoKoodiViitteet(koodisto).toList.flatten.find(_.koodiarvo == koodiArvo))

  def validate(input: Koodistokoodiviite):Option[Koodistokoodiviite] = {
    def toKoodistoViite(koodiviite: Koodistokoodiviite) = koodiviite.koodistoVersio.map(KoodistoViite(koodiviite.koodistoUri, _))

    val koodistoViite = toKoodistoViite(input).orElse(getLatestVersion(input.koodistoUri))

    val viite = koodistoViite.flatMap(getKoodistoKoodiViitteet).toList.flatten.find(_.koodiarvo == input.koodiarvo)

    if (!viite.isDefined) {
      logger.warn("Koodia " + input.koodiarvo + " ei löydy koodistosta " + input.koodistoUri)
    }
    viite
  }
}