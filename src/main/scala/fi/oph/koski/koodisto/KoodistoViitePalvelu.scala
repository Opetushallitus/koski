package fi.oph.koski.koodisto

import fi.oph.koski.cache.{CacheManager, GlobalCacheManager, KeyValueCache, KoskiCache}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Koodistokoodiviite

case class KoodistoViitePalvelu(koodistoPalvelu: KoodistoPalvelu)(implicit cacheInvalidator: CacheManager) extends Logging {
  private val koodiviiteCache = KeyValueCache(KoskiCache.cacheStrategy("KoodistoViitePalvelu"), { koodisto: KoodistoViite =>
    val koodit: Option[List[KoodistoKoodi]] = koodistoPalvelu.getKoodistoKoodit(koodisto)
    koodit.map { _.map { koodi => Koodistokoodiviite(koodi.koodiArvo, koodi.nimi, koodi.lyhytNimi, koodisto.koodistoUri, Some(koodisto.versio))} }
  })

  def getKoodistoKoodiViitteet(koodisto: KoodistoViite): Option[List[Koodistokoodiviite]] = {
    koodiviiteCache(koodisto)
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

object MockKoodistoViitePalvelu extends KoodistoViitePalvelu(MockKoodistoPalvelu())(GlobalCacheManager) {
  override def validate(input: Koodistokoodiviite) = super.validate(input).map(_.copy(koodistoVersio = None))
  override def getKoodistoKoodiViite(koodistoUri: String, koodiArvo: String) = super.getKoodistoKoodiViite(koodistoUri, koodiArvo).map(_.copy(koodistoVersio = None))
  override def getKoodistoKoodiViitteet(koodisto: KoodistoViite) = super.getKoodistoKoodiViitteet(koodisto).map(_.map(_.copy(koodistoVersio = None)))
}