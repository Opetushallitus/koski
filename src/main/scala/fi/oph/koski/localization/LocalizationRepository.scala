package fi.oph.koski.localization

import com.typesafe.config.Config
import fi.oph.koski.cache.{Cache, CacheManager, CachingProxy}
import fi.oph.koski.http.Http
import fi.oph.koski.json.Json._
import fi.oph.koski.localization.LocalizedString.sanitizeRequired

trait LocalizationRepository {
  def localizations(): Map[String, LocalizedString]
}

object LocalizationRepository {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager): LocalizationRepository = {
    CachingProxy(Cache.cacheAllNoRefresh("LocalizationRepository", 60, 1), config.getString("localization.url") match {
      case "mock" =>
        MockLocalizationRepository
      case url =>
        MockLocalizationRepository
    })
  }
}

object MockLocalizationRepository extends LocalizationRepository {

  override def localizations(): Map[String, LocalizedString] = {
    val localized: Map[String, Map[String, String]] = readResource("/mockdata/lokalisointi/koski.json").extract[List[LokalisaatioPalveluLokalisaatio]]
          .groupBy(_.key)
          .mapValues(_.map(v => (v.locale, v.value)).toMap)


    readResource("/localization/default-texts.json").extract[Map[String, String]].map {
      case (key, value) => (key, sanitizeRequired(localized.getOrElse(key, Map.empty), value))
    }
  }
}

class RemoteLocalizationRepository(http: Http) extends LocalizationRepository {
  override def localizations(): Map[String, LocalizedString] = ???
}

case class LokalisaatioPalveluLokalisaatio(
  id: Integer,
  locale: String,
  category: String,
  key: String,
  value: String
)


