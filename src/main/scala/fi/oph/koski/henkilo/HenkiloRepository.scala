package fi.oph.koski.henkilo

import fi.oph.koski.cache._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotRepository
import fi.oph.koski.schema._
import fi.oph.koski.virta.VirtaHenkilöRepository
import fi.oph.koski.ytr.YtrHenkilöRepository

import scala.concurrent.duration._

trait FindByOid {
  def findByOid(oid: String): Option[TäydellisetHenkilötiedot]
}

trait FindByHetu {
  def findByHetu(query: String)(implicit user: KoskiSession): Option[HenkilötiedotJaOid]
  def findByHetuDontCreate(hetu: String): Either[HttpStatus, Option[UusiHenkilö]]
}

object HenkilöRepository {
  def apply(application: KoskiApplication)(implicit cacheInvalidator: CacheManager): HenkilöRepository = {
    val opintopolku = new OpintopolkuHenkilöRepository(application.opintopolkuHenkilöFacade, application.koodistoViitePalvelu)
    HenkilöRepository(
      opintopolku,
      TimedProxy(VirtaHenkilöRepository(application.virtaClient, opintopolku, application.virtaAccessChecker).asInstanceOf[FindByHetu]),
      TimedProxy(YtrHenkilöRepository(application.ytrClient, opintopolku, application.ytrAccessChecker).asInstanceOf[FindByHetu]),
      application.perustiedotRepository
    )
  }
}

case class HenkilöRepository(opintopolku: OpintopolkuHenkilöRepository, virta: FindByHetu, ytr: FindByHetu, perustiedotRepository: OpiskeluoikeudenPerustiedotRepository)(implicit cacheInvalidator: CacheManager) extends FindByOid with Logging {
  private val oidCache: KeyValueCache[String, Option[TäydellisetHenkilötiedot]] =
    KeyValueCache(new ExpiringCache("HenkilöRepository", ExpiringCache.Params(1.hour, maxSize = 100, storeValuePredicate = {
      case (_, value) => value != None // Don't cache None results
    })), opintopolku.findByOid)
  
  // findByOid is locally cached
  def findByOid(oid: String): Option[TäydellisetHenkilötiedot] = oidCache(oid)
  // Other methods just call the non-cached implementation

  def findByOids(oids: List[String]): List[TäydellisetHenkilötiedot] = opintopolku.findByOids(oids)

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, TäydellisetHenkilötiedot] = opintopolku.findOrCreate(henkilö)

  // Etsii oppija-henkilön hetun perusteella oppijanumerorekisteristä. Jos henkilöä ei löydy oppijanumerorekisteristä,
  // mutta hetu löytyy Virrasta tai YTR:stä, niin luodaan oppijanumero ONR:ään käyttäen "nimitiedot" parametria jos
  // annettu, muuten Virrasta/YTR:stä löytynyttä nimeä. (Tämänhetkinen tulkinta laista on että oppijanumeroa ei
  // saa luoda jos ei löydy edes Virrasta tai YTR:stä.)
  def findByHetuOrCreateIfInYtrOrVirtaWithoutAccessCheck(hetu: String, nimitiedot: Option[Nimitiedot] = None): Option[HenkilötiedotJaOid] = {
    Hetu.validFormat(hetu) match {
      case Right(validHetu) =>
        val tiedot = opintopolku.findByHetu(hetu)(KoskiSession.systemUser)
        if (tiedot.isDefined) {
          tiedot
        } else {
          // Note: maps errors (Lefts) to None (and assumes the error has already been logged)
          val virtaTaiYtrHenkilo: Option[UusiHenkilö] =
            virta.findByHetuDontCreate(hetu).toOption.flatten.orElse(ytr.findByHetuDontCreate(hetu).toOption.flatten)
          val saadaankoLuodaOppijanumero = virtaTaiYtrHenkilo.nonEmpty
          if (saadaankoLuodaOppijanumero) {
            val uusiHenkilö = nimitiedot.map(n => UusiHenkilö(
              hetu = hetu,
              etunimet = n.etunimet,
              kutsumanimi = Some(n.kutsumanimi),
              sukunimi = n.sukunimi
            )).getOrElse(virtaTaiYtrHenkilo.get)
            opintopolku.findOrCreate(uusiHenkilö) match {
              case Right(henkilö) => Some(henkilö.toHenkilötiedotJaOid)
              case Left(error) =>
                logger.error("Oppijan lisäys henkilöpalveluun epäonnistui: " + error)
                None
            }
          } else {
            None
          }
        }
      case Left(status) => throw new Exception(status.errorString.mkString)
    }
  }

  def findHenkilötiedotByHetu(hetu: String)(implicit user: KoskiSession): List[HenkilötiedotJaOid] = Hetu.validFormat(hetu) match {
    case Right(validHetu) =>
      List(opintopolku, virta, ytr).iterator.map(_.findByHetu(hetu)).find(_.isDefined).toList.flatten
    case Left(status) => throw new Exception(status.errorString.mkString)
  }

  def findHenkilötiedot(query: String)(implicit user: KoskiSession): List[HenkilötiedotJaOid] =
    findByOids(perustiedotRepository.findOids(query)).map(_.toHenkilötiedotJaOid)
}
