package fi.oph.tor.oppija

import com.typesafe.config.Config
import fi.oph.tor.cache.{BaseCacheDetails, CachingStrategyBase, CachingProxy}
import fi.oph.tor.db.TorDatabase
import fi.oph.tor.henkilo.{Hetu, AuthenticationServiceClient}
import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.log.TimedProxy
import fi.oph.tor.schema._
import fi.oph.tor.util.Invocation
import fi.oph.tor.log.Logging

object OppijaRepository {
  def apply(config: Config, database: TorDatabase, koodistoViitePalvelu: KoodistoViitePalvelu): OppijaRepository = {
    CachingProxy(new OppijaRepositoryCachingStrategy, TimedProxy(if (config.hasPath("opintopolku.virkailija.username")) {
      new RemoteOppijaRepository(AuthenticationServiceClient(config), koodistoViitePalvelu)
    } else {
      new MockOppijaRepository(Some(database.db))
    }))
  }
}

class OppijaRepositoryCachingStrategy extends CachingStrategyBase(new BaseCacheDetails(durationSeconds = 60, maxSize = 100, refreshing = true) {
  override def storeValuePredicate: (Invocation, AnyRef) => Boolean = {
    case (invocation, value) => invocation.method.getName match {
      case "findByOid" => value match {
        case Some(_) => true
        case _ => false
      }
      case _ => false
    }
  }})

trait OppijaRepository extends Logging {
  def create(hetu: String, etunimet: String, kutsumanimi: String, sukunimi: String): Either[HttpStatus, Henkilö.Oid]

  def findOppijat(query: String): List[FullHenkilö]
  def findByOid(id: String): Option[FullHenkilö]
  def findByOids(oids: List[String]): List[FullHenkilö]

  def resetFixtures {}

  def findOrCreate(henkilö: Henkilö): Either[HttpStatus, Henkilö.Oid] = {
    def oidFrom(oppijat: List[FullHenkilö]): Either[HttpStatus, Henkilö.Oid] = {
      oppijat match {
        case List(oppija) => Right(oppija.oid)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(TorErrorCategory.internalError())
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
        Left(TorErrorCategory.badRequest.validation.henkilötiedot.puuttelliset())
    }
  }
}