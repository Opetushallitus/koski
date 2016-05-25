package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.Json
import fi.oph.koski.log.Logging

object KoodistoMockDataUpdater extends App with Logging {
  updateMockDataFromKoodistoPalvelu(KoskiApplication.apply().config)


  def updateMockDataFromKoodistoPalvelu(config: Config): Unit = {
    val kp = KoodistoPalvelu.withoutCache(config)
    MockKoodistoPalvelu.koodistot.foreach(koodisto => updateMockDataForKoodisto(koodisto, kp))
  }

  private def updateMockDataForKoodisto(koodistoUri: String, kp: KoodistoPalvelu): Unit = {
    kp.getLatestVersion(koodistoUri) match {
      case Some(versio) =>
        logger.info("Päivitetään testidata koodistolle " + koodistoUri + "/" + versio)
        Json.writeFile(
          MockKoodistoPalvelu.koodistoFileName(koodistoUri),
          kp.getKoodisto(versio)
        )
        val koodit: List[KoodistoKoodi] = kp.getKoodistoKoodit(versio).toList.flatten
        Json.writeFile(
          MockKoodistoPalvelu.koodistoKooditFileName(koodistoUri),
          koodit
        )
      case None =>
        logger.warn("Koodistoa ei löydy koodistopalvelusta: " + koodistoUri)
    }
  }
}
