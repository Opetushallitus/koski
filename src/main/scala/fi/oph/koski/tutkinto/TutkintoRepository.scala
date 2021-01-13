package fi.oph.koski.tutkinto

import fi.oph.common.schema.LocalizedString
import fi.oph.koski.cache.{CacheManager, CachingProxy, RefreshingCache}
import fi.oph.koski.eperusteet._
import fi.oph.koski.koodisto.KoodistoViitePalvelu

import scala.concurrent.duration._

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste]

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne]
}

object TutkintoRepository {
  def apply(eperusteet: EPerusteetRepository, koodistoPalvelu: KoodistoViitePalvelu)(implicit cacheInvalidator: CacheManager): TutkintoRepository =
    CachingProxy(RefreshingCache("TutkintoRepository", 1.hour, 100), new TutkintoRepositoryImpl(eperusteet, koodistoPalvelu).asInstanceOf[TutkintoRepository])
}

class TutkintoRepositoryImpl(eperusteet: EPerusteetRepository, koodistoPalvelu: KoodistoViitePalvelu) extends TutkintoRepository{
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste] = {
    eperusteet.findPerusteet(query) flatMap { peruste =>
      peruste.koulutukset.map(koulutus => TutkintoPeruste(peruste.diaarinumero, koulutus.koulutuskoodiArvo, LocalizedString.sanitize(peruste.nimi)))
    }
  }

  def findPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne] = {
    eperusteet.findRakenne(diaariNumero)
      .map(rakenne => EPerusteetTutkintoRakenneConverter.convertRakenne(rakenne)(koodistoPalvelu))
  }
}

