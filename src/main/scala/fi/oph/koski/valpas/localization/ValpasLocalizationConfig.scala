package fi.oph.koski.valpas.localization

import fi.oph.koski.localization.LocalizationConfig

class ValpasLocalizationConfig extends LocalizationConfig {
  final def localizationCategory: String = "valpas"

  final def defaultFinnishTextsResourceFilename: String = "/valpas/localization/valpas-default-texts.json"

  final def mockLocalizationResourceFilename: String = "/valpas/mockdata/lokalisointi/valpas.json"
}
