package fi.oph.tor.koodisto
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

object MockKoodistoPalvelu extends KoodistoPalvelu {
  val koodistot = List (
    "arviointiasteikkoammatillinent1k3",
    "arviointiasteikkoammatillinenhyvaksyttyhylatty",
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

  def getKoodistoKoodit(koodisto: KoodistoViite): Option[List[KoodistoKoodi]] = {
    Json.readFileIfExists(koodistoKooditFileName(koodisto.koodistoUri)).map(_.extract[List[KoodistoKoodi]])
  }

  def getKoodisto(koodisto: KoodistoViite): Option[Koodisto] = {
    getKoodisto(koodisto.koodistoUri)
  }

  def getKoodisto(koodistoUri: String): Option[Koodisto] = {
    Json.readFileIfExists(koodistoFileName(koodistoUri)).map(_.extract[Koodisto])
  }

  def getLatestVersion(koodistoUri: String): Option[KoodistoViite] = getKoodisto(koodistoUri).map { _.koodistoViite }

  def createKoodisto(koodisto: Koodisto) = throw new UnsupportedOperationException
  def createKoodi(koodistoUri: String, koodi: KoodistoKoodi) = throw new UnsupportedOperationException
  def createKoodistoRyhmä(ryhmä: KoodistoRyhmä) = throw new UnsupportedOperationException
  def removeKoodistoRyhmä(toInt: Int) = throw new UnsupportedOperationException

  def koodistoKooditFileName(koodistoUri: String): String = {
    "src/main/resources/mockdata/koodisto/koodit/" + koodistoUri + ".json"
  }

  def koodistoFileName(koodistoUri: String): String = {
    "src/main/resources/mockdata/koodisto/koodistot/" + koodistoUri + ".json"
  }

}