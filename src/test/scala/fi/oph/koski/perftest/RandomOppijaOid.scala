package fi.oph.koski.perftest

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import org.scalatest.matchers.should.Matchers

import scala.util.Random

case class RandomOppijaOid(fetchCount: Int) extends KoskidevHttpSpecification with Matchers with Logging {
  val url: String = s"api/oppija/oids?pageNumber=0&pageSize=$fetchCount"
  lazy val oids = {
    logger.info(s"Fetching $fetchCount oids")
    get(url, headers = (authHeaders() ++ jsonContent)) {
      response.status should equal(200)
      JsonSerializer.parse[List[String]](body)
    }
  }

  lazy val oidIterator = {
    logger.info("Looping through " + oids.length + " oids")
    SynchronizedIterator(Iterator.continually(Random.shuffle(oids).iterator).flatten)
  }

  def nextOid = oidIterator.next()
}

case class SynchronizedIterator[A](i: Iterator[A]) extends Iterator[A] {
  override def hasNext: Boolean = synchronized(i.hasNext)

  override def next(): A = synchronized(i.next())
}
