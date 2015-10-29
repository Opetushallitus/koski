package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet.{EPerusteet, EPerusteetRepository, EPerusteetTutkintoRakenne}

class TutkintoRepository(eperusteet: EPerusteetRepository) {
  def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto] = {
    ePerusteetToTutkinnot(eperusteet.findPerusteet(query))
  }

  def findByEPerusteDiaarinumero(diaarinumero: String) = {
    ePerusteetToTutkinnot(eperusteet.findPerusteetByDiaarinumero(diaarinumero)).headOption
  }

  def ePerusteetToTutkinnot(perusteet: EPerusteet) = {
    perusteet.data.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => Tutkinto(peruste.diaarinumero, koulutus.koulutuskoodiArvo, peruste.nimi.get("fi")))
    }
  }

  def findPerusteRakenne(diaariNumero: String)(implicit arviointiAsteikot: ArviointiasteikkoRepository) = {
    eperusteet.findRakenne(diaariNumero)
      .map(EPerusteetTutkintoRakenne.convertRakenne)
  }
}