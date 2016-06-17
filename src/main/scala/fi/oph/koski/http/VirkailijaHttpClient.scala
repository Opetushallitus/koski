package fi.oph.koski.http

import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.client.Client

object VirkailijaHttpClient {
  def apply(username: String, password: String, opintoPolkuVirkailijaUrl: String, serviceUrl: String, useCas: Boolean = true) = {
    val blazeHttpClient = Http.newClient
    val casClient = new CasClient(opintoPolkuVirkailijaUrl, blazeHttpClient)
    val casAuthenticatingClient: Client = if (useCas) {
      CasAuthenticatingClient(casClient, CasParams(serviceUrl, username, password), blazeHttpClient, "koski")
    } else {
      ClientWithBasicAuthentication(blazeHttpClient, username, password)
    }

    Http(opintoPolkuVirkailijaUrl, casAuthenticatingClient)
  }
}
