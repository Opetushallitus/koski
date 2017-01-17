package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.http.Http
import fi.vm.sade.utils.cas.CasClient

class ServiceTicketValidator(config: Config) {
  val casClient = new CasClient(config.getString("opintopolku.virkailija.url"), Http.newClient)

  def validateServiceTicket(service: String, ticket: String) = {
    val username = casClient.validateServiceTicket(service)(ticket).run
    username
  }
}
