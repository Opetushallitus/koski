package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.config.TorApplication
import fi.oph.tor.json.Json
import fi.oph.tor.log.Logging

object KoodistoMockDataUpdater extends App with Logging {
  updateMockDataFromKoodistoPalvelu(TorApplication.apply().config)


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
