package fi.oph.koski.localization

import com.typesafe.config.Config
import fi.oph.koski.cache.{Cache, CacheManager, CachingProxy}
import fi.oph.koski.http.Http
import fi.oph.koski.http.Http._
import fi.oph.koski.json.Json._
import fi.oph.koski.localization.LocalizedString.sanitizeRequired
import org.json4s._

trait LocalizationRepository {
  def localizations(): Map[String, LocalizedString]= {
    val localized: Map[String, Map[String, String]] = fetchLocalizations().extract[List[LokalisaatioPalveluLokalisaatio]]
      .groupBy(_.key)
      .mapValues(_.map(v => (v.locale, v.value)).toMap)


    readResource("/localization/default-texts.json").extract[Map[String, String]].map {
      case (key, value) => (key, sanitizeRequired(localized.getOrElse(key, Map.empty), value))
    }
  }

  def fetchLocalizations(): JValue
}

object LocalizationRepository {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager): LocalizationRepository = {
    CachingProxy(Cache.cacheAllNoRefresh("LocalizationRepository", 60, 1), config.getString("localization.url") match {
      case "mock" =>
        MockLocalizationRepository
      case url =>
        new RemoteLocalizationRepository(Http(url))
    })
  }
}

object MockLocalizationRepository extends LocalizationRepository {
  override def fetchLocalizations(): JValue = readResource("/mockdata/lokalisointi/koski.json")
}

class RemoteLocalizationRepository(http: Http) extends LocalizationRepository {
  override def fetchLocalizations(): JValue = runTask(http.get(uri"/lokalisointi/cxf/rest/v1/localisation?category=koski")(Http.parseJson[JValue]))
}

case class LokalisaatioPalveluLokalisaatio(
  id: Integer,
  locale: String,
  category: String,
  key: String,
  value: String
)


