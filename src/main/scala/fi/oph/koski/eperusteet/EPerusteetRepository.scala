package fi.oph.koski.eperusteet

import com.typesafe.config.Config
import fi.oph.koski.cache.CacheManager
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

trait EPerusteetRepository {
  def findPerusteet(query: String): List[EPeruste]

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPeruste]

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne]

  def findUusinRakenne(diaarinumero: String): Option[EPerusteRakenne]

  def findPerusteenYksilöintitiedot(diaariNumero: String): Option[EPerusteTunniste]

  def findLinkToEperusteetWeb(diaariNumero: String, lang: String): Option[String] = {
    val linkLang = if (webLanguages.contains(lang)) lang else webLanguages.head
    findPerusteenYksilöintitiedot(diaariNumero)
      .map(peruste => {
        val betaEperusteKategoria = betaEperusteenTarvitsevatDiaarinumerot.find(
          _._2.contains(diaariNumero))
        if (betaEperusteKategoria.nonEmpty) {
          s"$webBaseUrl/beta/#/${linkLang}/${betaEperusteKategoria.get._1}/${peruste.id}"
        } else {
          s"$webBaseUrl/#/${linkLang}/kooste/${peruste.id}"
        }
      })
  }

  protected val betaEperusteenTarvitsevatDiaarinumerot = Map(
    "vapaasivistystyo" -> List("OPH-58-2021", "OPH-2984-2017", "1/011/2012", "OPH-123-2021"),
    "lukiokoulutus" -> List("OPH-2267-2019", "OPH-4958-2020", "OPH-2263-2019"))

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
