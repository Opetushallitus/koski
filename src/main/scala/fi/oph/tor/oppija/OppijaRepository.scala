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
  def create(oppija: CreateOppija): CreationResult[String]

  def findOppijat(query: String): List[Oppija]
  def findById(id: String): Option[Oppija]

  def resetFixtures {}

  def findOrCreate(oppija: CreateOppija): CreationResult[String] = {
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