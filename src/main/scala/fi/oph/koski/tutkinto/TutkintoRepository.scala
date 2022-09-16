package fi.oph.koski.tutkinto

import fi.oph.koski.cache.{CacheManager, CachingProxy, RefreshingCache}
import fi.oph.koski.eperusteet._
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.LocalizedString

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[TutkintoPeruste]

  def findPerusteRakenteet(diaariNumero: String, päivä: Option[LocalDate]): List[TutkintoRakenne]

  def findUusinPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne]
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

  def findPerusteRakenteet(diaariNumero: String, päivä: Option[LocalDate]): List[TutkintoRakenne] = {
    eperusteet.findTarkatRakenteet(diaariNumero, päivä)
      .sortBy(_.luotu)(Ordering[Option[Long]]).reverse
      .map(rakenne => EPerusteetTutkintoRakenneConverter.convertRakenne(rakenne)(koodistoPalvelu))
  }

  def findUusinPerusteRakenne(diaariNumero: String): Option[TutkintoRakenne] = {
    eperusteet.findTarkatRakenteet(diaariNumero, None)
      .sortBy(_.luotu)(Ordering[Option[Long]]).reverse
      .headOption
      .map(rakenne => EPerusteetTutkintoRakenneConverter.convertRakenne(rakenne)(koodistoPalvelu))
  }
}

