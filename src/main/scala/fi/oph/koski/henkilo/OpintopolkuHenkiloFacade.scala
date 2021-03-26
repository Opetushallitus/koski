package fi.oph.koski.henkilo

import com.typesafe.config.Config
import fi.oph.koski.db.DB
import fi.oph.koski.http.Http._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedotIndexer, OpiskeluoikeudenPerustiedotRepository}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.util.Timing
import org.http4s._

trait OpintopolkuHenkilöFacade {
  def findOppijaByOid(oid: String): Option[LaajatOppijaHenkilöTiedot] =
    findOppijaJaYhteystiedotByOid(oid).map(_.copy(yhteystiedot = List.empty))
  def findOppijaJaYhteystiedotByOid(oid: String): Option[LaajatOppijaHenkilöTiedot]
  def findOppijaByHetu(hetu: String): Option[LaajatOppijaHenkilöTiedot]
  def findOppijatNoSlaveOids(oids: Seq[String]): Seq[OppijaHenkilö]
  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid]
  def findMasterOppija(oid: String): Option[LaajatOppijaHenkilöTiedot]
  def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot]
  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, OppijaHenkilö]
  def findOppijatByHetusNoSlaveOids(hetus: Seq[String]): Seq[OppijaHenkilö]
  def findSlaveOids(masterOid: String): List[Oid]
}

object OpintopolkuHenkilöFacade {
  def apply(
    config: Config,
    db: => DB,
    perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository,
    perustiedotIndexer: => OpiskeluoikeudenPerustiedotIndexer
  ): OpintopolkuHenkilöFacade = config.getString("opintopolku.virkailija.url") match {
    case "mock" => new MockOpintopolkuHenkilöFacadeWithDBSupport(db)
    case _ => RemoteOpintopolkuHenkilöFacade(config, perustiedotRepository, perustiedotIndexer)
  }
}

object RemoteOpintopolkuHenkilöFacade {
  def apply(config: Config, perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository, perustiedotIndexer: => OpiskeluoikeudenPerustiedotIndexer): RemoteOpintopolkuHenkilöFacade = {
    if (config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")) {
      new RemoteOpintopolkuHenkilöFacadeWithMockOids(OppijanumeroRekisteriClient(config), perustiedotRepository, perustiedotIndexer)
    } else {
      new RemoteOpintopolkuHenkilöFacade(OppijanumeroRekisteriClient(config))
    }
  }
}

class RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient: OppijanumeroRekisteriClient)
  extends OpintopolkuHenkilöFacade
    with EntityDecoderInstances
    with Timing {

  def findOppijaJaYhteystiedotByOid(oid: String): Option[LaajatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findOppijaByOid(oid))

  def findOppijatNoSlaveOids(oids: Seq[Oid]): Seq[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOppijatNoSlaveOids(oids))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid] =
    runTask(oppijanumeroRekisteriClient.findChangedOppijaOids(since, offset, amount))

  def findOppijaByHetu(hetu: String): Option[LaajatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findOppijaByHetu(hetu))

  def findMasterOppija(oid: String): Option[LaajatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findMasterOppija(oid))

  def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findMasterOppijat(oids))

  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOrCreate(createUserInfo))

  def findOppijatByHetusNoSlaveOids(hetus: Seq[String]): List[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOppijatByHetusNoSlaveOids(hetus))

  def findSlaveOids(masterOid: String): List[Oid] = runTask(oppijanumeroRekisteriClient.findSlaveOids(masterOid))
}

class RemoteOpintopolkuHenkilöFacadeWithMockOids(
  oppijanumeroRekisteriClient: OppijanumeroRekisteriClient,
  perustiedotRepository: OpiskeluoikeudenPerustiedotRepository,
  perustiedotIndexer: OpiskeluoikeudenPerustiedotIndexer
) extends RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient) {
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
