package fi.oph.koski.perftest

import java.time.LocalDate

import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.integrationtest.KoskidevHttpSpecification

class RandomHetu(lahtienVuodesta: Int) extends KoskidevHttpSpecification {
  def nextHetu = hetut.synchronized { hetut.next }
  private lazy val hetut: Iterator[String] = {
    Iterator.continually({
      println("Haetaan hetuja...")
      EasyHttp.getJson[List[String]]("https://www.telepartikkeli.net/tunnusgeneraattori/api/generoi/hetu/500").iterator
    }).flatten.filter(Hetu.validate(_, acceptSynthetic = false).isRight).filter(vuodenJalkeen(_, lahtienVuodesta))
  }

  def vuodenJalkeen(hetu: String, year: Int): Boolean = {
    Hetu.toBirthday(hetu).getOrElse(LocalDate.now).isAfter(LocalDate.of(year, 1, 1))
  }
}

