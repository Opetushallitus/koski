package fi.oph.koski.henkilo

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.OpiskeluOikeudetWithAccessCheck
import fi.oph.koski.db.{KoskiDatabaseMethods, PostgresDriverWithJsonSupport}
import fi.oph.koski.elasticsearch.ElasticSearch
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession.systemUser
import fi.oph.koski.log.Logging
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotRepository
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.util.Timing
import org.http4s._

trait OpintopolkuHenkilöFacade {
  def findOppijaByOid(oid: String): Option[OppijaHenkilö]
  def findOppijaByHetu(hetu: String): Option[OppijaHenkilö]
  def findOppijatNoSlaveOids(oids: List[String]): List[OppijaHenkilö]
  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid]
  def findMasterOppija(oid: String): Option[OppijaHenkilö]
  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, OppijaHenkilö]
  def findOppijatByHetusNoSlaveOids(hetus: List[String]): List[OppijaHenkilö]
  def findSlaveOids(masterOid: String): List[Oid]
}

object OpintopolkuHenkilöFacade {
  def apply(config: Config, db: => DB, perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository, elasticSearch: => ElasticSearch): OpintopolkuHenkilöFacade = config.getString("opintopolku.virkailija.url") match {
    case "mock" => new MockOpintopolkuHenkilöFacadeWithDBSupport(db)
    case _ => RemoteOpintopolkuHenkilöFacade(config, perustiedotRepository, elasticSearch)
  }
}

object RemoteOpintopolkuHenkilöFacade {
  def apply(config: Config, perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository, elasticSearch: => ElasticSearch): RemoteOpintopolkuHenkilöFacade = {
    if (config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")) {
      new RemoteOpintopolkuHenkilöFacadeWithMockOids(OppijanumeroRekisteriClient(config), perustiedotRepository, elasticSearch)
    } else {
      new RemoteOpintopolkuHenkilöFacade(OppijanumeroRekisteriClient(config))
    }
  }
}

class RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient: OppijanumeroRekisteriClient) extends OpintopolkuHenkilöFacade with EntityDecoderInstances with Timing {
  def findOppijaByOid(oid: String): Option[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOppijaByOid(oid))

  def findOppijatNoSlaveOids(oids: List[Oid]): List[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOppijatNoSlaveOids(oids))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid] =
    runTask(oppijanumeroRekisteriClient.findChangedOppijaOids(since, offset, amount))

  def findOppijaByHetu(hetu: String): Option[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOppijaByHetu(hetu))

  def findMasterOppija(oid: String): Option[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findMasterOppija(oid))

  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOrCreate(createUserInfo))

  def findOppijatByHetusNoSlaveOids(hetus: List[String]): List[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOppijatByHetusNoSlaveOids(hetus))

  def findSlaveOids(masterOid: String): List[Oid] = runTask(oppijanumeroRekisteriClient.findSlaveOids(masterOid))
}

class RemoteOpintopolkuHenkilöFacadeWithMockOids(oppijanumeroRekisteriClient: OppijanumeroRekisteriClient, perustiedotRepository: OpiskeluoikeudenPerustiedotRepository, elasticSearch: ElasticSearch) extends RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient) {
  override def findOppijatNoSlaveOids(oids: List[String]): List[OppijaHenkilö] = {
    val found = super.findOppijatNoSlaveOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap
    oids.map { oid =>
      found.get(oid) match {
        case Some(henkilö) => henkilö
        case None =>
          elasticSearch.refreshIndex
          perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(oid).map { henkilö =>
            OppijaHenkilö(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, Some("010101-123N"), None, None, None, 0, false)
          }.getOrElse(OppijaHenkilö(oid, oid.substring("1.2.246.562.24.".length, oid.length), "Testihenkilö", "Testihenkilö", Some("010101-123N"), None, None, None, 0, false))
      }
    }
  }
}

object RemoteOpintopolkuHenkilöFacadeWithMockOids {
  def oppijaWithMockOid(h: TäydellisetHenkilötiedot): OppijaHenkilö = {
    OppijaHenkilö(
      oid = h.oid,
      sukunimi = h.sukunimi,
      etunimet = h.etunimet,
      kutsumanimi = h.kutsumanimi,
      hetu = h.hetu,
      syntymäaika = h.syntymäaika
    )
  }
}

class MockOpintopolkuHenkilöFacadeWithDBSupport(val db: DB) extends MockOpintopolkuHenkilöFacade with KoskiDatabaseMethods {

