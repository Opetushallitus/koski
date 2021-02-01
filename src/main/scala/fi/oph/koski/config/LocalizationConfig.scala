package fi.oph.koski.config

case class LocalizationConfig(
  localizationCategory: String,
  defaultFinnishTextsResourceFilename: String,
  mockLocalizationResourceFilename: String
)

object LocalizationConfig {
  def apply(category: String): LocalizationConfig =
    category match {
      case "koski" => LocalizationConfig("koski", "/localization/koski-default-texts.json", "/mockdata/lokalisointi/koski.json")
      case "valpas" => LocalizationConfig("valpas", "/localization/valpas-default-texts.json", "/mockdata/lokalisointi/valpas.json")
    }
}
