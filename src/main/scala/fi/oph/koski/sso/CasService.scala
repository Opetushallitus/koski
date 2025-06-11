package fi.oph.koski.sso

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.{Http, OpintopolkuCallerId}
import fi.oph.koski.log.Logging
import fi.oph.koski.userdirectory.Password
import fi.oph.koski.cas.CasClient.Username
import fi.oph.koski.cas.{CasAuthenticationException, CasClient, CasUser}
import fi.oph.koski.henkilo.Hetu

import scala.concurrent.duration.DurationInt

case class KansalaisenTunnisteet(hetu: Option[String], oppijaOid: Option[String])

class CasService(config: Config) extends Logging {
  private val ATTRIBUTE_NAME_HETU = "nationalIdentificationNumber"
  private val ATTRIBUTE_NAME_EIDAS_ID = "personIdentifier"
  private val ATTRIBUTE_NAME_PERSON_OID = "personOid"


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

    val hetuAttempt = oppijaAttributes.get(ATTRIBUTE_NAME_HETU).filter(_.nonEmpty)
    val oppijaOidAttempt = oppijaAttributes.get(ATTRIBUTE_NAME_PERSON_OID).filter(_.nonEmpty)

    // Lue testiympäristöissä hetu eri kentästä tarvittaessa;
    //
    // Testi-digilompakkoa käytettäessä hetu tulee tällä hetkellä eri polussa, ja koska oppija-cas välittää suomi.fi -tunnistuksen datat
    // sellaisenaan ja suomi.fi:ssä on päätetty vaihtaa kentän nimeä, niin tätä pitää Koskenkin tukea.
    //
    // Pidemmällä aikavälillä näiden eri nimisten kenttien yhdistämisen pitäisi ehkä olla ennemmin otuva-palvelun vastuulla, mistä ovat sen
    // kehittäjät tietoisia.
    //
    // Tuotannossa tätä ei voi laittaa päälle, koska digilompakkotunnistusväline on vielä työn alla. On myös vielä yleisesti epäselvää,
    // miten kansainväliset kirjautumiset tehdään ja missä vaiheessa ja millä tavalla varmistetaan, että ei esim. ikinä käsitellä
    // suomalaisena hetuna muun maan kansallista hetua.
    if (!Environment.isProdEnvironment(config) && hetuAttempt.isEmpty) {
      // Huom. tässä kentässä palautuu eIDAS-tunniste kun käytetään eIDAS-kirjautumista, joka ei ole validi hetu
      val suomiFiHetuAttempt = oppijaAttributes
        .get(ATTRIBUTE_NAME_EIDAS_ID)
        .filter(_.nonEmpty)
        .filter(h => Hetu.validFormat(h).isRight)
      KansalaisenTunnisteet(suomiFiHetuAttempt, oppijaOidAttempt)
    } else {
      KansalaisenTunnisteet(hetuAttempt, oppijaOidAttempt)
    }
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
