package fi.oph.koski.sso

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.{Http, OpintopolkuCallerId}
import fi.oph.koski.log.Logging
import fi.oph.koski.userdirectory.Password
import fi.oph.koski.cas.CasClient.Username
import fi.oph.koski.cas.{CasAuthenticationException, CasClient, CasUser}
import fi.oph.koski.sso.CasAttributes._

import scala.concurrent.duration.DurationInt

case class KansalaisenTunnisteet(hetu: Option[String], oppijaOid: Option[String], nimi: Option[String])

class CasService(config: Config) extends Logging {
  private val casVirkailijaClient = new CasClient(
    config.getString("opintopolku.virkailija.url") + "/cas",
    Http.nonRetryingClient("cas.serviceticketvalidation.virkailija"),
    OpintopolkuCallerId.koski
  )

  private val casOppijaClient = new CasClient(
    config.getString("opintopolku.oppija.url") + "/cas-oppija",
    Http.nonRetryingClient("cas.serviceticketvalidation.oppija"),
    OpintopolkuCallerId.koski
  )

  private val mockUsernameForAllVirkailijaTickets = {
    if (Environment.isMockEnvironment(config) && config.hasPath("mock.casClient.usernameForAllVirkailijaTickets")) {
      Some(config.getString("mock.casClient.usernameForAllVirkailijaTickets"))
    } else {
      None
    }
  }

  def validateKansalainenServiceTicket(url: String, ticket: String): KansalaisenTunnisteet = {
    val oppijaAttributes = Http.runIO(
      casOppijaClient
        .validateServiceTicketWithOppijaAttributes(url)(ticket)
        .timeout(10.seconds)
    )

    def getOppijaAttribute(a: String): Option[String] = oppijaAttributes.get(a).map(_.trim).filter(_.nonEmpty)

    val hetuAttempt = getOppijaAttribute(ATTRIBUTE_HETU)
    val oppijaOidAttempt = getOppijaAttribute(ATTRIBUTE_PERSON_OID)

    val etunimiAttempt = getOppijaAttribute(ATTRIBUTE_FIRST_NAME)
      .orElse(getOppijaAttribute(ATTRIBUTE_FIRST_NAME_ALT))
      .orElse(getOppijaAttribute(ATTRIBUTE_GIVEN_NAME))
      .getOrElse("")
    val sukunimiAttempt = getOppijaAttribute(ATTRIBUTE_SUKUNIMI)
      .orElse(getOppijaAttribute(ATTRIBUTE_FAMILY_NAME))
      .getOrElse("")
    val kokonimiAttempt = Some(etunimiAttempt + " " + sukunimiAttempt).map(_.trim).filter(_.nonEmpty)
      .orElse(getOppijaAttribute(ATTRIBUTE_DISPLAY_NAME))

    KansalaisenTunnisteet(hetuAttempt, oppijaOidAttempt, kokonimiAttempt)
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
