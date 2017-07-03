package fi.oph.koski.pulssi

import fi.oph.koski.cache.{Cache, CacheManager, Cached, CachingProxy}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.KäyttöoikeusTilasto
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedotStatistics, OpiskeluoikeusTilasto}

trait KoskiPulssi {
  def opiskeluoikeusTilasto: OpiskeluoikeusTilasto
  def metriikka: JulkinenMetriikka
  def oppilaitosMäärät: OppilaitosMäärät
  def oppijoidenMäärä: Int
  def käyttöoikeudet: KäyttöoikeusTilasto
  def metrics: KoskiMetriikka
}

class KoskiStats(application: KoskiApplication) extends KoskiPulssi {
  private val perustiedotStats = OpiskeluoikeudenPerustiedotStatistics(application.koskiElasticSearchIndex)

  def opiskeluoikeusTilasto: OpiskeluoikeusTilasto = perustiedotStats.statistics
  def metriikka: JulkinenMetriikka = metrics.toPublic
  def oppijoidenMäärä: Int = perustiedotStats.henkilöCount.getOrElse(0)
  def käyttöoikeudet: KäyttöoikeusTilasto = application.authenticationServiceClient.henkilötPerKäyttöoikeusryhmä
  def metrics: KoskiMetriikka = application.prometheusRepository.koskiMetrics

  def oppilaitosMäärät = OppilaitosMäärät(Map(
    "Perusopetus" -> 2941,
    "Lukiokoulutus" -> 396,
    "Ammatillinen koulutus" -> 208
  ))
}

object KoskiPulssi {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): KoskiPulssi with Cached = {
    CachingProxy[KoskiPulssi](
      Cache.cacheAllNoRefresh("KoskiPulssi", durationSeconds = 10 * 60, maxSize = 5),
      new KoskiStats(application)
    )
  }
}

case class OppilaitosMäärät(koulutusmuodoittain: Map[String, Int]) {
  def yhteensä: Int = koulutusmuodoittain.values.sum
}
