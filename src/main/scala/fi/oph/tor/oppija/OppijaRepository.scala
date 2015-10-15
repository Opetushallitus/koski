package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.henkilö.HenkilöPalveluClient
import fi.oph.tor.http.HttpError

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
  def create(oppija: CreateOppija): Either[HttpError, Oppija.Id]

  def findOppijat(query: String): List[Oppija]
  def findById(id: String): Option[Oppija]

  def resetFixtures {}

  def findOrCreate(oppija: CreateOppija): Either[HttpError, Oppija.Id] = {
    create(oppija).left.flatMap { case HttpError(409, _) =>
        findOppijat(oppija.hetu) match {
          case (o :: Nil) => Right(o.oid)
          case _ => Left(HttpError(500, "Oppijan lisääminen epäonnistui"))
        }
    }
  }
}

trait CreateOppija {
  def etunimet: String
  def kutsumanimi: String
  def sukunimi: String
  def hetu: String
}