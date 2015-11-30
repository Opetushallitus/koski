package fi.oph.tor.koodisto
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

class MockKoodistoPalvelu extends KoodistoPalvelu {
  def getKoodistoKoodit(koodisto: KoodistoViittaus): Option[List[KoodistoKoodi]] = {
    Json.readFileIfExists("src/main/resources/mockdata/koodisto/koodit/" + koodisto.koodistoUri + ".json").map(_.extract[List[KoodistoKoodi]])
  }

  def getAlakoodit(koodiarvo: String): List[Alakoodi] = {
    Json.readFile("src/main/resources/mockdata/koodisto/alakoodit/" + koodiarvo + ".json").extract[List[Alakoodi]]
  }

  def getKoodisto(koodisto: KoodistoViittaus): Option[Koodisto] = {
    getKoodisto(koodisto.koodistoUri)
  }

  def getKoodisto(koodistoUri: String): Option[Koodisto] = {
    Json.readFileIfExists("src/main/resources/mockdata/koodisto/koodistot/" + koodistoUri + ".json").map(_.extract[Koodisto])
  }

  def getLatestVersion(koodistoUri: String): Option[Int] = getKoodisto(koodistoUri).map { _.versio }

  def createKoodisto(koodisto: Koodisto) = throw new UnsupportedOperationException
  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = throw new UnsupportedOperationException
}
