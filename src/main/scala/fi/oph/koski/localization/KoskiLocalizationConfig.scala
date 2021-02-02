package fi.oph.koski.localization

class KoskiLocalizationConfig extends LocalizationConfig {
  final def localizationCategory: String = "koski"
  final def defaultFinnishTextsResourceFilename: String = "/localization/koski-default-texts.json"
  final def mockLocalizationResourceFilename: String = "/mockdata/lokalisointi/koski.json"
}
