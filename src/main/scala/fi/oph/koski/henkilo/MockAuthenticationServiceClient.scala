package fi.oph.koski.henkilo

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{Futures, PostgresDriverWithJsonSupport, Tables}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{Käyttöoikeusryhmät, MockUsers}
import fi.oph.koski.log.Logging
import fi.oph.koski.oppija.{MockOppijat, TestingException}
import fi.oph.koski.schema.{Henkilö, TäydellisetHenkilötiedot}
import rx.lang.scala.Observable

class MockAuthenticationServiceClient(db: Option[DB] = None) extends AuthenticationServiceClient with Logging with Futures {
  private var oppijat = new MockOppijat(MockOppijat.defaultOppijat)

  val käyttöoikeusryhmät = Käyttöoikeusryhmät.käyttöoikeusryhmät.zipWithIndex.map {
    case (ryhmä, index) => new Käyttöoikeusryhmä(index, ryhmä.nimi)
  }

  override def käyttäjänKäyttöoikeusryhmät(oid: String): Observable[List[(String, Int)]] = {
    val ryhmät: List[(String, Int)] = MockUsers.users.find(_.oid == oid).toList.flatMap(_.käyttöoikeudet).map {
      case (organisaatioOid, ryhmä) =>
        (organisaatioOid, käyttöoikeusryhmät.find(_.name == ryhmä.nimi).get.id)
    }
    Observable.just(ryhmät)
  }

  def search(query: String): UserQueryResult = {
    if (query.toLowerCase.contains("error")) {
      throw new TestingException("Testing error handling")
    }
    val results = oppijat.getOppijat
      .filter(searchString(_).contains(query))
      .map(henkilö => UserQueryUser(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, Some(henkilö.hetu)))
    UserQueryResult(results.size, results)
  }

  def create(createUserInfo: CreateUser): Either[HttpStatus, String] = {
    if (createUserInfo.sukunimi == "error") {
      throw new TestingException("Testing error handling")
    } else if (oppijat.getOppijat.find { o => (Some(o.hetu) == createUserInfo.hetu) } .isDefined) {
      Left(KoskiErrorCategory.conflict.hetu("conflict"))
    } else {
      val newOppija = oppijat.oppija(createUserInfo.sukunimi, createUserInfo.etunimet, createUserInfo.hetu.getOrElse(throw new IllegalArgumentException("Hetu puuttuu")))
      Right(newOppija.oid)
    }
  }
  def findByOid(id: String): Option[User] = {
    val oppija: Option[TäydellisetHenkilötiedot] = oppijat.getOppijat.filter {_.oid == id}.headOption.orElse(findFromDb(id))
    oppija.map(henkilö => User(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, Some(henkilö.hetu), Some("FI"), None))
  }
  def findByOids(oids: List[String]): List[User] = {
    oids.flatMap(findByOid)
  }

  def findOrCreate(createUserInfo: CreateUser): Either[HttpStatus, User] = {
    def oidFrom(oppijat: List[UserQueryUser]): Either[HttpStatus, Henkilö.Oid] = {
      oppijat match {
        case List(oppija) =>
          Right(oppija.oidHenkilo)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(KoskiErrorCategory.internalError())
      }
    }
    val CreateUser(Some(hetu), sukunimi, etunimet, kutsumanimi, _, _) = createUserInfo
    val oid = Hetu.validate(hetu).right.flatMap { hetu =>
      create(createUserInfo).left.flatMap { case HttpStatus(409, _) =>
        oidFrom(search(hetu).results)
      }
    }
    oid.right.map(oid => findByOid(oid).get)
  }

  private def searchString(oppija: TäydellisetHenkilötiedot) = {
    oppija.toString.toUpperCase
  }

  def findFromDb(oid: String): Option[TäydellisetHenkilötiedot] = {
    runQuery(Tables.OpiskeluOikeudet.filter(_.oppijaOid === oid)).headOption.map { oppijaRow =>
      TäydellisetHenkilötiedot(oid, oid, oid, oid, oid, oppijat.äidinkieli, None)
    }
  }

  def runQuery[E, U](fullQuery: PostgresDriverWithJsonSupport.api.Query[E, U, Seq]): Seq[U] = {
    db.toSeq.flatMap { db => await(db.run(fullQuery.result)) }
  }
}
