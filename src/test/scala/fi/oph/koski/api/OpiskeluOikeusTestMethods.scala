package fi.oph.koski.api

import fi.oph.koski.http.{HttpSpecification, HttpStatus}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema._
import org.json4s.JValue
import org.scalatest.Matchers

trait OpiskeluoikeusTestMethods extends HttpSpecification with Matchers {
  import KoskiSchema.deserializationContext

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

  def tryOppija(oppijaOid: String, user: UserWithPassword = defaultUser): Either[HttpStatus, Oppija] = {
    authGet("api/oppija/" + oppijaOid, user) {
      response.status match {
        case 200 => Right(readOppija)
        case status => Left(HttpStatus(status, Nil))
      }
    }
  }

  def readOppija = {
    SchemaBasedJsonDeserializer.extract[Oppija](Json.read[JValue](body)).right.get
  }

  def readOpiskeluoikeus = {
    SchemaBasedJsonDeserializer.extract[Opiskeluoikeus](Json.read[JValue](body)).right.get
  }
}
