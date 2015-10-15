package fi.oph.tor.oppija

import fi.oph.tor.http.{HttpError, Http, VirkailijaHttpClient}
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
import org.http4s._

import scalaz.concurrent.Task

class RemoteOppijaRepository(henkilöPalveluClient: VirkailijaHttpClient) extends OppijaRepository with EntityDecoderInstances {
  override def findOppijat(query: String): List[Oppija] = {
    henkilöPalveluClient.httpClient
      .apply(Request(uri = henkilöPalveluClient.virkailijaUriFromString("/authentication-service/resources/henkilo?no=true&count=0&q=" + query)))(Http.parseJson[AuthenticationServiceUserQueryResult])
      .results.map(toOppija)
  }

  override def findById(id: String): Option[Oppija] = {
    henkilöPalveluClient.httpClient(Request(uri = henkilöPalveluClient.virkailijaUriFromString("/authentication-service/resources/henkilo/" + id)))(Http.parseJsonOptional[AuthenticationServiceUser])
      .map(toOppija)
  }

  override def create(oppija: CreateOppija) = {
    val task: Task[Request] = Request(
      uri = henkilöPalveluClient.virkailijaUriFromString("/authentication-service/resources/henkilo"),
      method = Method.POST
    ).withBody(new AuthenticationServiceCreateUser(oppija))(json4sEncoderOf[AuthenticationServiceCreateUser])

   henkilöPalveluClient.httpClient(task) {
      case (200, oid) => Right(oid)
      case (400, "socialsecuritynr.already.exists") => Left(HttpError(409, "socialsecuritynr.already.exists"))
      case (status, text) => throw new RuntimeException(status + ": " + text)
    }
  }

  private def toOppija(user: AuthenticationServiceUser) = Oppija(user.oidHenkilo, user.sukunimi, user.etunimet, user.hetu)
}

case class AuthenticationServiceUserQueryResult(totalCount: Integer, results: List[AuthenticationServiceUser])
case class AuthenticationServiceUser(oidHenkilo: String, sukunimi: String, etunimet: String, hetu: String)
case class AuthenticationServiceCreateUser(hetu: String, henkiloTyyppi: String, sukunimi: String, etunimet: String, kutsumanimi: String) {
  def this(oppija: CreateOppija) = this(oppija.hetu, "OPPIJA", oppija.sukunimi, oppija.etunimet, oppija.kutsumanimi)
}

