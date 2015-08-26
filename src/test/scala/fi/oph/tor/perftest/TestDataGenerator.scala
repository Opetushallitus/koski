package fi.oph.tor.perftest

import java.util.Date

import fi.oph.tor.model.{Arviointi, Komoto, Suoritus}
import scala.util.Random

object TestDataGenerator {
  private val random = new Random()

  def generoiSuorituksia(oppijoita: Int, organisaatioita: Int = 100, levels: List[(String, Int)]): Iterator[Suoritus] ={
    LazyRange(1, oppijoita).iterator.map("oppija-" + _).flatMap { oppijaId =>
      val organisaatioId = "organisaatio-" + (random.nextInt(organisaatioita) + 1)
      generoiSuorituksia(oppijaId, organisaatioId , "", levels.toIterable)
    }
  }

  def generoiSuorituksia(oppijaId: String, organisaatioId: String, prefix: String, levels: Iterable[(String, Int)]): Iterator[Suoritus] = levels match {
    case (levelName: String, count: Int) :: sublevels => LazyRange(1, count).iterator.map { num =>
      val komoId = prefix + levelName + "-" + num
      val komoto = Komoto(None, Some(komoId), Some("testidataa"), Some(komoId), Some(levelName), None, None, None)
      val arviointi = Some(Arviointi(None, "1-3", 2, Some("testidataa")))
      val osasuoritukset = generoiSuorituksia(oppijaId, organisaatioId, komoId + "-", sublevels)
      Suoritus(None, Some(new Date()), organisaatioId, organisaatioId, oppijaId, "suoritettu", Some("testidataa"), komoto, arviointi, osasuoritukset.toList)
    }
    case _ => Iterator.empty
  }
}

case class LazyRange(start: Int, end: Int) extends Iterable[Int] {
  override def iterator = new Iterator[Int] {
    var index = start

    override def hasNext = { index <= end }

    override def next = {
      index = index + 1
      index - 1
    }
  }
}