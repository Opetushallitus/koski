package fi.oph.koski.pulssi

import fi.oph.koski.cache._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedotStatistics, OpiskeluoikeusTilasto}

import scala.concurrent.duration._


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
  def käyttöoikeudet: KäyttöoikeusTilasto = {
    val ryhmät = application.opintopolkuHenkilöFacade.getKäyttöikeusRyhmät
    KäyttöoikeusTilasto(
      ryhmät.values.flatten.toList.distinct.size,
      ryhmät.map { case (x, y) => (x, y.size) }
    )
  }
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
      ExpiringCache("KoskiPulssi", 10 minutes, maxSize = 5),
      new KoskiStats(application)
    )
  }
}

case class OppilaitosMäärät(koulutusmuodoittain: Map[String, Int]) {
  def yhteensä: Int = koulutusmuodoittain.values.sum
}

case class KäyttöoikeusTilasto(kokonaismäärä: Int, ryhmienMäärät: Map[String, Int])