package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.henkilö.HenkilöPalveluClient
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.schema._
import fi.oph.tor.util.{CachingProxy, TimedProxy}
import fi.vm.sade.utils.slf4j.Logging

object OppijaRepository {
  def apply(config: Config): OppijaRepository = {
    CachingProxy(config, TimedProxy(if (config.hasPath("authentication-service")) {
      new RemoteOppijaRepository(HenkilöPalveluClient(config))
    } else {
      new MockOppijaRepository
    }))
  }
}

trait OppijaRepository extends Logging {
  def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpStatus, Henkilö.Id]

  def findOppijat(query: String): List[FullHenkilö]
  def findByOid(id: String): Option[FullHenkilö]

  def resetFixtures {}

  def findOrCreate(henkilö: Henkilö): Either[HttpStatus, Henkilö.Id] = {
    def oidFrom(oppijat: List[FullHenkilö]): Either[HttpStatus, Henkilö.Id] = {
      oppijat match {
        case List(oppija) => Right(oppija.oid)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(HttpStatus.internalError())
      }
    }
    henkilö match {
      case NewHenkilö(hetu, etunimet, kutsumanimi, sukunimi) =>
        create(hetu, etunimet, kutsumanimi, sukunimi).left.flatMap { case HttpStatus(409, _) =>
          oidFrom(findOppijat(hetu))
        }
      case OidHenkilö(oid) =>
        oidFrom(findByOid(oid).toList)
      case _ =>
        Left(HttpStatus.badRequest("Either identifier (oid) or all user info (hetu + names) needed"))
    }
  }
}