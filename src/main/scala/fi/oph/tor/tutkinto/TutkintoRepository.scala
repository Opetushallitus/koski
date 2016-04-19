package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet._
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.localization.LocalizedString

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste]

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne]
}

object TutkintoRepository {
  def apply(eperusteet: EPerusteetRepository, arviointiAsteikot: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoViitePalvelu): TutkintoRepository = new TutkintoRepositoryImpl(eperusteet, arviointiAsteikot, koodistoPalvelu)
}

class TutkintoRepositoryImpl(eperusteet: EPerusteetRepository, arviointiAsteikot: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoViitePalvelu) extends TutkintoRepository{
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste] = {
    eperusteet.findPerusteet(query) flatMap { peruste =>
      peruste.koulutukset.map(koulutus => TutkintoPeruste(peruste.diaarinumero, koulutus.koulutuskoodiArvo, LocalizedString(peruste.nimi)))
    }
  }

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne] = {
    eperusteet.findRakenne(diaariNumero)
      .map(rakenne => EPerusteetTutkintoRakenneConverter.convertRakenne(rakenne)(arviointiAsteikot, koodistoPalvelu))
  }
}

