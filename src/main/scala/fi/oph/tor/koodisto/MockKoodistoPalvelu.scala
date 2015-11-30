package fi.oph.tor.koodisto
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

object MockKoodistoPalvelu extends KoodistoPalvelu {
  val koodistot = List (
    "ammatillisenperustutkinnonarviointiasteikko",
    "ammattijaerikoisammattitutkintojenarviointiasteikko",
    "jarjestamismuoto",
    "koulutus",
    "kieli",
    "lahdejarjestelma",
    "lasnaolotila",
    "opetusryhma",
    "opintojenlaajuusyksikko",
    "opintojenrahoitus",
    "opintojentavoite",
    "opiskeluoikeudentila",
    "osaamisala",
    "suorituksentila",
    "suoritustapa",
    "tutkintonimikkeet",
    "tutkinnonosat"
  )

  def getKoodistoKoodit(koodisto: KoodistoViittaus): Option[List[KoodistoKoodi]] = {
    Json.readFileIfExists(koodistoKooditFileName(koodisto.koodistoUri)).map(_.extract[List[KoodistoKoodi]])
  }

  def getAlakoodit(koodiarvo: String): List[Alakoodi] = {
    Json.readFile("src/main/resources/mockdata/koodisto/alakoodit/" + koodiarvo + ".json").extract[List[Alakoodi]]
  }

  def getKoodisto(koodisto: KoodistoViittaus): Option[Koodisto] = {
    getKoodisto(koodisto.koodistoUri)
  }

  def getKoodisto(koodistoUri: String): Option[Koodisto] = {
    Json.readFileIfExists(koodistoFileName(koodistoUri)).map(_.extract[Koodisto])
  }

  def getLatestVersion(koodistoUri: String): Option[Int] = getKoodisto(koodistoUri).map { _.versio }

  def createKoodisto(koodisto: Koodisto) = throw new UnsupportedOperationException
  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = throw new UnsupportedOperationException

  def koodistoKooditFileName(koodistoUri: String): String = {
    "src/main/resources/mockdata/koodisto/koodit/" + koodistoUri + ".json"
  }

  def koodistoFileName(koodistoUri: String): String = {
    "src/main/resources/mockdata/koodisto/koodistot/" + koodistoUri + ".json"
  }
}