package fi.oph.tor.eperusteet

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

class MockEPerusteetRepository extends EPerusteetRepository {
  def findPerusteet(query: String): EPerusteet = {
    Json.readFile("src/main/resources/mockdata/eperusteet/auto.json").extract[EPerusteet]
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): EPerusteet = {
    val all: EPerusteet = findPerusteet("")
    all.copy(data = all.data.filter(_.diaarinumero == diaarinumero))
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    findPerusteetByDiaarinumero(diaariNumero).data.headOption.map { peruste =>
      Json.readFile("src/main/resources/mockdata/eperusteet/" + peruste.id + ".json").extract[EPerusteRakenne]
    }
  }
}
