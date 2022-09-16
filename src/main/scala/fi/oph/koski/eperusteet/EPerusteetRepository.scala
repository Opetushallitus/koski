package fi.oph.koski.eperusteet

import com.typesafe.config.Config
import fi.oph.koski.cache.CacheManager
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering

import java.time.LocalDate

trait EPerusteetRepository {
  def findPerusteet(nimi: String): List[EPerusteRakenne]

  def findPerusteetByKoulutustyyppi(koulutustyypit: Set[Koulutustyyppi]): List[EPerusteRakenne]

  def findTarkatRakenteet(diaariNumero: String, päivä: Option[LocalDate]): List[EPerusteTarkkaRakenne]

  def findRakenteet(diaarinumero: String, päivä: Option[LocalDate]): List[EPerusteRakenne] = {
    findKaikkiRakenteet(diaarinumero)
      .filter(perusteVoimassa(päivä))
      // TODO: tämä sorttaus pitäisi kyllä tehdä vasta kälissä, missä sitä tarvitaan, ei täällä
      .sortBy(_.koulutusvienti)
  }

  def findKaikkiRakenteet(diaarinumero: String): List[EPerusteRakenne]

  def findPerusteenYksilöintitiedot(diaariNumero: String, päivä: Option[LocalDate]): List[EPerusteTunniste] =
    findKaikkiPerusteenYksilöintitiedot(diaariNumero)
      .filter(perusteVoimassa(päivä))

  def findKaikkiPerusteenYksilöintitiedot(diaariNumero: String): List[EPerusteTunniste]

  def findLinkToEperusteetWeb(diaariNumero: String, lang: String, päivä: LocalDate): Option[String] = {
    val linkLang = if (webLanguages.contains(lang)) lang else webLanguages.head
    findPerusteenYksilöintitiedot(diaariNumero, Some(päivä))
      .sortBy(_.luotu)(Ordering[Option[Long]]).reverse
      .headOption
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

  private def perusteVoimassa(päivä: Option[LocalDate])(peruste: EPerusteVoimassaololla): Boolean = {
    //TODO tarviiko huomoioida voimassaolon alkamista?
//    päivä.isEmpty || (peruste.voimassaOloAlkanut(päivä.get) && !peruste.siirtymäTaiVoimassaoloPäättynyt(päivä.get))
    päivä.isEmpty || !peruste.siirtymäTaiVoimassaoloPäättynyt(päivä.get)
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
