package fi.oph.koski.henkilo

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.OpiskeluOikeudetWithAccessCheck
import fi.oph.koski.db.{KoskiDatabaseMethods, PostgresDriverWithJsonSupport}
import fi.oph.koski.elasticsearch.ElasticSearch
import fi.oph.koski.henkilo.kayttooikeusservice.KäyttöoikeusServiceClient
import fi.oph.koski.henkilo.oppijanumerorekisteriservice.{KäyttäjäHenkilö, OppijaHenkilö, UusiHenkilö, _}
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory, _}
import fi.oph.koski.koskiuser.KoskiSession.systemUser
import fi.oph.koski.koskiuser.{MockUsers, OrganisaationKäyttöoikeusryhmä}
import fi.oph.koski.log.Logging
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotRepository
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{Henkilö, TäydellisetHenkilötiedot, TäydellisetHenkilötiedotWithMasterInfo}
import fi.oph.koski.util.Timing
import org.http4s._

trait OpintopolkuHenkilöFacade {
  def findKäyttäjäByOid(oid: String): Option[KäyttäjäHenkilö]
  def findOppijaByOid(oid: String): Option[OppijaHenkilö]
  def findOppijaByHetu(hetu: String): Option[OppijaHenkilö]
  def findOppijatByOids(oids: List[String]): List[OppijaHenkilö]
  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid]
  def findMasterOppija(oid: String): Option[OppijaHenkilö]
  def findOrCreate(createUserInfo: UusiHenkilö): Either[HttpStatus, OppijaHenkilö]
  def organisaationSähköpostit(organisaatioOid: String, ryhmä: String): List[String]
}

object OpintopolkuHenkilöFacade {
  def apply(config: Config, db: => DB, perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository, elasticSearch: => ElasticSearch): OpintopolkuHenkilöFacade = config.getString("opintopolku.virkailija.url") match {
    case "mock" => new MockOpintopolkuHenkilöFacadeWithDBSupport(db)
    case _ => RemoteOpintopolkuHenkilöFacade(config, perustiedotRepository, elasticSearch)
  }
}

object RemoteOpintopolkuHenkilöFacade {
  def apply(config: Config, perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository, elasticSearch: => ElasticSearch): RemoteOpintopolkuHenkilöFacade = {
    val serviceConfig = makeServiceConfig(config)

    if (config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")) {
      new RemoteOpintopolkuHenkilöFacadeWithMockOids(OppijanumeroRekisteriClient(config), KäyttöoikeusServiceClient(config), perustiedotRepository, elasticSearch)
    } else {
      new RemoteOpintopolkuHenkilöFacade(OppijanumeroRekisteriClient(config), KäyttöoikeusServiceClient(config))
    }
  }

  def makeServiceConfig(config: Config) = ServiceConfig.apply(config, "authentication-service", "authentication-service.virkailija", "opintopolku.virkailija")
}

class RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient: OppijanumeroRekisteriClient, käyttöoikeusServiceClient: KäyttöoikeusServiceClient) extends OpintopolkuHenkilöFacade with EntityDecoderInstances with Timing {
  def findOppijaByOid(oid: String): Option[OppijaHenkilö] =
    findOppijatByOids(List(oid)).headOption

  def findOppijatByOids(oids: List[Oid]): List[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOppijatByOids(oids))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid] =
    runTask(oppijanumeroRekisteriClient.findChangedOppijaOids(since, offset, amount))

  def findOppijaByHetu(hetu: String): Option[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOppijaByHetu(hetu))

  def findMasterOppija(oid: String): Option[OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findMasterOppija(oid))

  def findKäyttäjäByOid(oid: String): Option[KäyttäjäHenkilö] = runTask(
    oppijanumeroRekisteriClient.findKäyttäjäByOid(oid).flatMap { käyttäjäHenkilö: Option[KäyttäjäHenkilö] =>
      käyttöoikeusServiceClient.getKäyttäjätiedot(oid)
        .map(käyttäjätiedot => käyttäjäHenkilö.map(_.copy(kayttajatiedot = käyttäjätiedot)))
    }
  )

  def findOrCreate(createUserInfo: UusiHenkilö): Either[HttpStatus, OppijaHenkilö] =
    runTask(oppijanumeroRekisteriClient.findOrCreate(createUserInfo))

  def organisaationSähköpostit(organisaatioOid: String, ryhmä: String): List[String] =
    runTask(oppijanumeroRekisteriClient.findSähköpostit(organisaatioOid, ryhmä))
}

