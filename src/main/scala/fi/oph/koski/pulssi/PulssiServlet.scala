package fi.oph.koski.pulssi

import fi.oph.koski.cache.{Cache, CacheManager, Cached, CachingProxy}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession.systemUser
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.organisaatio.Oppilaitostyyppi._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.tiedonsiirto.TiedonsiirtoYhteenveto
import fi.oph.koski.util.SortOrder.Ascending

class PulssiServlet(pulssi: KoskiPulssi) extends ApiServlet with NoCache with Unauthenticated {
  get("/") {
    // TODO: get stuff from prometheus
    Map(
      "opiskeluoikeudet" -> pulssi.opiskeluoikeudet,
      "operaatiot" -> pulssi.operaatiot
    )
  }
}

trait KoskiPulssi {
  def opiskeluoikeudet: Map[String, Any]
  def operaatiot: Seq[Map[String, Any]]
}

class KoskiPulse(application: KoskiApplication) extends KoskiPulssi {
  def opiskeluoikeudet: Map[String, Any] = application.perustiedotRepository.statistics
  def operaatiot: Seq[Map[String, Any]] = application.prometheusRepository.auditLogMetrics
}

object KoskiPulssi {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): KoskiPulssi with Cached = {
    CachingProxy[KoskiPulssi](
      Cache.cacheAllNoRefresh("KoskiPulssi", durationSeconds = 10, maxSize = 5),
      new KoskiPulse(application)
    )
  }
}
