package fi.oph.koski.koskiuser

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.servlet.IndexServlet
import fi.oph.koski.sso.KoskiUserCookie
import fi.oph.koski.fixture.MockAsiointikieliServlet
import fi.oph.koski.userdirectory.{DirectoryClient, DirectoryUser, MockDirectoryClient, Password}
import fi.oph.koski.valpas.ValpasBootstrapServlet
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatra.ScalatraServlet
import org.scalatra.test.scalatest.ScalatraFreeSpec

import java.net.{InetAddress, URLEncoder}
import java.util.UUID

/**
 * Virkailijan kieli on asiointikieli oppijanumerorekisteristä, ja se ratkaistaan palvelimella
 * jokaisella pyynnöllä. Kieltä ei kirjoiteta lang-evästeeseen, joten selaimen vanha eväste ei voi
 * jäädä ohittamaan asiointikieltä. Frontend saa kielen sivun mukana, ks. HtmlNodes.
 */
class VirkailijanKielivalintaSpec extends ScalatraFreeSpec with TestEnvironment {

  // Testiympäristö ei ole Local eikä servlet-kontekstista löydy buildversion.txt:tä, jolloin sivun renderöinti
  // kaatuisi ScriptCacheBusterissa.
  addServlet(new IndexServlet()(KoskiApplicationForTests) {
    override lazy val buildVersion: Option[String] = Some("VirkailijanKielivalintaSpec")
  }, "/koski/*")

  // Valpas jakaa saman kirjautumisen ja istunnon Kosken kanssa, mutta sen HTML tulee staattisesta
  // servletistä, joten kieli välitetään SPA:n käynnistyskutsun window-propertiesissa.
  addServlet(new ValpasBootstrapServlet()(KoskiApplicationForTests), "/koski/valpas/localization/*")

  private val kaatuvaDirectoryClient = new DirectoryClient {
    def findUser(username: String): Option[DirectoryUser] = throw new RuntimeException("ONR ei vastaa")
    override def findAsiointikieli(user: AuthenticationUser): Option[String] = throw new RuntimeException("ONR ei vastaa")
    def authenticate(userid: String, wrappedPassword: Password): Boolean = false
  }

  addServlet(new ScalatraServlet {
    get("/kieli") {
      UserLanguage.resolveLanguage(
        AuthenticationUser("1.2.246.562.24.99999999494", "kaatuva", "kaatuva", None),
        kaatuvaDirectoryClient,
        request,
        KoskiApplicationForTests.config
      )
    }
  }, "/testi/*")

  // Paikallisen kehitysympäristön kielenvaihto, ks. MockAsiointikieliServlet.
  addServlet(new MockAsiointikieliServlet()(KoskiApplicationForTests), "/testi-kieli/*")

  private val ruotsinkielinen = MockUsers.ruotsinkielinenKatselija

  "Virkailijan kielivalinta" - {
    "kieli tulee asiointikielestä ja välittyy frontendille sivun mukana" in {
      withVirkailijaSession(ruotsinkielinen.ldapUser.oid, ruotsinkielinen.username) { koskiUser =>
        get("/koski/virkailija", headers = Map("Cookie" -> koskiUser)) {
          status should equal(200)
          body should include("""<html lang="sv"""")
          body should include("""window["koskiLang"] = "sv";""")
        }
      }
    }

    "virkailijalle ei aseteta lang-evästettä" in {
      withVirkailijaSession(ruotsinkielinen.ldapUser.oid, ruotsinkielinen.username) { koskiUser =>
        get("/koski/virkailija", headers = Map("Cookie" -> koskiUser)) {
          status should equal(200)
          langCookieValue should equal(None)
        }
      }
    }

    "selaimeen jäänyt lang-eväste ei ohita asiointikieltä" in {
      withVirkailijaSession(ruotsinkielinen.ldapUser.oid, ruotsinkielinen.username) { koskiUser =>
        get("/koski/virkailija", headers = Map("Cookie" -> s"$koskiUser; lang=fi")) {
          status should equal(200)
          body should include("""<html lang="sv"""")
          langCookieValue should equal(None)
        }
      }
    }

    "ilman asiointikieltä käytetään oletuskieltä" in {
      withVirkailijaSession("1.2.246.562.24.99999999494", "tuntematon-" + UUID.randomUUID()) { koskiUser =>
        get("/koski/virkailija", headers = Map("Cookie" -> koskiUser)) {
          status should equal(200)
          body should include("""<html lang="fi"""")
          langCookieValue should equal(None)
        }
      }
    }

    "asiointikielen haun epäonnistuminen ei kaada sivunlatausta" in {
      get("/testi/kieli") {
        status should equal(200)
        body should equal("fi")
      }
    }

    // Valppaan kirjautumissivu on kirjautumaton, joten ainoa signaali on kävijän oma kielivalinta.
    "mock-käyttäjän asiointikielen vaihto näkyy heti sivunlatauksessa" in {
      val kalle = MockUsers.kalle
      withVirkailijaSession(kalle.ldapUser.oid, kalle.username) { koskiUser =>
        try {
          get("/koski/virkailija", headers = Map("Cookie" -> koskiUser)) {
            body should include("""<html lang="fi"""")
          }
          post("/testi-kieli/sv", headers = Map("Cookie" -> koskiUser)) {
            status should equal(200)
          }
          get("/koski/virkailija", headers = Map("Cookie" -> koskiUser)) {
            body should include("""<html lang="sv"""")
          }
        } finally {
          MockDirectoryClient.clearAsiointikieliOverrides()
          KoskiApplicationForTests.directoryClient.invalidateCache()
        }
      }
    }

    "kirjautumattoman Valpas-kävijän oma kielivalinta kelpaa" in {
      get("/koski/valpas/localization/window-properties", headers = Map("Cookie" -> "lang=sv")) {
        status should equal(200)
        body should include(""""valpasLang":"sv"""")
      }
    }

    "Valpas saa saman kielen window-propertiesissa" in {
      withVirkailijaSession(ruotsinkielinen.ldapUser.oid, ruotsinkielinen.username) { koskiUser =>
        get("/koski/valpas/localization/window-properties", headers = Map("Cookie" -> s"$koskiUser; lang=fi")) {
          status should equal(200)
          body should include(""""valpasLang":"sv"""")
          langCookieValue should equal(None)
        }
      }
    }
  }

  private def withVirkailijaSession[A](oid: String, username: String)(f: String => A): A = {
    val ticket = "ST-" + UUID.randomUUID()
    val user = AuthenticationUser(oid, username, username, Some(ticket))
    KoskiApplicationForTests.koskiSessionRepository.store(ticket, user, InetAddress.getByName("127.0.0.1"), "VirkailijanKielivalintaSpec")
    try {
      f("koskiUser=" + URLEncoder.encode(JsonSerializer.writeWithRoot(KoskiUserCookie(ticket, kansalainen = false)), "UTF-8"))
    } finally {
      KoskiApplicationForTests.koskiSessionRepository.removeSessionByTicket(ticket)
    }
  }

  private def langCookieValue: Option[String] =
    response.headers.getOrElse("Set-Cookie", Nil)
      .find(_.startsWith("lang="))
      .map(_.substring("lang=".length).takeWhile(_ != ';'))
      .filter(_.nonEmpty)
}
