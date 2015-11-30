package fi.oph.tor.arvosana

import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViittaus}
import fi.oph.tor.schema.KoodistoKoodiViite
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi

class ArviointiasteikkoRepository(koodistoPalvelu: KoodistoPalvelu) {
  def getArviointiasteikkoViittaus(koulutustyyppi: Koulutustyyppi): Option[KoodistoViittaus] = {
    koodistoPalvelu.getAlakoodit("koulutustyyppi_" + koulutustyyppi).map(_.koodisto)
      .map(_.latestVersion)
      .find(koodistoPalvelu.getKoodisto(_).find(_.metadata.flatMap(_.kasite).contains("arvosana")).isDefined)
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