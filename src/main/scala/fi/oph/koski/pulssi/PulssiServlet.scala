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
      "operaatiot" -> pulssi.operaatiot,
      "kattavuus" -> pulssi.kattavuus
    )
  }
}

trait KoskiPulssi {
  def opiskeluoikeudet: Map[String, Any]
  def operaatiot: Seq[Map[String, Any]]
  def kattavuus: Map[String, Int]
}

class KoskiPulse(application: KoskiApplication) extends KoskiPulssi {
  def opiskeluoikeudet: Map[String, Any] = application.perustiedotRepository.statistics
  def operaatiot: Seq[Map[String, Any]] = application.prometheusRepository.auditLogMetrics
  def kattavuus: Map[String, Int] = {
    val yhteenvedot: Seq[TiedonsiirtoYhteenveto] = application.tiedonsiirtoService.yhteenveto(systemUser, Ascending("oppilaitos"))
    val koulutusmuodot: Seq[String] = yhteenvedot.flatMap { yhteenveto =>
      if (yhteenveto.siirretyt > 0) {
        application.organisaatioRepository.getOrganisaatioHierarkia(yhteenveto.oppilaitos.oid).toList.flatMap(_.oppilaitostyyppi).flatMap {
          case tyyppi if List(peruskoulut, peruskouluasteenErityiskoulut, perusJaLukioasteenKoulut).contains(tyyppi) => List("perusopetus")
          case tyyppi if List(lukio).contains(tyyppi) => List("lukio")
          case tyyppi if List(ammatillisetOppilaitokset, ammatillisetErityisoppilaitokset, ammatillisetErikoisoppilaitokset, ammatillisetAikuiskoulutusKeskukset).contains(tyyppi) => List("ammatillinenkoulutus")
          case _ => List("ammatillinenkoulutus", "perusopetus")
        }
      } else {
        Nil
      }
    }
    koulutusmuodot.groupBy(identity).map { case (x, xs) => (x, xs.length) }
  }
}

object KoskiPulssi {
  def apply(application: KoskiApplication)(implicit cm: CacheManager): KoskiPulssi with Cached = {
    CachingProxy[KoskiPulssi](
      Cache.cacheAllRefresh("KoskiPulssi", durationSeconds = 120, maxSize = 5),
      new KoskiPulse(application)
    )
  }
}
