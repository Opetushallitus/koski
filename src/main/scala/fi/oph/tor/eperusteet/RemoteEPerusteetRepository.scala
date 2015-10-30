package fi.oph.tor.eperusteet

import fi.oph.tor.http.Http
import fi.oph.tor.util.Timed
import fi.vm.sade.utils.Timer

class RemoteEPerusteetRepository(ePerusteetRoot: String) extends EPerusteetRepository with Timed {
  private val http: Http = Http()

  def findPerusteet(query: String) = {
    http(ePerusteetRoot + "/api/perusteet?sivukoko=100&nimi=" + query)(Http.parseJson[EPerusteet]).data
  }

  def findPerusteetByDiaarinumero(diaarinumero: String) = {
    http(ePerusteetRoot + "/api/perusteet?diaarinumero=" + diaarinumero)(Http.parseJson[EPerusteet]).data
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = timed("findRakenne") {
    http(ePerusteetRoot + s"/api/perusteet/diaari?diaarinumero=$diaariNumero")(Http.parseJsonOptional[EPerusteTunniste])
      .map(e => http(ePerusteetRoot + "/api/perusteet/" + e.id + "/kaikki")(Http.parseJson[EPerusteRakenne]))
  }
}
