package fi.oph.koski.henkilo

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DB
import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory, RetryMiddleware}
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedotIndexer, OpiskeluoikeudenPerustiedotRepository}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.util.Timing

import scala.concurrent.duration.FiniteDuration

trait OpintopolkuHenkilöFacade {
  def findOppijaByOid(oid: String): Option[LaajatOppijaHenkilöTiedot] =
    findOppijaJaYhteystiedotByOid(oid).map(_.copy(yhteystiedot = List.empty))
  def findOppijaJaYhteystiedotByOid(oid: String): Option[LaajatOppijaHenkilöTiedot]
  def findOppijaByHetu(hetu: String): Option[LaajatOppijaHenkilöTiedot]
  def findOppijatNoSlaveOids(oids: Seq[String]): Seq[OppijaHenkilö]
  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid]
  def findByVarhaisinSyntymäaikaAndKotikunta(syntymäaika: String, kunta: String, page: Int): OppijaNumerorekisteriKuntarouhintatiedot
  def findMasterOppija(oid: String): Option[LaajatOppijaHenkilöTiedot]
  def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot]
  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, OppijaHenkilö]
  def findOppijatByHetusNoSlaveOids(hetus: Seq[String]): Seq[OppijaHenkilö]
  def findSlaveOids(masterOid: String): List[Oid]
  def findKuntahistoriat(oids: Seq[String], turvakiellolliset: Boolean): Either[HttpStatus, Seq[OppijanumerorekisteriKotikuntahistoriaRow]]
  def withRetryStrategy(strategy: OppijanumeroRekisteriClientRetryStrategy): OpintopolkuHenkilöFacade = this
}

object OpintopolkuHenkilöFacade {
  def apply(
    config: Config,
    db: => DB,
    hetu: Hetu,
    fixtures: => FixtureCreator,
    perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository,
    perustiedotIndexer: => OpiskeluoikeudenPerustiedotIndexer,
    rekisteriClientRetryStrategy: OppijanumeroRekisteriClientRetryStrategy = OppijanumeroRekisteriClientRetryStrategy.Default,
  ): OpintopolkuHenkilöFacade = config.getString("opintopolku.virkailija.url") match {
    case "mock" => new MockOpintopolkuHenkilöFacadeWithDBSupport(db, hetu, fixtures)
    case _ => RemoteOpintopolkuHenkilöFacade(config, perustiedotRepository, perustiedotIndexer, rekisteriClientRetryStrategy)
  }
}

object RemoteOpintopolkuHenkilöFacade {
  def apply(
    config: Config,
    perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository,
    perustiedotIndexer: => OpiskeluoikeudenPerustiedotIndexer,
    rekisteriClientRetryStrategy: OppijanumeroRekisteriClientRetryStrategy = OppijanumeroRekisteriClientRetryStrategy.Default,
  ): RemoteOpintopolkuHenkilöFacade = {
    val client = OppijanumeroRekisteriClient(config, rekisteriClientRetryStrategy)
    if (config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")) {
      new RemoteOpintopolkuHenkilöFacadeWithMockOids(client, perustiedotRepository, perustiedotIndexer, config)
    } else {
      new RemoteOpintopolkuHenkilöFacade(client, config)
    }
  }
}

class RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient: OppijanumeroRekisteriClient, config: Config)
  extends OpintopolkuHenkilöFacade
    with Timing {

  def findOppijaJaYhteystiedotByOid(oid: String): Option[LaajatOppijaHenkilöTiedot] =
    runIO(oppijanumeroRekisteriClient.findOppijaByOid(oid))

  def findOppijatNoSlaveOids(oids: Seq[Oid]): Seq[OppijaHenkilö] =
    runIO(oppijanumeroRekisteriClient.findOppijatNoSlaveOids(oids))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid] =
    runIO(oppijanumeroRekisteriClient.findChangedOppijaOids(since, offset, amount))

  def findByVarhaisinSyntymäaikaAndKotikunta(syntymäaika: String, kunta: String, page: Int)
  : OppijaNumerorekisteriKuntarouhintatiedot =
    runIO(oppijanumeroRekisteriClient.findByVarhaisinSyntymäaikaAndKotikunta(syntymäaika, kunta, page))

  def findOppijaByHetu(hetu: String): Option[LaajatOppijaHenkilöTiedot] =
    runIO(oppijanumeroRekisteriClient.findOppijaByHetu(hetu))

  def findMasterOppija(oid: String): Option[LaajatOppijaHenkilöTiedot] =
    runIO(oppijanumeroRekisteriClient.findMasterOppija(oid))

  def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot] =
    runIO(oppijanumeroRekisteriClient.findMasterOppijat(oids))

  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, OppijaHenkilö] =
    runIO(oppijanumeroRekisteriClient.findOrCreate(createUserInfo))

  def findOppijatByHetusNoSlaveOids(hetus: Seq[String]): List[OppijaHenkilö] =
    runIO(oppijanumeroRekisteriClient.findOppijatByHetusNoSlaveOids(hetus))

  def findSlaveOids(masterOid: String): List[Oid] = runIO(oppijanumeroRekisteriClient.findSlaveOids(masterOid))

  def findKuntahistoriat(oids: Seq[String], turvakielto: Boolean): Either[HttpStatus, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] =
    tryIO(oppijanumeroRekisteriClient.findKotikuntahistoria(oids, turvakielto)) { error =>
      logger.error(s"Kotikuntahistorian haku epäonnistui: $error")
      KoskiErrorCategory.internalError("Kotikuntahistorian haku epäonnistui")
    }

  override def withRetryStrategy(strategy: OppijanumeroRekisteriClientRetryStrategy): RemoteOpintopolkuHenkilöFacade =
    new RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient.withRetryStrategy(strategy), config)
}

class RemoteOpintopolkuHenkilöFacadeWithMockOids(
  oppijanumeroRekisteriClient: OppijanumeroRekisteriClient,
  perustiedotRepository: OpiskeluoikeudenPerustiedotRepository,
  perustiedotIndexer: OpiskeluoikeudenPerustiedotIndexer,
  config: Config,
) extends RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient, config) {
  override def findOppijatNoSlaveOids(oids: Seq[String]): Seq[OppijaHenkilö] = {
    val found = super.findOppijatNoSlaveOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap
    oids.map { oid =>
      found.get(oid) match {
        case Some(henkilö) => henkilö
        case None => createMock(oid)
      }
    }
  }

  override def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot] = {
    val found = super.findMasterOppijat(oids)
    oids.map { oid => oid -> createMockIfNotExists(oid, found) }.toMap
  }

  private def createMockIfNotExists(oid: String, found: Map[String, LaajatOppijaHenkilöTiedot]) = {
    found.get(oid) match {
      case Some(henkilö) => henkilö
      case None => createMock(oid)
    }
  }

  private def createMock(oid: String) = {
    perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(oid).map { henkilö =>
      LaajatOppijaHenkilöTiedot(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, Some("010101-123N"), None, None, None, None, 0, false)
    }.getOrElse(LaajatOppijaHenkilöTiedot(oid, oid.substring("1.2.246.562.24.".length, oid.length), "Testihenkilö", "Testihenkilö", Some("010101-123N"), None, None, None, None, 0, false))
  }

  override def withRetryStrategy(strategy: OppijanumeroRekisteriClientRetryStrategy): RemoteOpintopolkuHenkilöFacade =
    new RemoteOpintopolkuHenkilöFacadeWithMockOids(
      oppijanumeroRekisteriClient.withRetryStrategy(strategy),
      perustiedotRepository,
      perustiedotIndexer,
      config,
    )
}

object RemoteOpintopolkuHenkilöFacadeWithMockOids {
  def oppijaWithMockOid(h: TäydellisetHenkilötiedot): LaajatOppijaHenkilöTiedot = {
    LaajatOppijaHenkilöTiedot(
      oid = h.oid,
      sukunimi = h.sukunimi,
      etunimet = h.etunimet,
      kutsumanimi = h.kutsumanimi,
      hetu = h.hetu,
      syntymäaika = h.syntymäaika
    )
  }
}
