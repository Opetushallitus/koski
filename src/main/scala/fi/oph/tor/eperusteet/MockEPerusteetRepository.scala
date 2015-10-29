package fi.oph.tor.eperusteet

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

class MockEPerusteetRepository extends EPerusteetRepository {
  def findPerusteet(query: String): List[EPeruste] = {
    Json.readFile("src/main/resources/mockdata/eperusteet/auto.json").extract[EPerusteet].data.filter(_.nimi("fi").toLowerCase.contains(query.toLowerCase))
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    val all = findPerusteet("")
    all.filter(_.diaarinumero == diaarinumero)
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    findPerusteetByDiaarinumero(diaariNumero).headOption.flatMap { peruste =>
      Json.readFileIfExists("src/main/resources/mockdata/eperusteet/" + peruste.id + ".json").map(_.extract[EPerusteRakenne])
    }
  }
}
