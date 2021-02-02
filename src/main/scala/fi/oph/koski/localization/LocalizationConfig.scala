package fi.oph.koski.localization

trait LocalizationConfig {
  def localizationCategory: String
  def defaultFinnishTextsResourceFilename: String
  def mockLocalizationResourceFilename: String
}
