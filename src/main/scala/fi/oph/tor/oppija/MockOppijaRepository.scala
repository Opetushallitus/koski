package fi.oph.tor.oppija

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.TorDatabase.DB
import fi.oph.tor.db.{Tables, Futures, GlobalExecutionContext, PostgresDriverWithJsonSupport}
import fi.oph.tor.henkilo.Hetu
import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.log.{Logging, Loggable}
import fi.oph.tor.schema.{NewHenkilö, KoodistoKoodiViite, FullHenkilö, Henkilö}

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

class MockOppijat(private var oppijat: List[FullHenkilö] = Nil) extends Logging {
  private var idCounter = oppijat.length
  val äidinkieli: Some[KoodistoKoodiViite] = Some(KoodistoKoodiViite("FI", None, "kieli", None))

  def oppija(suku: String, etu: String, hetu: String): FullHenkilö = {
    val oppija = FullHenkilö(generateId(), hetu, etu, etu, suku, äidinkieli, None)
    logger.info("Oppija added: " + oppija)
    oppijat = oppija :: oppijat
    oppija
  }

  def getOppijat = oppijat

  private def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    "1.2.246.562.24.0000000000" + idCounter
  }
}

class MockOppijaRepository(db: Option[DB] = None) extends OppijaRepository with Futures {
  private var oppijat = new MockOppijat(MockOppijat.defaultOppijat)

  override def findOppijat(query: String) = {
    if (query.toLowerCase.contains("error")) {
      throw new TestingException("Testing error handling")
    }
    oppijat.getOppijat.filter(searchString(_).contains(query))
  }

  def findOrCreate(henkilö: NewHenkilö): Either[HttpStatus, Henkilö.Oid] =  {
    def oidFrom(oppijat: List[FullHenkilö]): Either[HttpStatus, Henkilö.Oid] = {
      oppijat match {
        case List(oppija) => Right(oppija.oid)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(TorErrorCategory.internalError())
      }
    }
    val NewHenkilö(hetu, etunimet, kutsumanimi, sukunimi) = henkilö
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

  private def searchString(oppija: FullHenkilö) = {
    oppija.toString.toUpperCase
  }


  override def resetFixtures {
    oppijat = new MockOppijat(MockOppijat.defaultOppijat)
  }

  def findFromDb(oid: String): Option[FullHenkilö] = {
    import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
    runQuery(Tables.OpiskeluOikeudet.filter(_.oppijaOid === oid)).headOption.map { oppijaRow =>
      FullHenkilö(oid, oid, oid, oid, oid, oppijat.äidinkieli, None)
    }
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