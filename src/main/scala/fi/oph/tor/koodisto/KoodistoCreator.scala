package fi.oph.tor.koodisto

import java.time.LocalDate
import fi.oph.tor.config.TorApplication

object KoodistoCreator extends App {
  val mock = new MockKoodistoPalvelu
  val app = TorApplication()
  private val kp: KoodistoPalvelu = KoodistoPalvelu.withoutCache(app.config)
  createKoodistoFromMockData("ammatillisenperustutkinnonarviointiasteikko")
  createKoodistoFromMockData("ammattijaerikoisammattitutkintojenarviointiasteikko")
  createKoodistoFromMockData("suoritustapa")

  def createKoodistoFromMockData(koodistoUri: String): Unit = {
    val versio: Int = kp.getLatestVersion(koodistoUri) match {
      case Some(version) => version
      case None =>
        mock.getKoodisto(koodistoUri) match {
          case None => throw new IllegalStateException("Mock not found: " + koodistoUri)
          case Some(koodisto) =>
            kp.createKoodisto(koodisto)
            kp.getLatestVersion(koodistoUri).get
        }
    }
    val koodistoViittaus: KoodistoViittaus = KoodistoViittaus(koodistoUri, versio)
    val koodit = kp.getKoodistoKoodit(koodistoViittaus).toList.flatten
    val luotavatKoodit = mock.getKoodistoKoodit(koodistoViittaus).toList.flatten.filter { koodi: KoodistoKoodi => !koodit.find(_.koodiArvo == koodi.koodiArvo).isDefined }
    luotavatKoodit.foreach { koodi =>
      kp.createKoodi(koodistoUri, koodi.copy(voimassaAlkuPvm = Some(LocalDate.now)))
    }
  }
}
