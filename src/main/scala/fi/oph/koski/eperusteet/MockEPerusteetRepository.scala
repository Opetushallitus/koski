package fi.oph.koski.eperusteet

import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._

class MockEPerusteetRepository extends EPerusteetRepository {
  lazy val rakenteet: List[EPerusteRakenne] = List(
    "rakenne-autoalan-perustutkinto",
    "rakenne-luonto-ja-ymparistoala",
    "rakenne-autoalan-tyonjohto",
    "rakenne-perusopetus",
    "rakenne-lukio").map { id =>
    Json.readResourceIfExists("/mockdata/eperusteet/" + id + ".json").get.extract[EPerusteRakenne]
  }

  def findPerusteet(query: String): List[EPeruste] = {
    // Hakee aina samoilla kriteereillä "auto"
    Json.readFile("src/main/resources/mockdata/eperusteet/hakutulokset-auto.json").extract[EPerusteet].data.filter(_.nimi("fi").toLowerCase.contains(query.toLowerCase))
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    rakenteet.filter(_.diaarinumero == diaarinumero).map(_.toEPeruste)
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    rakenteet.find(_.diaarinumero == diaariNumero)
  }
}
