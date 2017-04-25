package fi.oph.koski.pulssi

import fi.oph.koski.cache.{Cache, CacheManager, Cached, CachingProxy}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class PulssiServlet(pulssi: KoskiPulssi) extends ApiServlet with NoCache with Unauthenticated {
  get("/") {
    Map(
      "opiskeluoikeudet" -> pulssi.opiskeluoikeudet,
      "metriikka" -> pulssi.metriikka
    )
  }
}

trait KoskiPulssi {
  def opiskeluoikeudet: Map[String, Any]
  def metriikka: Map[String, Any]
}

class KoskiStats(application: KoskiApplication) extends KoskiPulssi {
  def opiskeluoikeudet: Map[String, Any] = application.perustiedotRepository.statistics
  def metriikka: Map[String, Any] = {
    Map(
      "saavutettavuus" -> application.prometheusRepository.koskiAvailability,
      "operaatiot" -> application.prometheusRepository.koskiMonthlyOperations
    )
  }
}

object KoskiPulssi {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): KoskiPulssi with Cached = {
    CachingProxy[KoskiPulssi](
      Cache.cacheAllNoRefresh("KoskiPulssi", durationSeconds = 10, maxSize = 5),
      new KoskiStats(application)
    )
  }
}
