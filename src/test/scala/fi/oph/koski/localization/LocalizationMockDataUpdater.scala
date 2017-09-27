package fi.oph.koski.localization

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonFiles
import fi.oph.koski.json.JsonSerializer.extract
import org.json4s.{JArray, JObject, JValue}

object LocalizationMockDataUpdater extends App {
  val JArray(localizations) = KoskiApplication.apply(KoskiApplication.defaultConfig).localizationRepository.fetchLocalizations.asInstanceOf[JArray]
  val sorted: JValue = JArray(localizations.sortBy {
    case j: JObject => (extract[String](j \ "key"), extract[String](j \ "locale"))
  })
  val filename = "src/main/resources" + MockLocalizationRepository.resourceName
  JsonFiles.writeFile(filename, sorted)
}
