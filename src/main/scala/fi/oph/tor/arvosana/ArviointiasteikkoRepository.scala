package fi.oph.tor.arvosana

import com.typesafe.config.Config
import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViittaus}

class ArviointiasteikkoRepository(koodistoPalvelu: KoodistoPalvelu) {
  def getArviointiasteikkoViittaus(koulutusKoodi: String, suoritustapakoodi: String): Option[KoodistoViittaus] = {
    Some(ArviointiasteikkoRepository.example)
  }

  def getArviointiasteikko(koodisto: KoodistoViittaus): Option[Arviointiasteikko] = {
    koodistoPalvelu.getKoodisto(koodisto).map(koodit => Arviointiasteikko(koodisto, koodit.map(koodi => Arvosana(koodi.koodiUri, koodi.metadata.flatMap(_.nimi).headOption.getOrElse(koodi.koodiUri))).sortBy(_.id)))
  }
}

object ArviointiasteikkoRepository {
  def apply(config: Config) = {
    new ArviointiasteikkoRepository(KoodistoPalvelu(config))
  }

  val example = KoodistoViittaus("ammatillisenperustutkinnonarviointiasteikko", 1) // TODO: <- get rid of fixed data
}