package fi.oph.koski.koodisto

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonFiles
import fi.oph.koski.log.Logging
import fi.oph.koski.koodisto.MockKoodistoPalvelu.{sortKoodistoMetadata, sortKoodistoKoodiMetadata}

object KoodistoMockDataUpdater extends App with Logging {
  updateMockDataFromKoodistoPalvelu(KoskiApplication.defaultConfig)

  def updateMockDataFromKoodistoPalvelu(config: Config): Unit = {
    val includeKoskiKoodistot = System.getProperty("koskiKoodistot", "true").toBoolean
    val includeMuutKoodistot = System.getProperty("muutKoodistot", "true").toBoolean
    val koodistot = Koodistot.koskiKoodistot.filter(Function.const(includeKoskiKoodistot)) ++ Koodistot.muutKoodistot.filter(Function.const(includeMuutKoodistot))
    val kp = KoodistoPalvelu.withoutCache(config)
    koodistot.foreach(koodisto => updateMockDataForKoodisto(koodisto, kp))
  }

  private def updateMockDataForKoodisto(koodistoUri: String, kp: KoodistoPalvelu): Unit = {
    kp.getLatestVersionOptional(koodistoUri) match {
      case Some(versio) =>
        logger.info("Päivitetään testidata koodistolle " + koodistoUri + "/" + versio)
        JsonFiles.writeFile(
          MockKoodistoPalvelu.koodistoFileName(koodistoUri, None),
          kp.getKoodisto(versio).map(sortKoodistoMetadata)
        )
        val koodit: List[KoodistoKoodi] = kp.getKoodistoKoodit(versio).map(sortKoodistoKoodiMetadata).sortBy(_.koodiArvo)
        JsonFiles.writeFile(
          MockKoodistoPalvelu.koodistoKooditFileName(koodistoUri, None),
          koodit
        )
      case None =>
        logger.warn("Koodistoa ei löydy koodistopalvelusta: " + koodistoUri)
    }
  }
}
