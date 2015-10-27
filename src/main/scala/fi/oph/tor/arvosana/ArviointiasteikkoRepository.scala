package fi.oph.tor.arvosana

import com.typesafe.config.Config
import fi.oph.tor.arvosana.ArviointiasteikkoRepository.example
import fi.oph.tor.koodisto.{KoodistoPalvelu, KoodistoViittaus}

class ArviointiasteikkoRepository(koodistoPalvelu: KoodistoPalvelu) {
  def getArviointiasteikko(koodisto: KoodistoViittaus): Option[Arviointiasteikko] = {
    koodistoPalvelu.getKoodisto(koodisto).map(koodit => Arviointiasteikko(koodisto, koodit.map(koodi => Arvosana(koodi.koodiUri, koodi.metadata.flatMap(_.nimi).headOption.getOrElse(koodi.koodiUri))).sortBy(_.id)))
  }

  def getAll: List[Arviointiasteikko] = List(getArviointiasteikko(example)).flatten // TODO: <- get rid of fixed data
}

object ArviointiasteikkoRepository {
  def apply(config: Config) = {
    new ArviointiasteikkoRepository(KoodistoPalvelu(config))
  }

  val example = KoodistoViittaus("ammatillisenperustutkinnonarviointiasteikko", 1) // TODO: <- get rid of fixed data
}