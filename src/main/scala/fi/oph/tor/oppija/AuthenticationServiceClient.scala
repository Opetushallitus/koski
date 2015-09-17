package fi.oph.tor.oppija

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._
import fi.oph.tor.json.Json4sHttp4s.json4sOf
import fi.vm.sade.utils.cas.{CasAbleClient, CasClient, CasParams}
import org.http4s.Uri._
import org.http4s._
import org.http4s.client.blaze
import org.http4s.client.blaze.BlazeClient

class AuthenticationServiceClient(username: String, password: String, opintoPolkuVirkailijaUrl: Path) extends OppijaRepository {
  private val blazeHttpClient: BlazeClient = blaze.defaultClient
  private val virkailijaUrl: Path = opintoPolkuVirkailijaUrl
  private val casClient = new CasClient(virkailijaUrl, blazeHttpClient)
  private val authenticationServiceClient = new CasAbleClient(casClient, CasParams("/authentication-service", username, password), blazeHttpClient)
  override def findOppijat(query: String): List[Oppija] = authenticationServiceClient
    .prepAs[AuthenticationServiceUserQueryResult](Request(uri = Uri.fromString(virkailijaUrl + "/authentication-service/resources/henkilo?q=" + query).toOption.get))(json4sOf[AuthenticationServiceUserQueryResult])
    .run.results.map { result => Oppija(result.sukunimi, result.etunimet, result.hetu)}

}

case class AuthenticationServiceUserQueryResult(totalCount: Integer, results: List[AuthenticationServiceUser])
case class AuthenticationServiceUser(sukunimi: String, etunimet: String, hetu: String)

