package fi.oph.tor.arvosana

import com.typesafe.config.Config
import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViittaus}
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi

class ArviointiasteikkoRepository(koodistoPalvelu: KoodistoPalvelu) {
  def getArviointiasteikkoViittaus(koulutustyyppi: Koulutustyyppi): Option[KoodistoViittaus] = {
    // TODO: parempi arviointiasteikkokoodiston tunnistus
    koodistoPalvelu.getAlakoodit("koulutustyyppi_" + koulutustyyppi).map(_.koodisto).find(_.koodistoUri.contains("asteikko")).map(_.latestVersion)
  }

  def getArviointiasteikko(koodisto: KoodistoViittaus): Option[Arviointiasteikko] = {
    koodistoPalvelu.getKoodisto(koodisto).map(koodit => Arviointiasteikko(koodisto, koodit.map(koodi => Arvosana(koodi.koodiUri, koodi.metadata.flatMap(_.nimi).headOption.getOrElse(koodi.koodiUri))).sortBy(_.id)))
  }
}

object ArviointiasteikkoRepository {
  def apply(config: Config) = {
    new ArviointiasteikkoRepository(KoodistoPalvelu(config))
  }
}