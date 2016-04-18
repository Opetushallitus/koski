package fi.oph.tor.eperusteet

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

class MockEPerusteetRepository extends EPerusteetRepository {
  lazy val rakenteet: List[EPerusteRakenne] = List("612", "33827", "1013059", "rakenne-perusopetus").map { id =>
    Json.readFile("src/main/resources/mockdata/eperusteet/" + id + ".json").extract[EPerusteRakenne]
  }

  def findPerusteet(query: String): List[EPeruste] = {
    // Hakee aina samoilla kriteereillä "auto"
    Json.readFile("src/main/resources/mockdata/eperusteet/auto.json").extract[EPerusteet].data.filter(_.nimi("fi").toLowerCase.contains(query.toLowerCase))
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    rakenteet.filter(_.diaarinumero == diaarinumero).map(_.toEPeruste)
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    rakenteet.find(_.diaarinumero == diaariNumero)
  }
}
