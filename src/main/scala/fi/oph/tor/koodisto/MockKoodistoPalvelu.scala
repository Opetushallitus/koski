package fi.oph.tor.koodisto
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

class MockKoodistoPalvelu extends KoodistoPalvelu {
  override def getKoodisto(koodisto: KoodistoViittaus) = {
    val filename = "src/main/resources/mockdata/koodisto/" + koodisto.koodistoUri + ".json"
    Json.readFileIfExists(filename).map(_.extract[List[KoodistoKoodi]])
  }
}
