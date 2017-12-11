package fi.oph.koski.localization

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonFiles
import fi.oph.koski.json.JsonSerializer.extract
import org.json4s.{JArray, JObject, JValue}

object LocalizationMockDataUpdater extends App {
  val filename = "src/main/resources" + MockLocalizationRepository.resourceName
  val JArray(localizations) = KoskiApplication.apply(KoskiApplication.defaultConfig).localizationRepository.fetchLocalizations.asInstanceOf[JArray]
  //val JArray(existing) = JsonFiles.readFile(filename)
  val sorted = stabilize(localizations)
  JsonFiles.writeFile(filename, sorted)

  private def stabilize(entries: Seq[JValue]) = {
    entries
      .sortBy {
        case j: JObject => (extract[String](j \ "key"), extract[String](j \ "locale"))
      }
      .map {
        case JObject(fields) => JObject(fields.sortBy(_._1))
      }
  }
}

