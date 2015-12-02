package fi.oph.tor.arvosana

import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViite}
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi

class ArviointiasteikkoRepository(koodistoPalvelu: KoodistoPalvelu) {
  def getArviointiasteikkoViittaus(koulutustyyppi: Koulutustyyppi): Option[KoodistoViite] = {
    val koodistoUri = koulutustyyppi match {
      case 1 => "ammatillisenperustutkinnonarviointiasteikko"
      case _ => "ammattijaerikoisammattitutkintojenarviointiasteikko"
    }
    koodistoPalvelu.getLatestVersion(koodistoUri)
  }

  def getArviointiasteikko(koodisto: KoodistoViite): Option[Arviointiasteikko] = {
    koodistoPalvelu.getKoodistoKoodiViitteet(koodisto).map(koodit => Arviointiasteikko(koodisto, koodit.sortBy(_.koodiarvo)))
  }
}

object ArviointiasteikkoRepository {
  def apply(koodistoPalvelu: KoodistoPalvelu) = {
    new ArviointiasteikkoRepository(koodistoPalvelu)
  }
}