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