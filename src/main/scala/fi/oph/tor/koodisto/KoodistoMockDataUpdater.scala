package fi.oph.tor.koodisto

import com.typesafe.config.Config
import fi.oph.tor.config.TorApplication
import fi.oph.tor.json.Json

object KoodistoMockDataUpdater extends App {
  updateMockDataFromKoodistoPalvelu(TorApplication.apply().config)


  def updateMockDataFromKoodistoPalvelu(config: Config): Unit = {
    val kp: KoodistoPalvelu = KoodistoPalvelu.withoutCache(config)
    MockKoodistoPalvelu.koodistot.foreach(koodisto => updateMockDataForKoodisto(koodisto, kp))
  }

  private def updateMockDataForKoodisto(koodistoUri: String, kp: KoodistoPalvelu): Unit = {
    kp.getLatestVersion(koodistoUri) match {
      case Some(versio) =>
        Json.writeFile(
          MockKoodistoPalvelu.koodistoFileName(koodistoUri),
          kp.getKoodisto(KoodistoViittaus(koodistoUri, versio))
        )
        Json.writeFile(
          MockKoodistoPalvelu.koodistoKooditFileName(koodistoUri),
          kp.getKoodistoKoodit(KoodistoViittaus(koodistoUri, versio))
        )
      case None => throw new IllegalStateException("Koodisto not found from koodisto-service: " + koodistoUri)
    }
  }
}
