package fi.oph.tor.eperusteet

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.http.Http
import fi.oph.tor.tutkinto._

class EPerusteetClient(ePerusteetRoot: String) extends TutkintoRepository {
  private val http: Http = Http()

  override def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto] = {
    ePerusteetToTutkinnot(http(ePerusteetRoot + "/api/perusteet?sivukoko=100&nimi=" + query)(Http.parseJson[EPerusteet]))
  }

  override def findByEPerusteDiaarinumero(diaarinumero: String) = {
    ePerusteetToTutkinnot(http(ePerusteetRoot + "/api/perusteet?diaarinumero=" + diaarinumero)(Http.parseJson[EPerusteet])).headOption
  }

  private def ePerusteetToTutkinnot(perusteet: EPerusteet) = {
    perusteet.data.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => Tutkinto(peruste.diaarinumero, koulutus.koulutuskoodiArvo, peruste.nimi.get("fi")))
    }
  }

  override def findPerusteRakenne(diaariNumero: String)(implicit arviointiAsteikot: ArviointiasteikkoRepository) = {
    http(ePerusteetRoot + s"/api/perusteet/diaari?diaarinumero=$diaariNumero")(Http.parseJsonOptional[EPerusteTunniste])
      .map(e => http(ePerusteetRoot + "/api/perusteet/" + e.id + "/kaikki")(Http.parseJson[EPerusteRakenne]))
      .map(EPerusteetTutkintoRakenne.convertRakenne)
  }
}

case class EPerusteet(data: List[EPeruste])
case class EPeruste(nimi: Map[String, String], diaarinumero: String, koulutukset: List[EPerusteKoulutus])
case class EPerusteKoulutus(nimi: Map[String, String], koulutuskoodiArvo: String)

case class EPerusteTunniste(id: String)