package fi.oph.koski.pulssi

import fi.oph.koski.cache.{Cache, CacheManager, Cached, CachingProxy}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.KäyttöoikeusTilasto
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedotStatistics, OpiskeluoikeusTilasto}

trait KoskiPulssi {
  def opiskeluoikeusTilasto: OpiskeluoikeusTilasto
  def metriikka: JulkinenMetriikka
  def oppilaitosMäärätTyypeittäin: Seq[Map[String, Any]]
  def oppijoidenMäärä: Int
  def käyttöoikeudet: KäyttöoikeusTilasto
  def metrics: KoskiMetriikka
}

class KoskiStats(application: KoskiApplication) extends KoskiPulssi {
  private val perustiedotStats = OpiskeluoikeudenPerustiedotStatistics(application.perustiedotIndex)

  def opiskeluoikeusTilasto: OpiskeluoikeusTilasto = perustiedotStats.statistics
  def metriikka: JulkinenMetriikka = metrics.toPublic
  def oppijoidenMäärä: Int = perustiedotStats.henkilöCount.getOrElse(0)
  def käyttöoikeudet: KäyttöoikeusTilasto = application.authenticationServiceClient.henkilötPerKäyttöoikeusryhmä
  def metrics: KoskiMetriikka = application.prometheusRepository.koskiMetrics

  def oppilaitosMäärätTyypeittäin: Seq[Map[String, Any]] = List(
    Map("koulutusmuoto" -> "Perusopetus", "määrä" -> 2941),
    Map("koulutusmuoto" -> "Lukiokoulutus", "määrä" -> 396),
    Map("koulutusmuoto" -> "Ammatillinen koulutus", "määrä" -> 208)
  )
}


object KoskiPulssi {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): KoskiPulssi with Cached = {
    CachingProxy[KoskiPulssi](
      Cache.cacheAllNoRefresh("KoskiPulssi", durationSeconds = 10 * 60, maxSize = 5),
      new KoskiStats(application)
    )
  }
}
