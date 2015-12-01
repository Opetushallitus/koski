package fi.oph.tor.arvosana

import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViittaus}
import fi.oph.tor.schema.KoodistoKoodiViite
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi

class ArviointiasteikkoRepository(koodistoPalvelu: KoodistoPalvelu) {
  def getArviointiasteikkoViittaus(koulutustyyppi: Koulutustyyppi): Option[KoodistoViittaus] = {
    val koodistoUri = koulutustyyppi match {
      case 1 => "ammatillisenperustutkinnonarviointiasteikko"
      case _ => "ammattijaerikoisammattitutkintojenarviointiasteikko"
    }
    koodistoPalvelu.getLatestVersion(koodistoUri).map(versio => KoodistoViittaus(koodistoUri, versio))
  }

  def getArviointiasteikko(koodisto: KoodistoViittaus): Option[Arviointiasteikko] = {
    koodistoPalvelu.getKoodistoKoodit(koodisto).map(koodit => Arviointiasteikko(koodisto, koodit.map(koodi => KoodistoKoodiViite(koodi.koodiArvo, koodi.metadata.flatMap(_.nimi).headOption, koodisto.koodistoUri, Some(koodisto.versio))).sortBy(_.koodiarvo)))
  }
}

object ArviointiasteikkoRepository {
  def apply(koodistoPalvelu: KoodistoPalvelu) = {
    new ArviointiasteikkoRepository(koodistoPalvelu)
  }
}