package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.cache.{CachingStrategyBase, CachingProxy}
import fi.oph.tor.db.TorDatabase
import fi.oph.tor.henkilo.{Hetu, AuthenticationServiceClient}
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.schema._
import fi.oph.tor.util.{Invocation, TimedProxy}
import fi.vm.sade.utils.slf4j.Logging

object OppijaRepository {
  def apply(config: Config, database: TorDatabase): OppijaRepository = {
    CachingProxy(new OppijaRepositoryCachingStrategy, TimedProxy(if (config.hasPath("authentication-service")) {
      new RemoteOppijaRepository(AuthenticationServiceClient(config))
    } else {
      new MockOppijaRepository(Some(database.db))
    }))
  }
}

class OppijaRepositoryCachingStrategy extends CachingStrategyBase(durationSeconds = 60, maxSize = 1000) {
  def apply(invocation: Invocation): AnyRef = {
    invocation.method.getName match {
      case "findByOid" =>
        invokeAndPossiblyStore(invocation) {
          case Some(_) => true
          case _ => false
        }
      case "findOppijat" =>
        invokeAndStore(invocation)
      case _ => invocation.invoke
    }
  }
}

trait OppijaRepository extends Logging {
  def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpStatus, Henkilö.Oid]

  def findOppijat(query: String): List[FullHenkilö]
  def findByOid(id: String): Option[FullHenkilö]

  def resetFixtures {}

  def findOrCreate(henkilö: Henkilö): Either[HttpStatus, Henkilö.Oid] = {
    def oidFrom(oppijat: List[FullHenkilö]): Either[HttpStatus, Henkilö.Oid] = {
      oppijat match {
        case List(oppija) => Right(oppija.oid)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(HttpStatus.internalError())
      }
    }
    henkilö match {
      case NewHenkilö(hetu, etunimet, kutsumanimi, sukunimi) =>
        Hetu.validate(hetu).right.flatMap { hetu =>
          create(hetu, etunimet, kutsumanimi, sukunimi).left.flatMap { case HttpStatus(409, _) =>
            oidFrom(findOppijat(hetu))
          }
        }
      case OidHenkilö(oid) =>
        oidFrom(findByOid(oid).toList)
      case _ =>
        Left(HttpStatus.badRequest("Joko oid tai (hetu, etunimet, sukunimi, kutsumanimi) tarvitaan henkilön hakuun/luontiin"))
    }
  }
}