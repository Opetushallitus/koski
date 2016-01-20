package fi.oph.tor.oppija

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db.{Futures, GlobalExecutionContext, PostgresDriverWithJsonSupport}
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.log.Loggable
import fi.oph.tor.schema.{FullHenkilö, Henkilö}

object MockOppijat {
  private val oppijat = new MockOppijat

  val eero = oppijat.oppija("esimerkki", "eero", "010101-123N")
  val eerola = oppijat.oppija("eerola", "jouni", "")
  val markkanen = oppijat.oppija("markkanen", "eero", "")
  val teija = oppijat.oppija("tekijä", "teija", "150995-914X")
  val tyhjä = oppijat.oppija("tyhjä", "tyhjä", "130196-961Y")
  val tero = oppijat.oppija("tunkkila-fagerlund", "tero petteri gustaf", "091095-9833")
  val presidentti = oppijat.oppija("Presidentti", "Tasavallan", "")

  def defaultOppijat = oppijat.getOppijat
}

class MockOppijat(private var oppijat: List[FullHenkilö] = Nil) {
  private var idCounter = oppijat.length

  def oppija(suku: String, etu: String, hetu: String): FullHenkilö = {
    val oppija = FullHenkilö(generateId(), hetu, etu, etu, suku)
    oppijat = oppija :: oppijat
    oppija
  }

  def getOppijat = oppijat

  private def generateId(): String = {
    idCounter = idCounter + 1
    "1.2.246.562.24.0000000000" + idCounter
  }
}

class MockOppijaRepository(db: Option[DB] = None) extends OppijaRepository with Futures with GlobalExecutionContext {
  private var oppijat = new MockOppijat(MockOppijat.defaultOppijat)

  override def findOppijat(query: String) = {
    if (query.toLowerCase.contains("error")) {
      throw new TestingException("Testing error handling")
    }
    oppijat.getOppijat.filter(searchString(_).contains(query))
  }

  override def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpStatus, Henkilö.Oid] = {
    if (sukunimi == "error") {
      throw new TestingException("Testing error handling")
    } else if (oppijat.getOppijat.find { o => (o.hetu == hetu) } .isDefined) {
      Left(HttpStatus.conflict("conflict"))
    } else {
      val newOppija = oppijat.oppija(sukunimi, etunimet, hetu)
      Right(newOppija.oid)
    }
  }

  private def searchString(oppija: FullHenkilö) = {
    oppija.toString.toUpperCase
  }


  override def resetFixtures {
    oppijat = new MockOppijat(MockOppijat.defaultOppijat)
  }

  def findFromDb(oid: String): Option[FullHenkilö] = {
    Some(FullHenkilö(oid, oid, oid, oid, oid))
  }

  def runQuery[E, U](fullQuery: PostgresDriverWithJsonSupport.api.Query[E, U, Seq]): Seq[U] = {
    db.toSeq.flatMap { db => await(db.run(fullQuery.result)) }
  }

  override def findByOid(id: String): Option[FullHenkilö] = {
    oppijat.getOppijat.filter {_.oid == id}.headOption.orElse(findFromDb(id))
  }

  override def findByOids(oids: List[String]): List[FullHenkilö] = oids.map(oid => findByOid(oid).get)

}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  override def toString = text
}