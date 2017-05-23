package fi.oph.koski.pulssi

import fi.oph.koski.cache.{Cache, CacheManager, Cached, CachingProxy}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotStatistics

trait KoskiPulssi {
  def opiskeluoikeudet: Map[String, Any]
  def metriikka: Map[String, Any]
  def oppilaitosMäärätTyypeittäin: Seq[Map[String, Any]]
  def sisäisetOpiskeluoikeusTiedot: Map[String, Any]
  def käyttöoikeudet: Map[String, Any]
}

class KoskiStats(application: KoskiApplication) extends KoskiPulssi {
  private val perustiedotStats = OpiskeluoikeudenPerustiedotStatistics(application.perustiedotIndex)

  def opiskeluoikeudet: Map[String, Any] = perustiedotStats.statistics

  def metriikka: Map[String, Any] = {
    Map(
      "saavutettavuus" -> application.prometheusRepository.koskiAvailability,
      "operaatiot" -> application.prometheusRepository.koskiMonthlyOperations
    )
  }

  def oppilaitosMäärätTyypeittäin: Seq[Map[String, Any]] = List(
    Map("koulutusmuoto" -> "Perusopetus", "määrä" -> 2941),
    Map("koulutusmuoto" -> "Lukiokoulutus", "määrä" -> 396),
    Map("koulutusmuoto" -> "Ammatillinen koulutus", "määrä" -> 208)
  )

  def sisäisetOpiskeluoikeusTiedot: Map[String, Any] = perustiedotStats.privateStatistics

  def käyttöoikeudet: Map[String, Any] = {
    val kokonaismäärä = application.authenticationServiceClient.henkilötPerKäyttöoikeusryhmä.values.flatten.toList.distinct.size
    val käyttöoikeusmäärät = application.authenticationServiceClient.henkilötPerKäyttöoikeusryhmä.map { case (x, y) => (x, y.size) }
    Map(
      "kokonaismäärä" -> kokonaismäärä,
      "käyttöoikeusmäärät" -> käyttöoikeusmäärät
    )
  }
}

object KoskiPulssi {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): KoskiPulssi with Cached = {
    CachingProxy[KoskiPulssi](
      Cache.cacheAllNoRefresh("KoskiPulssi", durationSeconds = 10 * 60, maxSize = 5),
      new KoskiStats(application)
    )
  }
}
