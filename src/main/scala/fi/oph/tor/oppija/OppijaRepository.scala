package fi.oph.tor.oppija

import com.typesafe.config.Config

object OppijaRepository {
  def apply(config: Config) = {
    if (config.hasPath("authentication-service")) {
      new RemoteOppijaRepository(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"))
    } else {
      new MockOppijaRepository
    }
  }
}

trait OppijaRepository {
  def create(oppija: CreateOppija): OppijaCreationResult

  def findOppijat(query: String): List[Oppija]
  def findById(id: String): Option[Oppija]

  def resetMocks {}
}

class MockOppijaRepository extends OppijaRepository {
  private def defaultOppijat = List(
    Oppija(generateId, "esimerkki", "eero", "010101-123N"),
    Oppija(generateId, "eerola", "jouni", ""),
    Oppija(generateId, "markkanen", "eero", ""),
    Oppija(generateId, "tekijä", "teija", "150995-914X")
  )

  private var idCounter = 0

  private var oppijat = defaultOppijat

  override def findOppijat(query: String) = {
    if(query.toLowerCase.contains("error")) {
      throw new RuntimeException("BOOM!")
    }
    oppijat.filter(searchString(_).contains(query))
  }

  override def create(oppija: CreateOppija): OppijaCreationResult = {
    if (oppijat.find(o => o.hetu == oppija.hetu).isDefined) {
      Failed(409, "conflict")
    } else {
      val newOppija = Oppija(generateId, oppija.sukunimi, oppija.etunimet, oppija.hetu)
      oppijat = oppijat :+ newOppija
      Created(newOppija.oid)
    }
  }

  private def searchString(oppija: Oppija) = {
    oppija.toString.toUpperCase
  }

  private def generateId(): String = {
    idCounter = idCounter + 1
    "1.2.246.562.24.0000000000" + idCounter
  }

  override def resetMocks {
    idCounter = 0
    oppijat = defaultOppijat
  }

  override def findById(id: String): Option[Oppija] = oppijat.filter(_.oid == id).headOption
}

trait CreateOppija {
  def etunimet: String
  def kutsumanimi: String
  def sukunimi: String
  def hetu: String
}

sealed trait OppijaCreationResult {
  def httpStatus: Int
  def text: String
}
case class Created(oid: String) extends OppijaCreationResult {
  def httpStatus = 200
  def text = oid
}
case class Failed(val httpStatus: Int, val text: String) extends OppijaCreationResult