package fi.oph.tor.oppija

import fi.oph.tor.http.VirkailijaHttpClient
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s._
import org.http4s._

import scalaz.concurrent.Task

class RemoteOppijaRepository(henkilöPalveluClient: VirkailijaHttpClient) extends OppijaRepository with EntityDecoderInstances {
  override def findOppijat(query: String): List[Oppija] = henkilöPalveluClient.httpClient
    .prepAs[AuthenticationServiceUserQueryResult](Request(uri = henkilöPalveluClient.uriFromString("/authentication-service/resources/henkilo?no=true&q=" + query)))(json4sOf[AuthenticationServiceUserQueryResult])
    .run.results.map { result => Oppija(result.oidHenkilo, result.sukunimi, result.etunimet, result.hetu)}

  override def findById(id: String): Option[Oppija] = findOppijat(id).headOption

  override def create(oppija: CreateOppija): OppijaCreationResult = {
    val task: Task[Request] = Request(
      uri = henkilöPalveluClient.uriFromString("/authentication-service/resources/henkilo"),
      method = Method.POST
    ).withBody(new AuthenticationServiceCreateUser(oppija))(json4sEncoderOf[AuthenticationServiceCreateUser])

    val response: Response = henkilöPalveluClient.httpClient(task).run
    val responseText: String = response.as[String].run

    (response.status.code, responseText) match {
      case (200, oid) => Created(oid)
      case (400, "socialsecuritynr.already.exists") => Failed(409, "socialsecuritynr.already.exists")
      case _ => throw new RuntimeException(response.toString)
    }
  }
}

case class AuthenticationServiceUserQueryResult(totalCount: Integer, results: List[AuthenticationServiceUser])
case class AuthenticationServiceUser(oidHenkilo: String, sukunimi: String, etunimet: String, hetu: String)
case class AuthenticationServiceCreateUser(hetu: String, henkiloTyyppi: String, sukunimi: String, etunimet: String, kutsumanimi: String) {
  def this(oppija: CreateOppija) = this(oppija.hetu, "OPPIJA", oppija.sukunimi, oppija.etunimet, oppija.kutsumanimi)
}

