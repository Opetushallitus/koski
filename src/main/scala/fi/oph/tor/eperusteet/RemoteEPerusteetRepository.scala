package fi.oph.tor.eperusteet

import fi.oph.tor.http.Http
import fi.oph.tor.http.Http.runTask

class RemoteEPerusteetRepository(ePerusteetRoot: String) extends EPerusteetRepository {
  private val http: Http = Http(ePerusteetRoot)

  def findPerusteet(query: String): List[EPeruste] = {
    runTask(http("/api/perusteet?sivukoko=100&nimi=" + query)(Http.parseJson[EPerusteet])).data
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    runTask(http("/api/perusteet?diaarinumero=" + diaarinumero)(Http.parseJson[EPerusteet])).data
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    runTask(http(s"/api/perusteet/diaari?diaarinumero=$diaariNumero")(Http.parseJsonOptional[EPerusteTunniste]))
      .map(e => runTask(http("/api/perusteet/" + e.id + "/kaikki")(Http.parseJson[EPerusteRakenne])))
  }
}
