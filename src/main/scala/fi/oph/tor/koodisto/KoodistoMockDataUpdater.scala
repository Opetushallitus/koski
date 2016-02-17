package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.config.TorApplication
import fi.oph.tor.json.Json

object KoodistoMockDataUpdater extends App {
  updateMockDataFromKoodistoPalvelu(TorApplication.apply().config)


  def updateMockDataFromKoodistoPalvelu(config: Config): Unit = {
    val kp = KoodistoPalvelu.withoutCache(config)
    MockKoodistoPalvelu.koodistot.foreach(koodisto => updateMockDataForKoodisto(koodisto, kp))
  }

  private def updateMockDataForKoodisto(koodistoUri: String, kp: KoodistoPalvelu): Unit = {
    kp.getLatestVersion(koodistoUri) match {
      case Some(versio) =>
        Json.writeFile(
          MockKoodistoPalvelu.koodistoFileName(koodistoUri),
          kp.getKoodisto(versio)
        )
        val koodit: List[KoodistoKoodi] = kp.getKoodistoKoodit(versio).toList.flatten
        Json.writeFile(
          MockKoodistoPalvelu.koodistoKooditFileName(koodistoUri),
          koodit
        )
      case None => throw new IllegalStateException("Koodisto not found from koodisto-service: " + koodistoUri)
    }
  }
}
