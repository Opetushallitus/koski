package fi.oph.koski.tutkinto

import fi.oph.koski.arvosana.ArviointiasteikkoRepository
import fi.oph.koski.cache.Cache._
import fi.oph.koski.cache.{CacheManager, CachingProxy}
import fi.oph.koski.eperusteet._
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedString

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste]

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne]
}

object TutkintoRepository {
  def apply(eperusteet: EPerusteetRepository, arviointiAsteikot: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager): TutkintoRepository =
    CachingProxy(cacheAllRefresh("TutkintoRepository", 3600, 100), new TutkintoRepositoryImpl(eperusteet, arviointiAsteikot, koodistoPalvelu).asInstanceOf[TutkintoRepository])
}

class TutkintoRepositoryImpl(eperusteet: EPerusteetRepository, arviointiAsteikot: ArviointiasteikkoRepository, koodistoPalvelu: KoodistoViitePalvelu) extends TutkintoRepository{
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste] = {
    eperusteet.findPerusteet(query) flatMap { peruste =>
      peruste.koulutukset.map(koulutus => TutkintoPeruste(peruste.diaarinumero, koulutus.koulutuskoodiArvo, LocalizedString.sanitize(peruste.nimi)))
    }
  }

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne] = {
    eperusteet.findRakenne(diaariNumero)
      .map(rakenne => EPerusteetTutkintoRakenneConverter.convertRakenne(rakenne)(arviointiAsteikot, koodistoPalvelu))
  }
}

