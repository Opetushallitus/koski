package fi.oph.koski.henkilo

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{KoskiDatabaseMethods, PostgresDriverWithJsonSupport, Tables}
import fi.oph.koski.henkilo.AuthenticationServiceClient._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{Käyttöoikeusryhmät, MockUsers}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Henkilö, TäydellisetHenkilötiedot}


class MockAuthenticationServiceClientWithDBSupport(val db: DB) extends MockAuthenticationServiceClient with KoskiDatabaseMethods {
  def findFromDb(oid: String): Option[TäydellisetHenkilötiedot] = {
    runQuery(Tables.OpiskeluOikeudet.filter(_.oppijaOid === oid)).headOption.map { oppijaRow =>
      TäydellisetHenkilötiedot(oid, Some(oid), None, oid, oid, oid, oppijat.äidinkieli, None)
    }
  }

  def runQuery[E, U](fullQuery: PostgresDriverWithJsonSupport.api.Query[E, U, Seq]): Seq[U] = {
    runDbSync(fullQuery.result)
  }

  override protected def findHenkilötiedot(id: String): Option[TäydellisetHenkilötiedot] = {
    super.findHenkilötiedot(id).orElse(findFromDb(id))
  }
}

class MockAuthenticationServiceClient() extends AuthenticationServiceClient with Logging {
  // TODO: synchronize access

  var oppijat = new MockOppijat(MockOppijat.defaultOppijat)

  def resetFixtures = {
    oppijat = new MockOppijat(MockOppijat.defaultOppijat)
  }

  private def create(createUserInfo: UusiHenkilö): Either[HttpStatus, String] = {
    if (createUserInfo.sukunimi == "error") {
      throw new TestingException("Testing error handling")
    } else if (oppijat.getOppijat.exists(_.hetu == createUserInfo.hetu)) {
      Left(KoskiErrorCategory.conflict.hetu("conflict"))
    } else {
      val newOppija = oppijat.oppija(createUserInfo.sukunimi, createUserInfo.etunimet, createUserInfo.hetu.getOrElse(throw new IllegalArgumentException("Hetu puuttuu")))
      Right(newOppija.oid)
    }
  }

  def findOppijaByOid(henkilöOid: String): Option[OppijaHenkilö] = {
    findHenkilötiedot(henkilöOid).map(toOppijaHenkilö)
  }

  private def toOppijaHenkilö(henkilö: TäydellisetHenkilötiedot) = {
    OppijaHenkilö(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, henkilö.hetu, henkilö.syntymäaika, Some("FI"), None, 0)
  }

  override def findKäyttäjäByOid(oid: String): Option[KäyttäjäHenkilö] = {
    findHenkilötiedot(oid).map(henkilö => KäyttäjäHenkilö(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, None))
  }

  protected def findHenkilötiedot(id: String): Option[TäydellisetHenkilötiedot] = {
    oppijat.getOppijat.filter {_.oid == id}.headOption
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
      create(createUserInfo).left.flatMap { case HttpStatus(409, _) =>
        oidFrom(findOppijaByHetu(hetu))
      }
    }
    oid.right.map(oid => findOppijaByOid(oid).get)
  }

  def modify(oppija: TäydellisetHenkilötiedot): Unit = {
    oppijat = new MockOppijat(oppijat.getOppijat.map { o =>
      if (o.oid == oppija.oid)
        o.copy(etunimet = oppija.etunimet, kutsumanimi = oppija.kutsumanimi, sukunimi = oppija.sukunimi)
      else o
    })
  }

  def reset(): Unit = oppijat = new MockOppijat(MockOppijat.defaultOppijat)

  private def searchString(oppija: TäydellisetHenkilötiedot) = {
    oppija.toString.toUpperCase
  }

  override def organisaationYhteystiedot(ryhmä: String, organisaatioOid: String): List[Yhteystiedot] =
    MockUsers.users.filter(_.käyttöoikeudet.contains((organisaatioOid, Käyttöoikeusryhmät.vastuukäyttäjä))).map(u => Yhteystiedot(u.username + "@example.com"))

  override def findOppijaByHetu(hetu: String): Option[OppijaHenkilö] = oppijat.getOppijat.find(_.hetu.contains(hetu)).map(toOppijaHenkilö)

  override def findChangedOppijaOids(since: Long): List[String] = MockOppijat.defaultOppijat.diff(oppijat.getOppijat).map(_.oid)

  override def getKäyttöikeusRyhmät: Map[String, List[String]] =
    MockUsers.users.flatMap(u => u.käyttöoikeudet.map(_._2.nimi).map(ko => (ko, u.ldapUser.oid))).groupBy(_._1).mapValues(_.map(_._2))
}
