package fi.oph.tor.eperusteet

import fi.oph.tor.http.Http
import fi.oph.tor.http.Http._

class RemoteEPerusteetRepository(ePerusteetRoot: String) extends EPerusteetRepository {
  private val http: Http = Http(ePerusteetRoot)

  def findPerusteet(query: String): List[EPeruste] = {
    runTask(http(uri"/api/perusteet?sivukoko=100&nimi=${query}")(Http.parseJson[EPerusteet])).data
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    runTask(http(uri"/api/perusteet?diaarinumero=${diaarinumero}")(Http.parseJson[EPerusteet])).data
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    runTask(http(uri"/api/perusteet/diaari?diaarinumero=${diaariNumero}")(Http.parseJsonOptional[EPerusteTunniste]))
      .map(e => runTask(http(uri"/api/perusteet/${e.id}/kaikki")(Http.parseJson[EPerusteRakenne])))
  }
}
