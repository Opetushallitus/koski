package fi.oph.koski.localization

import com.typesafe.config.Config
import fi.oph.koski.cache._
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{Http, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.json.{JsonResources, JsonSerializer}
import fi.oph.koski.localization.LocalizationRepository.parseLocalizations
import fi.oph.koski.localization.MockLocalizationRepository.readLocalLocalizations
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString.sanitize
import fi.oph.koski.schema.{Finnish, LocalizedString}
import org.json4s._

import scala.collection.immutable
import scala.concurrent.duration.DurationInt

trait LocalizationRepository extends Logging {
  def localizationConfig: LocalizationConfig

  def localizations: Map[String, LocalizedString]

  def get(key: String): LocalizedString = localizations.getOrElse(key, {
    logger.error(s"Unknown localization key: $key in category ${localizationConfig.localizationCategory} and default file ${localizationConfig.defaultFinnishTextsResourceFilename}")
    LocalizedString.unlocalized(key)
  })

  def fetchLocalizations(): JValue

  def createOrUpdate(localizations: List[UpdateLocalization])

  def localizationsFromLocalizationService: Map[String, Map[String, String]] = parseLocalizations(fetchLocalizations())

  def init
}

class DefaultLocalizations(resourceFilename: String) {
  val defaultFinnishTexts: Map[String, String] = extract[Map[String, String]](JsonResources.readResource(resourceFilename))
}

abstract class CachedLocalizationService(localizationConfig: LocalizationConfig)(implicit cacheInvalidator: CacheManager) extends LocalizationRepository {
  protected val cache = KeyValueCache[String, Map[String, LocalizedString]](
    new RefreshingCache(s"LocalizationRepository.${localizationConfig.localizationCategory}Localizations", RefreshingCache.Params(60.seconds, 1)),
    key => fetch()
  )

  def localizations(): Map[String, LocalizedString] = {
    cache("key")
  }

  private def fetch(): Map[String, LocalizedString] = {
    val inLocalizationService = localizationsFromLocalizationService

    defaultLocalizations.defaultFinnishTexts.map {
      case (key, finnishDefaultText) =>
        inLocalizationService.get(key).map(l => (key, sanitize(l).get)).getOrElse {
          reportMissingLocalization(key)
          (key, Finnish(finnishDefaultText))
        }
    }
  }

  def reportMissingLocalization(key: String): Unit = {
    logger.warn(s"Localizations missing for key $key")
  }

  protected lazy val defaultLocalizations = new DefaultLocalizations(localizationConfig.defaultFinnishTextsResourceFilename)
}

object LocalizationRepository {
  def apply(config: Config, localizationConfig: LocalizationConfig)(implicit cacheInvalidator: CacheManager): LocalizationRepository = {
    config.getString("localization.url") match {
      case "mock" =>
        new MockLocalizationRepository(localizationConfig)
      case url: Any =>
        new RemoteLocalizationRepository(config, localizationConfig)
    }
  }
  def parseLocalizations(json: JValue) = extract[List[LocalizationServiceLocalization]](json, ignoreExtras = true)
    .groupBy(_.key)
    .mapValues(_.map(v => (v.locale, v.value)).toMap)
}

case class MockLocalizationRepository(localizationConfig: LocalizationConfig)(implicit cacheInvalidator: CacheManager) extends CachedLocalizationService(localizationConfig) {

  private var _localizations: Map[String, LocalizedString] = super.localizations()

  override def localizations(): Map[String, LocalizedString] = {
    _localizations
  }

  override def fetchLocalizations(): JValue = readLocalLocalizations(localizationConfig.mockLocalizationResourceFilename)

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

  def init {}

  override def reportMissingLocalization(key: String): Unit = {
    // Don't report missing localizations in MockLocalizationRepository, i.e. entries missing from the localizationConfig.mockLocalizationResourceFilename
    // file.
  }
}

object MockLocalizationRepository {
  def readLocalLocalizations(resourceName: String) = JsonResources.readResource(resourceName)
}

class ReadOnlyRemoteLocalizationRepository(virkalijaRoot: String, val localizationConfig: LocalizationConfig)(implicit cacheInvalidator: CacheManager) extends CachedLocalizationService(localizationConfig) {
  private val http = Http(virkalijaRoot, "lokalisaatiopalvelu")
  override def fetchLocalizations(): JValue = runIO(http.get(uri"/lokalisointi/cxf/rest/v1/localisation?category=${localizationConfig.localizationCategory}")(Http.parseJson[JValue]))
  override def createOrUpdate(localizations: List[UpdateLocalization]): Unit = ???
  def init {}
}

class RemoteLocalizationRepository(config: Config, val localizationConfig: LocalizationConfig)(implicit cacheInvalidator: CacheManager) extends CachedLocalizationService(localizationConfig) {
  private val http = VirkailijaHttpClient(ServiceConfig.apply(config, "localization", "opintopolku.virkailija"), "/lokalisointi", false)

  override def fetchLocalizations(): JValue = runIO(http.get(uri"/lokalisointi/cxf/rest/v1/localisation?category=${localizationConfig.localizationCategory}")(Http.parseJson[JValue]))

  override def createOrUpdate(localizations: List[UpdateLocalization]): Unit = {
    cache.strategy.invalidateCache()
    runIO(http.post(uri"/lokalisointi/cxf/rest/v1/localisation/update", localizations)(json4sEncoderOf[List[UpdateLocalization]])(Http.unitDecoder))
  }

  def init {
    lazy val inLocalizationService = localizationsFromLocalizationService
    if (config.getBoolean("localization.create")) {
      val missing = defaultLocalizations.defaultFinnishTexts.flatMap {
        case (key, defaultText) => inLocalizationService.get(key) match {
          case Some(_) => None
          case None => Some(List(UpdateLocalization("fi", key, defaultText, localizationConfig.localizationCategory), UpdateLocalization("sv", key, "", localizationConfig.localizationCategory), UpdateLocalization("en", key, "", localizationConfig.localizationCategory)))
        }
      }.toList.flatten

      if (missing.nonEmpty) {
        logger.info("Creating " + missing.length + " missing localizations: " + JsonSerializer.writeWithRoot(missing))
        updateToRemote(missing)
      }
    }

    if (config.getBoolean("localization.update")) {
      logger.info(s"Updating all localizations to localization-service")
      val mockValues: immutable.Seq[(String, String, String)] = parseLocalizations(readLocalLocalizations(localizationConfig.mockLocalizationResourceFilename)).toList.flatMap { case (key, localizationsForKey) =>
        localizationsForKey.toList.map { case (lang, value) =>
          (key, lang, value)
        }
      }
      val toUpdate = mockValues.flatMap { case (key, lang, value) =>
        inLocalizationService.getOrElse(key, Map()).get(lang) match {
            case Some(remoteValue) if remoteValue == value =>
              //logger.info(s"Up to date $key.$lang = $value")
              Nil
            case Some(remoteValue) =>
              logger.info(s"Update $key.$lang $remoteValue => $value")
              List(UpdateLocalization(lang, key, value, localizationConfig.localizationCategory))
            case None =>
              logger.info(s"Add $key.$lang = $value")
              List(UpdateLocalization(lang, key, value, localizationConfig.localizationCategory))
          }
      }.toList

      if (toUpdate.isEmpty) {
        logger.info(s"Localization are up to date at ${config.getString("localization.url")}")
      } else {
        logger.info(s"Updating ${toUpdate.length} missing localizations to ${config.getString("localization.url")}")
        updateToRemote(toUpdate)
      }
    }
  }

  private def updateToRemote(toUpdate: List[UpdateLocalization]) = {
    if (toUpdate.nonEmpty) {
      try {
        createOrUpdate(toUpdate)
      } catch {
        case e: Exception => logger.warn(e)("Failed to create missing localizations")
      }
    }

    logger.info("done.")
  }
}

case class UpdateLocalization(
  locale: String,
  key: String,
  value: String,
  category: String
)

case class LocalizationServiceLocalization(
  id: Int,
  locale: String,
  category: String,
  key: String,
  value: String
)
