package fi.oph.tor.koodisto

import java.time.LocalDate

import com.typesafe.config.Config
import fi.vm.sade.utils.slf4j.Logging

object KoodistoCreator extends Logging {
  def createKoodistotFromMockData(config: Config): Unit = {
    val kp = KoodistoPalvelu.withoutCache(config)
    MockKoodistoPalvelu.koodistot.foreach(koodisto => createKoodistoFromMockData(koodisto, kp))
  }

  private def createKoodistoFromMockData(koodistoUri: String, kp: KoodistoPalvelu): Unit = {
    val koodistoViite: KoodistoViite = kp.getLatestVersion(koodistoUri).getOrElse {
      MockKoodistoPalvelu.getKoodisto(koodistoUri) match {
        case None => throw new IllegalStateException("Mock not found: " + koodistoUri)
        case Some(koodisto) =>
          logger.info("Luodaan koodisto " + koodisto.koodistoUri)
          kp.createKoodisto(koodisto)
          koodisto.koodistoViite
      }
    }
    val koodit = kp.getKoodistoKoodit(koodistoViite).toList.flatten
    val luotavatKoodit = MockKoodistoPalvelu.getKoodistoKoodit(koodistoViite).toList.flatten.filter { koodi: KoodistoKoodi => !koodit.find(_.koodiArvo == koodi.koodiArvo).isDefined }
    luotavatKoodit.foreach { koodi =>
      logger.info("Luodaan koodi " + koodi.koodiUri)
      kp.createKoodi(koodistoUri, koodi.copy(voimassaAlkuPvm = Some(LocalDate.now)))
    }
  }
}