  def findFromDb(oid: String): Option[OppijaHenkilö] = {
    runQuery(OpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oppijaOid === oid)).headOption.map { oppijaRow =>
      OppijaHenkilö(oid, oid, oid, oid, Some(oid), None, None, None)
    }
  }

  def runQuery[E, U](fullQuery: PostgresDriverWithJsonSupport.api.Query[E, U, Seq]): Seq[U] = {
    runDbSync(fullQuery.result, skipCheck = true)
  }

  override protected def findHenkilötiedot(id: String): Option[OppijaHenkilöWithMasterInfo] = {
    super.findHenkilötiedot(id).orElse(findFromDb(id).map(OppijaHenkilöWithMasterInfo(_, None)))
  }
}

class MockOpintopolkuHenkilöFacade() extends OpintopolkuHenkilöFacade with Logging {
  var oppijat = new MockOppijat(MockOppijat.defaultOppijat)

  def resetFixtures = synchronized {
    oppijat = new MockOppijat(MockOppijat.defaultOppijat)
  }

  private def create(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, String] = synchronized {
    if (createUserInfo.sukunimi == "error") {
      throw new TestingException("Testing error handling")
    } else if (oppijat.getOppijat.exists(_.henkilö.hetu == createUserInfo.hetu)) {
      Left(KoskiErrorCategory.conflict.hetu("conflict"))
    } else {
      val newOppija = oppijat.oppija(createUserInfo.sukunimi, createUserInfo.etunimet, createUserInfo.hetu.getOrElse(throw new IllegalArgumentException("Hetu puuttuu")), kutsumanimi = Some(createUserInfo.kutsumanimi))
      Right(newOppija.oid)
    }
  }

  def findOppijaByOid(henkilöOid: String): Option[OppijaHenkilö] = {
    findHenkilötiedot(henkilöOid).map(_.henkilö).map(withLinkedOids)
  }

  def findMasterOppija(henkilöOid: String): Option[OppijaHenkilö] = {
    findHenkilötiedot(henkilöOid).flatMap(_.master).map(withLinkedOids)
  }

  protected def findHenkilötiedot(id: String): Option[OppijaHenkilöWithMasterInfo] = synchronized {
    oppijat.getOppijat.find(_.henkilö.oid == id)
  }

  def findOppijatNoSlaveOids(oids: List[String]): List[OppijaHenkilö] = {
    oids.flatMap(findOppijaByOid).map(withLinkedOids)
  }

  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, OppijaHenkilö] = {
    def oidFrom(oppijat: Option[OppijaHenkilö]): Either[HttpStatus, Oid] = {
      oppijat match {
        case Some(oppija) =>
          Right(oppija.oid)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(KoskiErrorCategory.internalError())
      }
    }
    val UusiOppijaHenkilö(Some(hetu), sukunimi, etunimet, kutsumanimi, _) = createUserInfo
    val oid = Hetu.validate(hetu, acceptSynthetic = true).right.flatMap { hetu =>
      create(createUserInfo).left.flatMap {
        case HttpStatus(409, _) => oidFrom(findOppijaByHetu(hetu))
        case HttpStatus(_, _) => throw new RuntimeException("Unreachable match arm: HTTP status code must be 409")
      }
    }
    oid.right.map(oid => findOppijaByOid(oid).get)
  }

  def modifyMock(oppija: OppijaHenkilöWithMasterInfo): Unit = synchronized {
    oppijat = new MockOppijat(oppijat.getOppijat.map { o =>
      if (o.henkilö.oid == oppija.henkilö.oid)
        o.copy(henkilö = o.henkilö.copy(etunimet = oppija.henkilö.etunimet, kutsumanimi = oppija.henkilö.kutsumanimi, sukunimi = oppija.henkilö.sukunimi), master = oppija.master)
      else o
    })
  }

  def resetMock(): Unit = synchronized {
    oppijat = new MockOppijat(MockOppijat.defaultOppijat)
  }

  override def findOppijaByHetu(hetu: String): Option[OppijaHenkilö] = synchronized {
    oppijat.getOppijat.find(_.henkilö.hetu.contains(hetu)).map(h => h.master.getOrElse(h.henkilö))
  }

  override def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid] = synchronized {
    MockOppijat.defaultOppijat.diff(oppijat.getOppijat).map(_.henkilö.oid)
  }

  def findOppijatByHetusNoSlaveOids(hetus: List[String]): List[OppijaHenkilö] = synchronized {
    hetus.flatMap(findOppijaByHetu)
  }

  override def findSlaveOids(masterOid: String): List[Oid] =
    MockOppijat.defaultOppijat.filter(_.master.exists(_.oid == masterOid)).map(_.henkilö.oid)

  private def withLinkedOids(x: OppijaHenkilö) = x.copy(linkitetytOidit = findSlaveOids(x.oid))
}
