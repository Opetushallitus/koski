package fi.oph.tor.perftest

import java.util.Date

import fi.oph.tor.model.{Arviointi, Komoto, Suoritus}

object TestDataGenerator {
  def generoiSuorituksia(oppijaId: String, organisaatioId: String, prefix: String, levels: List[(String, Int)]): List[Suoritus] = levels match {
    case (levelName: String, count: Int) :: sublevels => (1 to count).map { num =>
      val komoId = prefix + levelName + "-" + num
      val komoto = Komoto(None, Some(komoId), Some("testidataa"), Some(komoId), Some(levelName), None, None, None)
      val arviointi = Some(Arviointi(None, "1-3", 2, Some("testidataa")))
      val osasuoritukset = generoiSuorituksia(oppijaId, organisaatioId, komoId + "-", sublevels)
      Suoritus(None, Some(new Date()), organisaatioId, organisaatioId, oppijaId, "suoritettu", Some("testidataa"), komoto, arviointi, osasuoritukset)
    }.toList
    case _ => Nil
  }
}
