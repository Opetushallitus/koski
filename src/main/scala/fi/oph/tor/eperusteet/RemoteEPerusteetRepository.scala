package fi.oph.tor.eperusteet

import fi.oph.tor.http.Http

class RemoteEPerusteetRepository(ePerusteetRoot: String) extends EPerusteetRepository {
  private val http: Http = Http()

  def findPerusteet(query: String): EPerusteet = {
    http(ePerusteetRoot + "/api/perusteet?sivukoko=100&nimi=" + query)(Http.parseJson[EPerusteet])
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): EPerusteet = {
    http(ePerusteetRoot + "/api/perusteet?diaarinumero=" + diaarinumero)(Http.parseJson[EPerusteet])
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    http(ePerusteetRoot + s"/api/perusteet/diaari?diaarinumero=$diaariNumero")(Http.parseJsonOptional[EPerusteTunniste])
      .map(e => http(ePerusteetRoot + "/api/perusteet/" + e.id + "/kaikki")(Http.parseJson[EPerusteRakenne]))
  }
}