class RemoteOpintopolkuHenkilöFacadeWithMockOids(oppijanumeroRekisteriClient: OppijanumeroRekisteriClient, käyttöoikeusServiceClient: KäyttöoikeusServiceClient, perustiedotRepository: OpiskeluoikeudenPerustiedotRepository, elasticSearch: ElasticSearch) extends RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient, käyttöoikeusServiceClient) {
  override def findOppijatByOids(oids: List[String]): List[OppijaHenkilö] = {
    val found = super.findOppijatByOids(oids).map(henkilö => (henkilö.oidHenkilo, henkilö)).toMap
    oids.map { oid =>
      found.get(oid) match {
        case Some(henkilö) => henkilö
        case None =>
          elasticSearch.refreshIndex
          perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(oid).map { henkilö =>
            OppijaHenkilö(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, Some("010101-123N"), None, None, None, 0)
          }.getOrElse(OppijaHenkilö(oid, oid.substring("1.2.246.562.24.".length, oid.length), "Testihenkilö", "Testihenkilö", Some("010101-123N"), None, None, None, 0))
      }
    }
  }

  override def findKäyttäjäByOid(oid: String): Option[KäyttäjäHenkilö] = super.findKäyttäjäByOid(oid).orElse {
    Some(KäyttäjäHenkilö(oid, oid.substring("1.2.246.562.24.".length, oid.length), "Tuntematon", "Tuntematon", None, None))
  }
}

class MockOpintopolkuHenkilöFacadeWithDBSupport(val db: DB) extends MockOpintopolkuHenkilöFacade with KoskiDatabaseMethods {
  def findFromDb(oid: String): Option[TäydellisetHenkilötiedot] = {
    runQuery(OpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oppijaOid === oid)).headOption.map { oppijaRow =>
      TäydellisetHenkilötiedot(oid, Some(oid), None, oid, oid, oid, oppijat.äidinkieli, None)
    }
  }

  def runQuery[E, U](fullQuery: PostgresDriverWithJsonSupport.api.Query[E, U, Seq]): Seq[U] = {
    runDbSync(fullQuery.result, skipCheck = true)
  }

  override protected def findHenkilötiedot(id: String): Option[TäydellisetHenkilötiedotWithMasterInfo] = {
    super.findHenkilötiedot(id).orElse(findFromDb(id).map(TäydellisetHenkilötiedotWithMasterInfo(_, None)))
  }
}

class MockOpintopolkuHenkilöFacade() extends OpintopolkuHenkilöFacade with Logging {
  var oppijat = new MockOppijat(MockOppijat.defaultOppijat)

  def resetFixtures = synchronized {
    oppijat = new MockOppijat(MockOppijat.defaultOppijat)
  }

  private def create(createUserInfo: UusiHenkilö): Either[HttpStatus, String] = synchronized {
    if (createUserInfo.sukunimi == "error") {
      throw new TestingException("Testing error handling")
    } else if (oppijat.getOppijat.exists(_.hetu == createUserInfo.hetu)) {
      Left(KoskiErrorCategory.conflict.hetu("conflict"))
    } else {
      val newOppija = oppijat.oppija(createUserInfo.sukunimi, createUserInfo.etunimet, createUserInfo.hetu.getOrElse(throw new IllegalArgumentException("Hetu puuttuu")), kutsumanimi = Some(createUserInfo.kutsumanimi))
      Right(newOppija.oid)
    }
  }

