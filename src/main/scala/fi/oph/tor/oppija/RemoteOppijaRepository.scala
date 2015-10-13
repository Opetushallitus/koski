package fi.oph.tor.oppija

import fi.oph.tor.http.{Http, VirkailijaHttpClient}
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
import org.http4s._

import scalaz.concurrent.Task

class RemoteOppijaRepository(henkilöPalveluClient: VirkailijaHttpClient) extends OppijaRepository with EntityDecoderInstances {
  override def findOppijat(query: String): List[Oppija] = henkilöPalveluClient.httpClient
    .apply(Task(Request(uri = henkilöPalveluClient.virkailijaUriFromString("/authentication-service/resources/henkilo?no=true&q=" + query))))(Http.parseJson[AuthenticationServiceUserQueryResult])
    .results.map { result => Oppija(result.oidHenkilo, result.sukunimi, result.etunimet, result.hetu)}

  override def findById(id: String): Option[Oppija] = findOppijat(id).headOption

  override def create(oppija: CreateOppija): OppijaCreationResult = {
    val task: Task[Request] = Request(
      uri = henkilöPalveluClient.virkailijaUriFromString("/authentication-service/resources/henkilo"),
      method = Method.POST
    ).withBody(new AuthenticationServiceCreateUser(oppija))(json4sEncoderOf[AuthenticationServiceCreateUser])

   henkilöPalveluClient.httpClient(task) {
      case (200, oid) => Created(oid)
      case (400, "socialsecuritynr.already.exists") => Failed(409, "socialsecuritynr.already.exists")
      case (status, text) => throw new RuntimeException(status + ": " + text)
    }
  }
}

case class AuthenticationServiceUserQueryResult(totalCount: Integer, results: List[AuthenticationServiceUser])
case class AuthenticationServiceUser(oidHenkilo: String, sukunimi: String, etunimet: String, hetu: String)
case class AuthenticationServiceCreateUser(hetu: String, henkiloTyyppi: String, sukunimi: String, etunimet: String, kutsumanimi: String) {
  def this(oppija: CreateOppija) = this(oppija.hetu, "OPPIJA", oppija.sukunimi, oppija.etunimet, oppija.kutsumanimi)
}

