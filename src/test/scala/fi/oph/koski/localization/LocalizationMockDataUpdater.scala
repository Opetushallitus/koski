package fi.oph.koski.localization

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonFiles
import org.json4s.JValue

object LocalizationMockDataUpdater extends App {
  val localizations: JValue = KoskiApplication.apply(KoskiApplication.defaultConfig).localizationRepository.fetchLocalizations
  val filename = "src/main/resources" + MockLocalizationRepository.resourceName
  JsonFiles.writeFile(filename, localizations)
}
