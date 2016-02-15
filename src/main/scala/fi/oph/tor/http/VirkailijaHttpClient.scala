package fi.oph.tor.http

import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.Uri._
import org.http4s.client.{Client, blaze}

object VirkailijaHttpClient {
  def apply(username: String, password: String, opintoPolkuVirkailijaUrl: Path, serviceUrl: String, useCas: Boolean = true) = {
    val blazeHttpClient = blaze.PooledHttp1Client()
    val casClient = new CasClient(opintoPolkuVirkailijaUrl, blazeHttpClient)
    val casAuthenticatingClient: Client = if (useCas) {
      CasAuthenticatingClient(casClient, CasParams(serviceUrl, username, password), blazeHttpClient)
    } else {
      ClientWithBasicAuthentication(blazeHttpClient, username, password)
    }

    Http(opintoPolkuVirkailijaUrl, casAuthenticatingClient)
  }
}
