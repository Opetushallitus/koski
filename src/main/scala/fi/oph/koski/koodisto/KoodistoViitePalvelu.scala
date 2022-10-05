package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.koski.cache._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.servlet.InvalidRequestException

import scala.concurrent.duration.DurationInt

case class KoodistoViitePalvelu(config: Config, koodistoPalvelu: KoodistoPalvelu)(implicit cacheInvalidator: CacheManager) extends Logging {
  private val koodiviiteCache = KeyValueCache(RefreshingCache("KoodistoViitePalvelu", 1.hour, 100), { koodisto: KoodistoViite =>
    val koodit: List[KoodistoKoodi] = koodistoPalvelu.getKoodistoKoodit(koodisto)
    koodit.map(toKoodiviite(koodisto))
  })

  def getKoodistoKoodiViitteet(koodisto: KoodistoViite): List[Koodistokoodiviite] = {
    koodiviiteCache(koodisto)
  }

  def getSisältyvätKoodiViitteet(koodisto: KoodistoViite, parentViite: Koodistokoodiviite): Option[List[Koodistokoodiviite]] = {
    for {
      parentKoodisto <- toKoodistoViiteOptional(parentViite)
      parent <- koodistoPalvelu.getKoodistoKoodit(parentKoodisto).find(_.koodiArvo == parentViite.koodiarvo)
      koodit: List[KoodistoKoodi] <- Some(koodistoPalvelu.getKoodistoKoodit(koodisto))
    } yield {
      koodit.filter(_.hasParent(parent)).map(toKoodiviite(koodisto))
    }
  }

  def getLatestVersionRequired(koodistoUri: String): KoodistoViite = koodistoPalvelu.getLatestVersionRequired(koodistoUri)

  def getLatestVersionOptional(koodistoUri: String): Option[KoodistoViite] = koodistoPalvelu.getLatestVersionOptional(koodistoUri)

  def validate(koodistoUri: String, koodiArvo: String): Option[Koodistokoodiviite] = {
    validate(Koodistokoodiviite(koodiArvo, koodistoUri))
  }

  def onKoodistossa(koodistoUri: String, koodiArvo: String): Boolean = {
    onKoodistossa(Koodistokoodiviite(koodiArvo, koodistoUri))
  }

  def validate(input: Koodistokoodiviite): Option[Koodistokoodiviite] = {
    val viite = toKoodistokoodiviiteOptional(input)

    if (viite.isEmpty) {
      logger.warn("Koodia " + input.koodiarvo + " ei löydy koodistosta " + input.koodistoUri)
    }
    viite
  }

  def onKoodistossa(input: Koodistokoodiviite): Boolean = {
    toKoodistokoodiviiteOptional(input).isDefined
  }

  private def toKoodistokoodiviiteOptional(input: Koodistokoodiviite): Option[Koodistokoodiviite] = {
    val koodistoViite = toKoodistoViiteOptional(input)
    koodistoViite.flatMap(getKoodistoKoodiViitteet(_).find(_.koodiarvo == input.koodiarvo))
  }

  private def toKoodistoViiteOptional(koodiviite: Koodistokoodiviite): Option[KoodistoViite] = koodiviite
    .koodistoVersio
    .map(KoodistoViite(koodiviite.koodistoUri, _))
    .orElse(getLatestVersionOptional(koodiviite.koodistoUri))

  def validateRequired(uri: String, koodi: String): Koodistokoodiviite = {
    validateRequired(Koodistokoodiviite(koodi, uri))
  }

  def validateRequired(input: Koodistokoodiviite): Koodistokoodiviite = {
    validate(input).getOrElse(throw new InvalidRequestException(KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi("Koodia ei löydy koodistosta: " + input)))
  }

  private def toKoodiviite(koodisto: KoodistoViite)(koodi: KoodistoKoodi): Koodistokoodiviite =
    Koodistokoodiviite(koodi.koodiArvo, koodi.nimi, koodi.lyhytNimi, koodisto.koodistoUri, Some(koodisto.versio))
}

object MockKoodistoViitePalvelu extends KoodistoViitePalvelu(KoskiApplication.defaultConfig, MockKoodistoPalvelu())(GlobalCacheManager) {
  override def validate(input: Koodistokoodiviite) = super.validate(input).map(_.copy(koodistoVersio = None))
  override def validate(koodistoUri: String, koodiArvo: String) = super.validate(koodistoUri, koodiArvo).map(_.copy(koodistoVersio = None))
  override def getKoodistoKoodiViitteet(koodisto: KoodistoViite) = super.getKoodistoKoodiViitteet(koodisto).map(_.copy(koodistoVersio = None))
}
