package fi.oph.tor.http

import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.Uri._
import org.http4s.client.blaze
import org.http4s.client.blaze.BlazeClient

class VirkailijaHttpClient(username: String, password: String, opintoPolkuVirkailijaUrl: Path, serviceUrl: String) {
  private val blazeHttpClient: BlazeClient = blaze.defaultClient
  private val virkailijaUrl: Path = opintoPolkuVirkailijaUrl
  private val casClient = new CasClient(virkailijaUrl, blazeHttpClient)

  val httpClient = Http(opintoPolkuVirkailijaUrl, new CasAuthenticatingClient(casClient, CasParams(serviceUrl, username, password), blazeHttpClient))
}
