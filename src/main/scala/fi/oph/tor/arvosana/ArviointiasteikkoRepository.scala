package fi.oph.tor.arvosana

import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViite}
import fi.oph.tor.schema.KoodistoKoodiViite
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
    koodistoPalvelu.getKoodistoKoodit(koodisto).map(koodit => Arviointiasteikko(koodisto, koodit.map(koodi => KoodistoKoodiViite(koodi.koodiArvo, koodi.metadata.flatMap(_.nimi).headOption, koodisto.koodistoUri, Some(koodisto.versio))).sortBy(_.koodiarvo)))
  }
}

object ArviointiasteikkoRepository {
  def apply(koodistoPalvelu: KoodistoPalvelu) = {
    new ArviointiasteikkoRepository(koodistoPalvelu)
  }
}