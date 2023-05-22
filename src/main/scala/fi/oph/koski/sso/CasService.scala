package fi.oph.koski.sso

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.{Http, OpintopolkuCallerId}
import fi.oph.koski.log.Logging
import fi.oph.koski.userdirectory.Password
import fi.vm.sade.utils.cas.CasClient.Username
import fi.vm.sade.utils.cas.{CasAuthenticationException, CasClient, CasUser}

import scala.concurrent.duration.DurationInt

class CasService(config: Config) extends Logging {
  private val casVirkailijaClient = new CasClient(
    config.getString("opintopolku.virkailija.url") + "/cas",
    Http.retryingClient("cas.serviceticketvalidation.virkailija"),
    OpintopolkuCallerId.koski
  )

  private val casOppijaClient = new CasClient(
    config.getString("opintopolku.oppija.url") + "/cas-oppija",
    Http.retryingClient("cas.serviceticketvalidation.oppija"),
    OpintopolkuCallerId.koski
  )

  private val mockUsernameForAllVirkailijaTickets = {
    if (Environment.isMockEnvironment(config) && config.hasPath("mock.casClient.usernameForAllVirkailijaTickets")) {
      Some(config.getString("mock.casClient.usernameForAllVirkailijaTickets"))
    } else {
      None
    }
  }

  def validateKansalainenServiceTicket(url: String, ticket: String): String = {
    val oppijaAttributes = Http.runIO(
      casOppijaClient
        .validateServiceTicketWithOppijaAttributes(url)(ticket)
        .timeout(10.seconds)
    )
    oppijaAttributes("nationalIdentificationNumber")
  }

  def validateVirkailijaServiceTicket(url: String, ticket: String): Username = {
    mockUsernameForAllVirkailijaTickets.getOrElse({
      Http.runIO(
        casVirkailijaClient
          .validateServiceTicketWithVirkailijaUsername(url)(ticket)
          .timeout(10.seconds)
      )
    })
  }

  def authenticateVirkailija(username: String, wrappedPassword: Password): Boolean = {
    try {
      Http.runIO(casVirkailijaClient.authenticateVirkailija(CasUser(username, wrappedPassword.password)))
    } catch {
      case e: CasAuthenticationException =>
        logger.warn(e.getMessage)
        false
    }
  }
}
