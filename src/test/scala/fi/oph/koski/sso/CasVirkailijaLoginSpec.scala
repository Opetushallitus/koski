package fi.oph.koski.sso

import fi.oph.koski.cas.CasClient.CasVirkailija
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatra.test.scalatest.ScalatraFreeSpec

import java.util.UUID

class CasVirkailijaLoginSpec
  extends ScalatraFreeSpec
    with TestEnvironment {

  @volatile private var casResponse: CasVirkailija = _

  private val stubCasService = new CasService(KoskiApplicationForTests.config) {
    override def validateVirkailijaServiceTicket(url: String, ticket: String): CasVirkailija =
      casResponse
  }

  private val casServlet = new CasServlet()(KoskiApplicationForTests) {
    override protected val casService: CasService = stubCasService
  }

  addServlet(casServlet, "/koski/cas/*")

  private val username = MockUsers.kalle.username

  "CAS-virkailijakirjautuminen" - {
    "hylkää palvelukäyttäjän" in {
      tryLogin(username, Some("PALVELU")) { ticket =>
        assertLoginRejected(ticket)
      }
    }

    "hylkää palvelukäyttäjän, vaikka käyttäjätyyppi sisältäisi välilyöntejä tai eri kirjainkokoja" in {
      tryLogin(username, Some(" palvELU ")) { ticket =>
        assertLoginRejected(ticket)
      }
    }

    "kirjautuminen onnistuu, kun käyttäjätyyppi on VIRKAILIJA" in {
      tryLogin(username, Some("VIRKAILIJA")) { ticket =>
        assertLoginSuccess(ticket)
      }
    }

    "hylkää virkailijan, jonka käyttäjätietoja ei löydy" in {
      tryLogin("tuntematon-" + UUID.randomUUID(), Some("VIRKAILIJA")) { ticket =>
        assertLoginRejected(ticket)
      }
    }
  }

  private def assertLoginSuccess(ticket: String): Unit = {
    isRedirectedToLogin should equal(false)
    koskiUserCookieIsSet should equal(true)
    koskiSessionExists(ticket) should equal(Some(username))
  }

  private def assertLoginRejected(ticket: String): Unit = {
    isRedirectedToLogin should equal(true)
    koskiUserCookieIsSet should equal(false)
    koskiSessionExists(ticket) should equal(None)
  }

  private def tryLogin[A](username: String, kayttajaTyyppi: Option[String])(assertLoginResult: String => A): A = {
    val ticket = "ST-" + UUID.randomUUID()
    casResponse = CasVirkailija(username, kayttajaTyyppi)
    try {
      get("/koski/cas/virkailija", "ticket" -> ticket) {
        assertLoginResult(ticket)
      }
    } finally {
      KoskiApplicationForTests.koskiSessionRepository.removeSessionByTicket(ticket)
    }
  }

  private def isRedirectedToLogin: Boolean = {
    status should equal(302)
    response.header.get("Location").exists(_.endsWith("/koski/login"))
  }

  private def koskiUserCookieIsSet: Boolean =
    response.headers.getOrElse("Set-Cookie", Nil).exists(_.startsWith("koskiUser="))

  private def koskiSessionExists(ticket: String): Option[String] =
    KoskiApplicationForTests.koskiSessionRepository.getUserByTicket(ticket).map(_.username)
}
