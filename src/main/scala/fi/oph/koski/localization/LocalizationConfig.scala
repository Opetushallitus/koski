package fi.oph.koski.localization

case class LocalizationConfig(
  localizationCategory: String,
  defaultFinnishTextsResourceFilename: String,
  mockLocalizationResourceFilename: String
)

object LocalizationConfig {
  def apply(category: String): LocalizationConfig =
    category match {
      case "koski" => LocalizationConfig("koski", "/localization/koski-default-texts.json", "/mockdata/lokalisointi/koski.json")
    }
}
