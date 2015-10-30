package fi.oph.tor.koodisto
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

class MockKoodistoPalvelu extends KoodistoPalvelu {
  override def getKoodisto(koodisto: KoodistoViittaus) = {
    Json.readFileIfExists("src/main/resources/mockdata/koodisto/" + koodisto.koodistoUri + ".json").map(_.extract[List[KoodistoKoodi]])
  }

  override def getAlakoodit(koodiarvo: String) = {
    Json.readFile("src/main/resources/mockdata/koodisto/alakoodit/" + koodiarvo + ".json").extract[List[Alakoodi]]
  }
}
