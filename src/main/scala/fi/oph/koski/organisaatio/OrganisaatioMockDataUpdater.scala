package fi.oph.koski.organisaatio

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.{KoodistoPalvelu, KoodistoViitePalvelu}

object OrganisaatioMockDataUpdater extends App {
  updateMockDataFromOrganisaatioPalvelu(KoskiApplication.defaultConfig)

  private def updateMockDataFromOrganisaatioPalvelu(config: Config): Unit = {
    val koodisto = KoodistoViitePalvelu(KoodistoPalvelu.apply(config))
    val organisaatioPalvelu = OrganisaatioRepository.withoutCache(config, koodisto).asInstanceOf[RemoteOrganisaatioRepository]

    MockOrganisaatiot.roots.foreach(oid => updateMockDataForOrganisaatio(oid, organisaatioPalvelu))
  }

  private def updateMockDataForOrganisaatio(oid: String, organisaatioPalvelu: RemoteOrganisaatioRepository): Unit = {
    val tulos = organisaatioPalvelu.fetch(oid)
    Json.writeFile(MockOrganisaatioRepository.hierarchyFilename(oid), tulos)
  }
}
