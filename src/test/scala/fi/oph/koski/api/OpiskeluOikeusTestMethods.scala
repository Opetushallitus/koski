package fi.oph.koski.api

import fi.oph.koski.http.{HttpSpecification, HttpStatus}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.jackson.JsonMethods
import org.scalatest.Matchers

trait OpiskeluoikeusTestMethods extends HttpSpecification with Matchers {
  import KoskiSchema.deserializationContext

  def lastOpiskeluoikeusByHetu(oppija: Henkilö, user: UserWithPassword = defaultUser): KoskeenTallennettavaOpiskeluoikeus = {
    val hetu = oppija match {
      case u: UusiHenkilö => u.hetu
      case h: Henkilötiedot => h.hetu.get
    }
    oppijaByHetu(hetu, user).tallennettavatOpiskeluoikeudet.last
  }
  def lastOpiskeluoikeus(oppijaOid: String, user: UserWithPassword = defaultUser): KoskeenTallennettavaOpiskeluoikeus = {
    oppija(oppijaOid, user).tallennettavatOpiskeluoikeudet.last
  }

  def getOpiskeluoikeudet(oppijaOid: String, user: UserWithPassword = defaultUser): Seq[Opiskeluoikeus] = {
    tryOppija(oppijaOid, user) match {
      case Right(oppija) => oppija.opiskeluoikeudet
      case Left(HttpStatus(404, _)) => Nil
      case Left(status) => throw new RuntimeException(status.toString)
    }
  }

  def getOpiskeluoikeus(oppijaOid: String, tyyppi: String) = {
    getOpiskeluoikeudet(oppijaOid).find(_.tyyppi.koodiarvo == tyyppi).get
  }

  def oppija(oppijaOid: String, user: UserWithPassword = defaultUser): Oppija = {
    authGet("api/oppija/" + oppijaOid, user) {
      verifyResponseStatusOk()
      readOppija
    }
  }

  def oppijaByHetu(hetu: String, user: UserWithPassword = defaultUser): Oppija = {
    authGet("api/henkilo/hetu/" + hetu, user) {
      verifyResponseStatusOk()
      val oid = JsonSerializer.parse[List[HenkilötiedotJaOid]](body).head.oid
      oppija(oid, user)
    }
  }

  def tryOppija(oppijaOid: String, user: UserWithPassword = defaultUser): Either[HttpStatus, Oppija] = {
    authGet("api/oppija/" + oppijaOid, user) {
      response.status match {
        case 200 => Right(readOppija)
        case status => Left(HttpStatus(status, Nil))
      }
    }
  }

  def readOppija = {
    SchemaValidatingExtractor.extract[Oppija](JsonMethods.parse(body)).right.get
  }

  def readOpiskeluoikeus = {
    SchemaValidatingExtractor.extract[Opiskeluoikeus](JsonMethods.parse(body)).right.get
  }

  def kansalainenLoginHeaders[T](hetu: String): List[(String, String)] = {
    get("user/shibbolethlogin", headers = List("hetu" -> hetu, "security" -> "mock")) {
      verifyResponseStatusOk(302)
      val cookie = response.headers("Set-Cookie").find(x => x.startsWith("koskiOppija")).get
      List("Cookie" -> cookie)
    }
  }
}
