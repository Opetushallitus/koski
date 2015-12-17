package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet._
import fi.oph.tor.koodisto.KoodistoPalvelu

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste]

  def findByEPerusteDiaarinumero(diaarinumero: String): Option[TutkintoPeruste]

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne]
}

object TutkintoRepository {
  def apply(eperusteet: EPerusteetRepository, arviointiAsteikot: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoPalvelu): TutkintoRepository = new TutkintoRepositoryImpl(eperusteet, arviointiAsteikot, koodistoPalvelu)
}

class TutkintoRepositoryImpl(eperusteet: EPerusteetRepository, arviointiAsteikot: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoPalvelu) extends TutkintoRepository{
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste] = {
    ePerusteetToTutkinnot(eperusteet.findPerusteet(query))
  }

  def findByEPerusteDiaarinumero(diaarinumero: String): Option[TutkintoPeruste] = {
    ePerusteetToTutkinnot(eperusteet.findPerusteetByDiaarinumero(diaarinumero)).headOption
  }

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne] = {
    eperusteet.findRakenne(diaariNumero)
      .map(rakenne => EPerusteetTutkintoRakenneConverter.convertRakenne(rakenne)(arviointiAsteikot, koodistoPalvelu))
  }

  private def ePerusteetToTutkinnot(perusteet: List[EPeruste]) = {
    perusteet.flatMap { peruste =>
      peruste.koulutukset.map(koulutus => TutkintoPeruste(peruste.diaarinumero, koulutus.koulutuskoodiArvo, peruste.nimi.get("fi")))
    }
  }
}

