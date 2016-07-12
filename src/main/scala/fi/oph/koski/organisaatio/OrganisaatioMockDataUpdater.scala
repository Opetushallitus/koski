package fi.oph.koski.organisaatio

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.{KoodistoPalvelu, KoodistoViitePalvelu}

object OrganisaatioMockDataUpdater extends App {
  updateMockDataFromOrganisaatioPalvelu(KoskiApplication.defaultConfig)

  def updateMockDataFromOrganisaatioPalvelu(config: Config): Unit = {
    val koodisto = KoodistoViitePalvelu(KoodistoPalvelu.apply(config))
    val organisaatioPalvelu = OrganisaatioRepository.withoutCache(config, koodisto)

    MockOrganisaatiot.organisaatiot.foreach(oid => updateMockDataForOrganisaatio(oid, organisaatioPalvelu))
  }

  def updateMockDataForOrganisaatio(oid: String, organisaatioPalvelu: JsonOrganisaatioRepository): Unit = {
    val tulos = organisaatioPalvelu.fetch(oid)
    Json.writeFile(MockOrganisaatioRepository.hierarchyFilename(oid), tulos)
  }
}
