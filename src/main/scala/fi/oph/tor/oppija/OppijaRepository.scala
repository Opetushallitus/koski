package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.henkilö.HenkilöPalveluClient
import fi.oph.tor.http.HttpError
import fi.oph.tor.tor.TorOppija

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
  def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpError, Oppija.Id]

  def findOppijat(query: String): List[Oppija]
  def findByOid(id: String): Option[Oppija]

  def resetFixtures {}

  def findOrCreate(oppija: TorOppija): Either[HttpError, Oppija.Id] = {
    def oidFrom(oppijat: List[Oppija]): Either[HttpError, Oppija.Id] = {
      oppijat match {
        case List(oppija) => Right(oppija.oid.get)
        case _ => Left(HttpError(500, "Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt."))
      }
    }
    oppija match {
      case TorOppija(Oppija(None, Some(hetu), Some(etunimet), Some(kutsumanimi), Some(sukunimi)), _) =>
        create(hetu, etunimet, kutsumanimi, sukunimi).left.flatMap { case HttpError(409, _) =>
          oidFrom(findOppijat(hetu))
        }
      case TorOppija(Oppija(Some(oid), _, _, _, _), _) =>
        oidFrom(findByOid(oid).toList)
      case TorOppija(Oppija(_, Some(hetu), _, _, _), _) =>
        oidFrom(findOppijat(hetu))
      case _ =>
        Left(HttpError(400, "Either identifier (hetu, oid) or all user info (hetu + names) needed"))
    }
  }
}