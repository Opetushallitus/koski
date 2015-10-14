package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.henkilö.HenkilöPalveluClient

object OppijaRepository {
  def apply(config: Config) = {
    if (config.hasPath("authentication-service")) {
      new RemoteOppijaRepository(HenkilöPalveluClient(config))
    } else {
      new MockOppijaRepository
    }
  }
}

trait OppijaRepository {
  def create(oppija: CreateOppija): OppijaCreationResult

  def findOppijat(query: String): List[Oppija]
  def findById(id: String): Option[Oppija]

  def resetFixtures {}

  def findOrCreate(oppija: CreateOppija): OppijaCreationResult = {
    create(oppija) match {
      case Failed(409, _) =>
        findOppijat(oppija.hetu) match {
          case (o :: Nil) => Exists(o.oid)
          case _ => Failed(500, "Oppijan lisääminen epäonnistui")
        }
      case result => result
    }
  }
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
  def ok = httpStatus == 200
}
case class Created(oid: String) extends OppijaCreationResult {
  def httpStatus = 200
  def text = oid
}
case class Exists(oid: String) extends OppijaCreationResult {
  def httpStatus = 200
  def text = oid
}
case class Failed(val httpStatus: Int, val text: String) extends OppijaCreationResult