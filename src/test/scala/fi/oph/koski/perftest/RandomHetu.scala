package fi.oph.koski.perftest

import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.integrationtest.KoskidevHttpSpecification

class RandomHetu extends KoskidevHttpSpecification {
  def nextHetu = hetut.synchronized { hetut.next }
  private lazy val hetut: Iterator[String] = {
    Iterator.continually({
      println("Haetaan hetuja...")
      EasyHttp.getJson[List[String]]("https://www.telepartikkeli.net/tunnusgeneraattori/api/generoi/hetu/1000").iterator
    }).flatten.filter(Hetu.validate(_, acceptSynthetic = false).isRight)
  }
}

