package fi.oph.tor.eperusteet

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

class MockEPerusteetRepository extends EPerusteetRepository {
  def findPerusteet(query: String): List[EPeruste] = {
    Json.readFile("src/main/resources/mockdata/eperusteet/auto.json").extract[EPerusteet].data
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    val all = findPerusteet("")
    all.filter(_.diaarinumero == diaarinumero)
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    findPerusteetByDiaarinumero(diaariNumero).headOption.map { peruste =>
      Json.readFile("src/main/resources/mockdata/eperusteet/" + peruste.id + ".json").extract[EPerusteRakenne]
    }
  }
}
