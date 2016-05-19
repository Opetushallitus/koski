package fi.oph.tor.oppija

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db.{Tables, Futures, GlobalExecutionContext, PostgresDriverWithJsonSupport}
import fi.oph.tor.henkilo.Hetu
import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.log.{Logging, Loggable}
import fi.oph.tor.schema._

object MockOppijat {
  private val oppijat = new MockOppijat

  val eero = oppijat.oppija("Esimerkki", "Eero", "010101-123N")
  val eerola = oppijat.oppija("Eerola", "Jouni", "")
  val markkanen = oppijat.oppija("Markkanen", "Eero", "")
  val teija = oppijat.oppija("Tekijä", "Teija", "150995-914X")
  val tyhjä = oppijat.oppija("Tyhjä", "Tyhjä", "130196-961Y")
  val tero = oppijat.oppija("Tunkkila-Fagerlund", "Tero Petteri Gustaf", "091095-9833")
  val presidentti = oppijat.oppija("Presidentti", "Tasavallan", "")
  val koululainen = oppijat.oppija("Koululainen", "Kaisa", "110496-926Y")
  val lukiolainen = oppijat.oppija("Lukiolainen", "Liisa", "110496-9369")
  val ammattilainen = oppijat.oppija("Ammattilainen", "Aarne", "120496-949B")
  val dippainssi = oppijat.oppija("Dippainssi", "Dilbert", "290492-9455")
  val korkeakoululainen = oppijat.oppija("Korkeakoululainen", "Kikka", "010675-9981")
  val amkValmistunut = oppijat.oppija("Amis", "Valmis", "101291-954C")
  val amkKesken = oppijat.oppija("Amiskesken", "Jalmari", "100292-980D")
  val amkKeskeytynyt = oppijat.oppija("Pudokas", "Valtteri", "100193-948U")

  def defaultOppijat = oppijat.getOppijat
}

class MockOppijat(private var oppijat: List[TaydellisetHenkilötiedot] = Nil) extends Logging {
  private var idCounter = oppijat.length
  val äidinkieli: Some[Koodistokoodiviite] = Some(Koodistokoodiviite("FI", None, "kieli", None))

  def oppija(suku: String, etu: String, hetu: String): TaydellisetHenkilötiedot = {
    val oppija = TaydellisetHenkilötiedot(generateId(), hetu, etu, etu, suku, äidinkieli, None)
    oppijat = oppija :: oppijat
    oppija
  }

  def getOppijat = oppijat

  private def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    "1.2.246.562.24.0000000000" + idCounter
  }
}

case class MockOppijaRepository(initialOppijat: List[TaydellisetHenkilötiedot] = MockOppijat.defaultOppijat, db: Option[DB] = None) extends OppijaRepository with Futures {
  private var oppijat = new MockOppijat(initialOppijat)

  override def findOppijat(query: String) = {
    if (query.toLowerCase.contains("error")) {
      throw new TestingException("Testing error handling")
    }
    oppijat.getOppijat.filter(searchString(_).contains(query))
  }

  def addOppija(suku: String, etu: String, hetu: String): TaydellisetHenkilötiedot = {
    oppijat.oppija(suku, etu, hetu)
  }

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, Henkilö.Oid] =  {
    def oidFrom(oppijat: List[TaydellisetHenkilötiedot]): Either[HttpStatus, Henkilö.Oid] = {
      oppijat match {
        case List(oppija) => Right(oppija.oid)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(TorErrorCategory.internalError())
      }
    }
    val UusiHenkilö(hetu, etunimet, kutsumanimi, sukunimi) = henkilö
    Hetu.validate(hetu).right.flatMap { hetu =>
      create(hetu, etunimet, kutsumanimi, sukunimi).left.flatMap { case HttpStatus(409, _) =>
        oidFrom(findOppijat(hetu))
      }
    }
  }

  private def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpStatus, Henkilö.Oid] = {
    if (sukunimi == "error") {
      throw new TestingException("Testing error handling")
    } else if (oppijat.getOppijat.find { o => (o.hetu == hetu) } .isDefined) {
      Left(TorErrorCategory.conflict.hetu("conflict"))
    } else {
      val newOppija = oppijat.oppija(sukunimi, etunimet, hetu)
      Right(newOppija.oid)
    }
  }

  private def searchString(oppija: TaydellisetHenkilötiedot) = {
    oppija.toString.toUpperCase
  }


  override def resetFixtures {
    oppijat = new MockOppijat(MockOppijat.defaultOppijat)
  }

  def findFromDb(oid: String): Option[TaydellisetHenkilötiedot] = {
    import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
    runQuery(Tables.OpiskeluOikeudet.filter(_.oppijaOid === oid)).headOption.map { oppijaRow =>
      TaydellisetHenkilötiedot(oid, oid, oid, oid, oid, oppijat.äidinkieli, None)
    }
  }

  def runQuery[E, U](fullQuery: PostgresDriverWithJsonSupport.api.Query[E, U, Seq]): Seq[U] = {
    db.toSeq.flatMap { db => await(db.run(fullQuery.result)) }
  }

  override def findByOid(id: String): Option[TaydellisetHenkilötiedot] = {
    oppijat.getOppijat.filter {_.oid == id}.headOption.orElse(findFromDb(id))
  }

  override def findByOids(oids: List[String]): List[TaydellisetHenkilötiedot] = oids.map(oid => findByOid(oid).get)

}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  def logString = text
}