  def findOppijaByOid(henkilöOid: String): Option[OppijaHenkilö] = {
    findHenkilötiedot(henkilöOid).map(_.henkilö).map(toOppijaHenkilö)
  }

  def findMasterOppija(henkilöOid: String): Option[OppijaHenkilö] = {
    findHenkilötiedot(henkilöOid).flatMap(_.master).map(toOppijaHenkilö)
  }

  private def toOppijaHenkilö(henkilö: TäydellisetHenkilötiedot) = {
    OppijaHenkilö(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, henkilö.hetu, henkilö.syntymäaika, Some("FI"), None, 0)
  }

  override def findKäyttäjäByOid(oid: String): Option[KäyttäjäHenkilö] = {
    findHenkilötiedot(oid).map(henkilö => KäyttäjäHenkilö(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, None, None))
  }

  protected def findHenkilötiedot(id: String): Option[TäydellisetHenkilötiedotWithMasterInfo] = synchronized {
    oppijat.getOppijat.find(_.oid == id)
  }

  def findOppijatByOids(oids: List[String]): List[OppijaHenkilö] = {
    oids.flatMap(findOppijaByOid)
  }

  def findOrCreate(createUserInfo: UusiHenkilö): Either[HttpStatus, OppijaHenkilö] = {
    def oidFrom(oppijat: Option[OppijaHenkilö]): Either[HttpStatus, Henkilö.Oid] = {
      oppijat match {
        case Some(oppija) =>
          Right(oppija.oidHenkilo)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(KoskiErrorCategory.internalError())
      }
    }
    val UusiHenkilö(Some(hetu), sukunimi, etunimet, kutsumanimi, _, _) = createUserInfo
    val oid = Hetu.validate(hetu, acceptSynthetic = true).right.flatMap { hetu =>
      create(createUserInfo).left.flatMap {
        case HttpStatus(409, _) => oidFrom(findOppijaByHetu(hetu))
        case HttpStatus(_, _) => throw new RuntimeException("Unreachable match arm: HTTP status code must be 409")
      }
    }
    oid.right.map(oid => findOppijaByOid(oid).get)
  }

  def modify(oppija: TäydellisetHenkilötiedotWithMasterInfo): Unit = synchronized {
    oppijat = new MockOppijat(oppijat.getOppijat.map { o =>
      if (o.oid == oppija.oid)
        o.copy(henkilö = o.henkilö.copy(etunimet = oppija.etunimet, kutsumanimi = oppija.kutsumanimi, sukunimi = oppija.sukunimi), master = oppija.master)
      else o
    })
  }

  def modify(oppija: TäydellisetHenkilötiedot): Unit = modify(TäydellisetHenkilötiedotWithMasterInfo(oppija, None))

  def reset(): Unit = synchronized {
    oppijat = new MockOppijat(MockOppijat.defaultOppijat)
  }

  private def searchString(oppija: TäydellisetHenkilötiedot) = {
    oppija.toString.toUpperCase
  }

  override def organisaationSähköpostit(organisaatioOid: String, ryhmä: String): List[String] =
    MockUsers.users.filter(_.käyttöoikeudet.exists { case (oid, käyttöoikeusryhmä) =>
      käyttöoikeusryhmä match {
        case o: OrganisaationKäyttöoikeusryhmä => organisaatioOid == oid && o.tunniste == ryhmä
        case _ => false
      }
    }).map(_.username + "@example.com")

  override def findOppijaByHetu(hetu: String): Option[OppijaHenkilö] = synchronized {
    oppijat.getOppijat.find(_.hetu.contains(hetu)).map(h => h.master.map(toOppijaHenkilö).getOrElse(toOppijaHenkilö(h.henkilö)))
  }

  override def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid] = synchronized {
    MockOppijat.defaultOppijat.diff(oppijat.getOppijat).map(_.oid)
  }
}
