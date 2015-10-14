package fi.oph.tor.tutkinto

import fi.oph.tor.http.Http

class EPerusteetTutkintoRepository(ePerusteetRoot: String) extends TutkintoRepository {
  private val http: Http = Http()

  override def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto] = {
    ePerusteetToTutkinnot(http(ePerusteetRoot + "/api/perusteet?sivukoko=100&nimi=" + query)(Http.parseJson[EPerusteet]))
  }

  override def findByEPerusteDiaarinumero(diaarinumero: String) = {
    ePerusteetToTutkinnot(http(ePerusteetRoot + "/api/perusteet?diaarinumero=" + diaarinumero)(Http.parseJson[EPerusteet])).headOption
  }

  private def ePerusteetToTutkinnot(perusteet: EPerusteet) = {
    perusteet.data.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => Tutkinto(koulutus.nimi("fi"), peruste.diaarinumero, koulutus.koulutuskoodiArvo))
    }
  }
}

case class EPerusteet(data: List[EPeruste])
case class EPeruste(diaarinumero: String, koulutukset: List[EPerusteKoulutus])
case class EPerusteKoulutus(nimi: Map[String, String], koulutuskoodiArvo: String)
