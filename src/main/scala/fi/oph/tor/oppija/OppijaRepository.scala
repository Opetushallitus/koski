package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.henkilö.HenkilöPalveluClient
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.tor.TorOppija
import fi.oph.tor.util.{CachingProxy, TimedProxy}

object OppijaRepository {
  def apply(config: Config): OppijaRepository = {
    CachingProxy(config, TimedProxy(if (config.hasPath("authentication-service")) {
      new RemoteOppijaRepository(HenkilöPalveluClient(config))
    } else {
      new MockOppijaRepository
    }))
  }
}

trait OppijaRepository {
  def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpStatus, Oppija.Id]

  def findOppijat(query: String): List[Oppija]
  def findByOid(id: String): Option[Oppija]

  def resetFixtures {}

  def findOrCreate(oppija: TorOppija): Either[HttpStatus, Oppija.Id] = {
    def oidFrom(oppijat: List[Oppija]): Either[HttpStatus, Oppija.Id] = {
      oppijat match {
        case List(oppija) => Right(oppija.oid.get)
        case _ => Left(HttpStatus.internalError("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt."))
      }
    }
    oppija match {
      case TorOppija(Oppija(None, Some(hetu), Some(etunimet), Some(kutsumanimi), Some(sukunimi)), _) =>
        create(hetu, etunimet, kutsumanimi, sukunimi).left.flatMap { case HttpStatus(409, _) =>
          oidFrom(findOppijat(hetu))
        }
      case TorOppija(Oppija(Some(oid), _, _, _, _), _) =>
        oidFrom(findByOid(oid).toList)
      case TorOppija(Oppija(_, Some(hetu), _, _, _), _) =>
        oidFrom(findOppijat(hetu))
      case _ =>
        Left(HttpStatus.badRequest("Either identifier (hetu, oid) or all user info (hetu + names) needed"))
    }
  }
}