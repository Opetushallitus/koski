package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet.{EPerusteet, EPerusteetClient, EPerusteetTutkintoRakenne}

class EPerusteetTutkintoRepository(ePerusteetRoot: String) extends TutkintoRepository {
  private val client = new EPerusteetClient(ePerusteetRoot)

  override def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto] = {
    ePerusteetToTutkinnot(client.findPerusteet(query))
  }

  override def findByEPerusteDiaarinumero(diaarinumero: String) = {
    ePerusteetToTutkinnot(client.findPerusteetByDiaarinumero(diaarinumero)).headOption
  }

  private def ePerusteetToTutkinnot(perusteet: EPerusteet) = {
    perusteet.data.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => Tutkinto(peruste.diaarinumero, koulutus.koulutuskoodiArvo, peruste.nimi.get("fi")))
    }
  }

  override def findPerusteRakenne(diaariNumero: String)(implicit arviointiAsteikot: ArviointiasteikkoRepository) = {
    client.findRakenne(diaariNumero)
      .map(EPerusteetTutkintoRakenne.convertRakenne)
  }
}