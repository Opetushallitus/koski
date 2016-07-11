package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.vm.sade.utils.cas.CasClient

class ServiceTicketValidator(config: Config) {
  val opintoPolkuVirkailijaUrl = config.getString("opintopolku.virkailija.url")
  val casClient = new CasClient(opintoPolkuVirkailijaUrl, Http.newClient)

  def validateServiceTicket(service: String, ticket: String) = {
    val username = casClient.validateServiceTicket(service)(ticket).run
    username
  }
}
