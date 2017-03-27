package fi.oph.koski.api

import fi.oph.koski.http.{HttpSpecification, HttpStatus}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.scalatest.Matchers

trait OpiskeluoikeusTestMethods extends HttpSpecification with Matchers {
  import KoskiSchema.deserializationContext

  def lastOpiskeluoikeusByHetu(oppija: Hetullinen, user: UserWithPassword = defaultUser): KoskeenTallennettavaOpiskeluoikeus = {
    oppijaByHetu(oppija.hetu, user).tallennettavatOpiskeluoikeudet.last
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
      verifyResponseStatus(200)
      readOppija
    }
  }

  def oppijaByHetu(hetu: String, user: UserWithPassword = defaultUser): Oppija = {
    authGet("api/henkilo/hetu/" + hetu, user) {
      verifyResponseStatus(200)
      val oid = Json.read[List[HenkilötiedotJaOid]](body).head.oid
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
    SchemaValidatingExtractor.extract[Oppija](body).right.get
  }

  def readOpiskeluoikeus = {
    SchemaValidatingExtractor.extract[Opiskeluoikeus](body).right.get
  }
}
