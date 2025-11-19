package fi.oph.koski.pulssi

import cats.effect.IO
import fi.oph.koski.cache._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.Http
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.perustiedot.OpiskeluoikeusTilasto
import fi.oph.koski.tiedonsiirto.TiedonsiirtoTilasto
import fi.oph.koski.userdirectory.KäyttöoikeusServiceClient

import scala.concurrent.duration.DurationInt
import cats.syntax.parallel._


trait KoskiPulssi {
  def opiskeluoikeusTilasto: OpiskeluoikeusTilasto
  def tiedonsiirtoTilasto: TiedonsiirtoTilasto
  def metriikka: JulkinenMetriikka
  def oppijoidenMäärä: Int
  def käyttöoikeudet: KäyttöoikeusTilasto
  def metrics: KoskiMetriikka
}

class KoskiStats(application: KoskiApplication) extends KoskiPulssi {
  private val perustiedotStats = application.perustiedotIndexer.statistics()
  private val tiedonsiirtoStats = application.tiedonsiirtoService.statistics()

  def opiskeluoikeusTilasto: OpiskeluoikeusTilasto = perustiedotStats.statistics
  def tiedonsiirtoTilasto: TiedonsiirtoTilasto = tiedonsiirtoStats.statistics
  def metriikka: JulkinenMetriikka = metrics.toPublic
  def oppijoidenMäärä: Int = perustiedotStats.henkilöCount.getOrElse(0)
  def käyttöoikeudet: KäyttöoikeusTilasto = {
    val ryhmät: Map[String, List[String]] = if (!application.fixtureCreator.shouldUseFixtures) {
      val client = KäyttöoikeusServiceClient(application.config)

      Http.runIO(client.findKäyttöoikeusryhmät.flatMap { ryhmät =>
        ryhmät
          .parTraverse { ryhmä =>
            client.findKäyttöoikeusRyhmänHenkilöt(ryhmä.id).map(henkilöOids => (ryhmä.fi, henkilöOids))
          }
      }.map(_.toMap))

    } else {
      MockUsers.users.flatMap(u => u.käyttöoikeusRyhmät.map(ko => (ko, u.ldapUser.oid))).groupBy(_._1).view.mapValues(_.map(_._2)).toMap
    }

    KäyttöoikeusTilasto(
      ryhmät.values.flatten.toList.distinct.size,
      ryhmät.map { case (x, y) => (x, y.size) }
    )
  }

  def metrics: KoskiMetriikka = application.prometheusRepository.koskiMetrics

}

object KoskiPulssi {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): KoskiPulssi with Cached = {
    CachingProxy[KoskiPulssi](
      ExpiringCache("KoskiPulssi", 10.minutes, maxSize = 5),
      new KoskiStats(application)
    )
  }
}

case class KäyttöoikeusTilasto(kokonaismäärä: Int, ryhmienMäärät: Map[String, Int])
