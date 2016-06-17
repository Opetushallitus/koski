package fi.oph.koski.arvosana

import fi.oph.koski.koodisto.{KoodistoViite, KoodistoViitePalvelu}
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

class ArviointiasteikkoRepository(koodistoPalvelu: KoodistoViitePalvelu) {
  def getArviointiasteikkoViittaus(koulutustyyppi: Koulutustyyppi): Option[KoodistoViite] = {
    val koodistoUri = koulutustyyppi match {
      case 1 => "arviointiasteikkoammatillinent1k3"
      case _ => "arviointiasteikkoammatillinenhyvaksyttyhylatty"
    }
    koodistoPalvelu.getLatestVersion(koodistoUri)
  }

  def getArviointiasteikko(koodisto: KoodistoViite): Option[Arviointiasteikko] = {
    koodistoPalvelu.getKoodistoKoodiViitteet(koodisto).map(koodit => Arviointiasteikko(koodisto, koodit.sortBy(_.koodiarvo)))
  }
}

object ArviointiasteikkoRepository {
  def apply(koodistoPalvelu: KoodistoViitePalvelu) = {
    new ArviointiasteikkoRepository(koodistoPalvelu)
  }
}