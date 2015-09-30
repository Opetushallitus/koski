package fi.oph.tor.oppija

import com.typesafe.config.Config

object OppijaRepository {
  def apply(config: Config) = {
    if (config.hasPath("authentication-service")) {
      new AuthenticationServiceClient(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"))
    } else {
      new MockOppijaRepository
    }
  }
}

trait OppijaRepository {
  def create(oppija: CreateOppija): String

  def findOppijat(query: String): List[Oppija]

  def resetMocks {}
}

class MockOppijaRepository extends OppijaRepository {
  private val defaultOppijat = List(
    Oppija(generateId, "esimerkki", "eero", "010101-123N"),
    Oppija(generateId, "eerola", "jouni", ""),
    Oppija(generateId, "markkanen", "eero", ""),
    Oppija(generateId, "tekijä", "teija", "150995-914X")
  )

  private var idCounter = 0

  private var oppijat = defaultOppijat

  override def findOppijat(query: String) = {
    oppijat.filter(searchString(_).contains(query.toLowerCase))
  }

  override def create(oppija: CreateOppija): String = {
    val newOppija = Oppija(generateId, oppija.sukunimi, oppija.etunimet, oppija.hetu)
    oppijat = oppijat :+ newOppija
    newOppija.oid
  }

  private def searchString(oppija: Oppija) = {
    oppija.toString.toLowerCase
  }

  private def generateId(): String = {
    idCounter = idCounter + 1
    "1.2.246.562.24.0000000000" + idCounter
  }

  override def resetMocks {
    idCounter = 0
    oppijat = defaultOppijat
  }
}

case class CreateOppija(etunimet: String, kutsumanimi: String, sukunimi: String, hetu: String)