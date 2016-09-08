package fi.oph.koski.api

import fi.oph.koski.http.{HttpSpecification, HttpStatus}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema._
import org.scalatest.Matchers

trait OpiskeluOikeusTestMethods extends HttpSpecification with Matchers {
  def lastOpiskeluOikeus(oppijaOid: String, user: UserWithPassword = defaultUser): KoskeenTallennettavaOpiskeluoikeus = {
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
      Json.read[Oppija](body)
    }
  }

  def tryOppija(oppijaOid: String, user: UserWithPassword = defaultUser): Either[HttpStatus, Oppija] = {
    authGet("api/oppija/" + oppijaOid, user) {
      response.status match {
        case 200 => Right(Json.read[Oppija](body))
        case status => Left(HttpStatus(status, Nil))
      }
    }
  }
}
