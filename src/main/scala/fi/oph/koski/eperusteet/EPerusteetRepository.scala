package fi.oph.koski.eperusteet

import com.typesafe.config.Config
import fi.oph.koski.cache.CacheManager
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

trait EPerusteetRepository {
  def findPerusteet(query: String): List[EPeruste]

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste]

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPeruste]

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne]

  def findPerusteenYksilöintitiedot(diaariNumero: String): Option[EPerusteTunniste]

  def findLinkToEperusteetWeb(diaariNumero: String, lang: String): Option[String] = {
    val linkLang = if (webLanguages.contains(lang)) lang else webLanguages.head
    findPerusteenYksilöintitiedot(diaariNumero)
      .map(peruste => s"$webBaseUrl/#/${linkLang}/kooste/${peruste.id}")
  }

  protected val webLanguages = List("fi", "sv")

  protected def webBaseUrl: String
}

object EPerusteetRepository {
  def apply(config: Config)(implicit cacheInvalidator: CacheManager): EPerusteetRepository = {
    config.getString("eperusteet.url") match {
      case "mock" =>
        MockEPerusteetRepository
      case url =>
        new RemoteEPerusteetRepository(url, config.getString("eperusteet.baseUrl"))
    }
  }
}
