package fi.oph.tor.koodisto

import java.time.LocalDate

import com.typesafe.config.Config

object KoodistoCreator {
  def createKoodistotFromMockData(config: Config): Unit = {
    val kp: KoodistoPalvelu = KoodistoPalvelu.withoutCache(config)
    MockKoodistoPalvelu.koodistot.foreach(koodisto => createKoodistoFromMockData(koodisto, kp))
  }

  private def createKoodistoFromMockData(koodistoUri: String, kp: KoodistoPalvelu): Unit = {
    val versio: Int = kp.getLatestVersion(koodistoUri) match {
      case Some(version) => version
      case None =>
        MockKoodistoPalvelu.getKoodisto(koodistoUri) match {
          case None => throw new IllegalStateException("Mock not found: " + koodistoUri)
          case Some(koodisto) =>
            kp.createKoodisto(koodisto)
            koodisto.versio
        }
    }
    val koodistoViittaus: KoodistoViittaus = KoodistoViittaus(koodistoUri, versio)
    val koodit = kp.getKoodistoKoodit(koodistoViittaus).toList.flatten
    val luotavatKoodit = MockKoodistoPalvelu.getKoodistoKoodit(koodistoViittaus).toList.flatten.filter { koodi: KoodistoKoodi => !koodit.find(_.koodiArvo == koodi.koodiArvo).isDefined }
    luotavatKoodit.foreach { koodi =>
      kp.createKoodi(koodistoUri, koodi.copy(voimassaAlkuPvm = Some(LocalDate.now)))
    }
  }
}
