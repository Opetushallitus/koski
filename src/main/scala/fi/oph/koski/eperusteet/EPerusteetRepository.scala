package fi.oph.koski.eperusteet

import com.typesafe.config.Config
import fi.oph.koski.cache.CacheManager

trait EPerusteetRepository {
  def findPerusteet(query: String): List[EPeruste]

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste]

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne]

  def findPerusteenYksilöintitiedot(diaariNumero: String): Option[EPerusteTunniste]
}

object EPerusteetRepository {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager): EPerusteetRepository = {
    config.getString("eperusteet.url") match {
      case "mock" =>
        MockEPerusteetRepository
      case url =>
        new RemoteEPerusteetRepository(url)
    }
  }
}