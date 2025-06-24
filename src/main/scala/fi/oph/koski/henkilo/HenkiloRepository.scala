package fi.oph.koski.henkilo

import fi.oph.koski.cache._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{Logging, TimedProxy}
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotRepository
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import fi.oph.koski.virta.VirtaHenkilöRepository
import fi.oph.koski.ytr.YtrHenkilöRepository

import scala.concurrent.duration.DurationInt

trait HetuBasedHenkilöRepository {
  def findByHetuDontCreate(hetu: String): Either[HttpStatus, Option[UusiHenkilö]]
  def hasAccess(user: KoskiSpecificSession): Boolean
}

object HenkilöRepository {
  def apply(application: KoskiApplication)(implicit cacheInvalidator: CacheManager): HenkilöRepository = {
    val opintopolku = new OpintopolkuHenkilöRepository(application.opintopolkuHenkilöFacade, application.koodistoViitePalvelu)
    HenkilöRepository(
      opintopolku,
      TimedProxy(VirtaHenkilöRepository(application.virtaClient, application.virtaAccessChecker).asInstanceOf[HetuBasedHenkilöRepository]),
      TimedProxy(YtrHenkilöRepository(application.ytrRepository, application.ytrAccessChecker).asInstanceOf[HetuBasedHenkilöRepository]),
      application.perustiedotRepository
    )
  }
}

case class HenkilöCacheKey(oid: String, findMasterIfSlaveOid: Boolean)

case class HenkilöRepository(
  opintopolku: OpintopolkuHenkilöRepository,
  virta: HetuBasedHenkilöRepository,
  ytr: HetuBasedHenkilöRepository,
  perustiedotRepository: OpiskeluoikeudenPerustiedotRepository
)(implicit cacheInvalidator: CacheManager) extends Logging with Timing {

  private val oidCache: KeyValueCache[HenkilöCacheKey, Option[LaajatOppijaHenkilöTiedot]] =
    KeyValueCache(new ExpiringCache("HenkilöRepository", ExpiringCache.Params(1.hour, maxSize = 100, storeValuePredicate = {
      case (_, value) => value != None // Don't cache None results
    })), findByCacheKey)

  private def findByCacheKey(key: HenkilöCacheKey) = if (key.findMasterIfSlaveOid) {
    opintopolku.findMasterByOid(key.oid)
  } else {
    opintopolku.findByOid(key.oid)
  }

  def henkilöExists(oid: String, findMasterIfSlaveOid: Boolean = false): Boolean =
    findByOid(oid, findMasterIfSlaveOid).isDefined

  // findByOid is locally cached
  def findByOid(oid: String, findMasterIfSlaveOid: Boolean = false): Option[LaajatOppijaHenkilöTiedot] =
    oidCache(HenkilöCacheKey(oid, findMasterIfSlaveOid))
  // Other methods just call the non-cached implementation

  def findOrCreate(henkilö: UusiHenkilö): Either[HttpStatus, OppijaHenkilö] = opintopolku.findOrCreate(henkilö)

  // Etsii oppija-henkilön hetun perusteella oppijanumerorekisteristä. Jos henkilöä ei löydy oppijanumerorekisteristä,
  // mutta hetu löytyy Virrasta tai YTR:stä, niin luodaan oppijanumero ONR:ään käyttäen "nimitiedot" parametria jos
  // annettu, muuten Virrasta/YTR:stä löytynyttä nimeä. (Tämänhetkinen tulkinta laista on että oppijanumeroa ei
  // saa luoda jos ei löydy edes Virrasta tai YTR:stä.)
  //
  // Jos userForAccessChecks on annettu, niin Virta/YTR katsotaan vain, jos ko. käyttäjällä on potentiaalisesti
  // pääsy johonkin Virta/YTR-tietoihin. Tämä on optimointi oppilaitosten virkailijakäliin, joissa käyttäjillä
  // ei yleensä ole tällaista pääsyä.
  def findByHetuOrCreateIfInYtrOrVirta(
    hetu: String,
    nimitiedot: Option[Nimitiedot] = None,
    userForAccessChecks: Option[KoskiSpecificSession] = None
  ): Option[OppijaHenkilö] = {
    Hetu.validFormat(hetu) match {
      case Right(validHetu) =>
        val kansalainen = opintopolku.findByHetu(hetu)
        if (kansalainen.isDefined) {
          kansalainen
        } else {
          val tarkistetaankoVirta = userForAccessChecks.isEmpty || userForAccessChecks.exists(virta.hasAccess)
          val tarkistetaankoYtr = userForAccessChecks.isEmpty || userForAccessChecks.exists(ytr.hasAccess)
          // huom, virheet (Left) tulkitaan Noneksi (ja oletetaan että virheet on lokitettu jo aikaisemmin)
          val virtaTaiYtrHenkilo: Option[UusiHenkilö] =
            (if (tarkistetaankoVirta) virta.findByHetuDontCreate(hetu).toOption.flatten else None)
            .orElse(if (tarkistetaankoYtr) ytr.findByHetuDontCreate(hetu).toOption.flatten else None)

          val saadaankoLuodaOppijanumero = virtaTaiYtrHenkilo.nonEmpty
          if (saadaankoLuodaOppijanumero) {
            val uusiHenkilö = nimitiedot.map(n => UusiHenkilö(
              hetu = hetu,
              etunimet = n.etunimet,
              kutsumanimi = Some(n.kutsumanimi),
              sukunimi = n.sukunimi
            )).getOrElse(virtaTaiYtrHenkilo.get)
            opintopolku.findOrCreate(uusiHenkilö) match {
              case Right(henkilö) => Some(henkilö)
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

  def findByOids(query: String)(implicit session: KoskiSpecificSession): List[LaajatOppijaHenkilöTiedot] = {
    val perustiedotResult = timed("searchHenkilötiedot:findByOids:perustiedotRepository.findOids", 0) {
      perustiedotRepository.findOids(query)
    }
    timed("searchHenkilötiedot:findByOids:opintopolku.findMastersByOids", 0) {
      opintopolku.findMastersByOids(perustiedotResult)
    }
  }

  def oppijaHenkilöToTäydellisetHenkilötiedot(henkilö: OppijaHenkilö): TäydellisetHenkilötiedot =
    opintopolku.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö)
}
