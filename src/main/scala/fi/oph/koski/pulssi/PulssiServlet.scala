package fi.oph.koski.pulssi

import fi.oph.koski.cache.{Cache, CacheManager, Cached, CachingProxy}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{AuthenticationSupport, Unauthenticated, UserAuthenticationContext}
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotStatistics
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class PulssiServlet(val application: KoskiApplication) extends ApiServlet with NoCache with AuthenticationSupport {
  get("/") {
    publicStats ++ privateStats
  }

  private def publicStats = {
    Map(
      "opiskeluoikeudet" -> pulssi.opiskeluoikeudet,
      "metriikka" -> pulssi.metriikka,
      "oppilaitosMäärätTyypeittäin" -> pulssi.oppilaitosMäärätTyypeittäin
    )
  }

  private def privateStats = if (koskiSessionOption.exists(_.hasGlobalReadAccess)) {
    pulssi.sisäisetOpiskeluoikeusTiedot
  } else {
    Map()
  }

  private def pulssi = application.koskiPulssi
}

trait KoskiPulssi {
  def opiskeluoikeudet: Map[String, Any]
  def metriikka: Map[String, Any]
  def oppilaitosMäärätTyypeittäin: Seq[Map[String, Any]]
  def sisäisetOpiskeluoikeusTiedot: Map[String, Any]
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
}

object KoskiPulssi {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): KoskiPulssi with Cached = {
    CachingProxy[KoskiPulssi](
      Cache.cacheAllNoRefresh("KoskiPulssi", durationSeconds = 10, maxSize = 5),
      new KoskiStats(application)
    )
  }
}
