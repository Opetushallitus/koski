package fi.oph.koski.localization

import com.typesafe.config.Config
import fi.oph.koski.cache.{Cache, CacheManager, KeyValueCache}
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, VirkailijaHttpClient}
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.localization.LocalizedString.sanitize
import fi.oph.koski.log.Logging
import org.json4s._

trait LocalizationRepository extends Logging {

  def localizations(): Map[String, LocalizedString]

  def get(key: String) = localizations.get(key).getOrElse{
    logger.error(s"Unknown localization key: $key")
    LocalizedString.unlocalized(key)
  }

  def fetchLocalizations(): JValue

  def createOrUpdate(localizations: List[UpdateLocalization])

  def createMissing(): Unit = {
    val inLocalizationService = localizationsFromLocalizationService

    val missing = DefaultLocalizations.defaultFinnishTexts.flatMap {
      case (key, defaultText) => inLocalizationService.get(key) match {
        case Some(_) => None
        case None => Some(List(UpdateLocalization("fi", key, defaultText), UpdateLocalization("sv", key, ""), UpdateLocalization("en", key, "")))
      }
    }.toList.flatten

    logger.info("Creating " + missing.length + " missing localizations: " + Json.write(missing))

    if (missing.nonEmpty) {
      try {
        createOrUpdate(missing)
      } catch {
        case e: Exception => logger.warn(e)("Failed to create missing localizations")
      }
    }

    logger.info("done.")
  }

  def localizationsFromLocalizationService: Map[String, Map[String, String]] = fetchLocalizations().extract[List[LocalizationServiceLocalization]]
    .groupBy(_.key)
    .mapValues(_.map(v => (v.locale, v.value)).toMap)
}

object DefaultLocalizations {
  lazy val defaultFinnishTexts: Map[String, String] = readResource("/localization/default-texts.json").extract[Map[String, String]]
}

abstract class CachedLocalizationService(implicit cacheInvalidator: CacheManager) extends LocalizationRepository {
  protected val cache = KeyValueCache[String, Map[String, LocalizedString]](
    Cache.cacheAllRefresh("LocalizationRepository.localizations", durationSeconds = 60, maxSize = 1),
    key => fetch()
  )

  def localizations(): Map[String, LocalizedString] = {
    cache("key")
  }

  private def fetch(): Map[String, LocalizedString] = {
    val inLocalizationService = localizationsFromLocalizationService

    DefaultLocalizations.defaultFinnishTexts.map {
      case (key, finnishDefaultText) =>
        inLocalizationService.get(key).map(l => (key, sanitize(l).get)).getOrElse {
          logger.info(s"Localizations missing for key $key")
          (key, Finnish(finnishDefaultText))
        }
    }
  }
}

object LocalizationRepository {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager): LocalizationRepository = {
    config.getString("localization.url") match {
      case "mock" =>
        new MockLocalizationRepository
      case url: Any =>
        new RemoteLocalizationRepository(VirkailijaHttpClient(config.getString("opintopolku.virkailija.username"), config.getString("opintopolku.virkailija.password"), config.getString("localization.url"), "/lokalisointi"))
    }
  }
}

class MockLocalizationRepository(implicit cacheInvalidator: CacheManager) extends CachedLocalizationService {

  private var _localizations: Map[String, LocalizedString] = super.localizations()

  override def localizations(): Map[String, LocalizedString] = {
    _localizations
  }

  override def fetchLocalizations(): JValue = readResource("/mockdata/lokalisointi/koski.json")

  override def createOrUpdate(toUpdate: List[UpdateLocalization]): Unit = {
    _localizations = toUpdate.foldLeft(_localizations) { (acc, n) =>
      if (acc.contains(n.key)) {
        acc + (n.key -> sanitize(acc(n.key).values + (n.locale -> n.value)).get)
      } else {
        acc + (n.key -> sanitize(Map(n.locale -> n.value)).get)
      }
    }
  }
  def reset = {
    _localizations = super.localizations
  }
}

class RemoteLocalizationRepository(http: Http)(implicit cacheInvalidator: CacheManager) extends CachedLocalizationService {
  override def fetchLocalizations(): JValue = runTask(http.get(uri"/lokalisointi/cxf/rest/v1/localisation?category=koski")(Http.parseJson[JValue]))

  override def createOrUpdate(localizations: List[UpdateLocalization]): Unit = {
    cache.strategy.invalidateCache()
    runTask(http.post(uri"/lokalisointi/cxf/rest/v1/localisation/update", localizations)(json4sEncoderOf[List[UpdateLocalization]])(Http.unitDecoder))
  }
}

case class UpdateLocalization(
  locale: String,
  key: String,
  value: String,
  category: String = "koski"
)

case class LocalizationServiceLocalization(
  id: Integer,
  locale: String,
  category: String,
  key: String,
  value: String
)


