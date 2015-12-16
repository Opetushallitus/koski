package fi.oph.tor.eperusteet

import fi.oph.tor.http.Http

class RemoteEPerusteetRepository(ePerusteetRoot: String) extends EPerusteetRepository {
  private val http: Http = Http(ePerusteetRoot)

  def findPerusteet(query: String) = {
    http("/api/perusteet?sivukoko=100&nimi=" + query)(Http.parseJson[EPerusteet]).run.data
  }

  def findPerusteetByDiaarinumero(diaarinumero: String) = {
    http("/api/perusteet?diaarinumero=" + diaarinumero)(Http.parseJson[EPerusteet]).run.data
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    http(s"/api/perusteet/diaari?diaarinumero=$diaariNumero")(Http.parseJsonOptional[EPerusteTunniste]).run
      .map(e => http("/api/perusteet/" + e.id + "/kaikki")(Http.parseJson[EPerusteRakenne]).run)
  }
}
