package fi.oph.tor.arvosana

import com.typesafe.config.Config
import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViittaus}
import fi.oph.tor.tutkinto.Koulutustyyppi.Koulutustyyppi

class ArviointiasteikkoRepository(koodistoPalvelu: KoodistoPalvelu) {
  def getArviointiasteikkoViittaus(koulutustyyppi: Koulutustyyppi): Option[KoodistoViittaus] = {
    koodistoPalvelu.getAlakoodit("koulutustyyppi_" + koulutustyyppi).map(_.koodisto)
      .map(_.latestVersion)
      .find(koodistoPalvelu.getKoodisto(_).find(_.metadata.flatMap(_.kasite).contains("arviointiasteikko")).isDefined)
  }

  def getArviointiasteikko(koodisto: KoodistoViittaus): Option[Arviointiasteikko] = {
    koodistoPalvelu.getKoodistoKoodit(koodisto).map(koodit => Arviointiasteikko(koodisto, koodit.map(koodi => Arvosana(koodi.koodiUri, koodi.metadata.flatMap(_.nimi).headOption.getOrElse(koodi.koodiUri))).sortBy(_.id)))
  }
}

object ArviointiasteikkoRepository {
  def apply(config: Config) = {
    new ArviointiasteikkoRepository(KoodistoPalvelu(config))
  }
}