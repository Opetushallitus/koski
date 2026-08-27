package fi.oph.koski.koskiuser

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.servlet.IndexServlet
import fi.oph.koski.sso.KoskiUserCookie
import fi.oph.koski.userdirectory.{DirectoryClient, DirectoryUser, Password}
import fi.oph.koski.valpas.ValpasBootstrapServlet
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatra.ScalatraServlet
import org.scalatra.test.scalatest.ScalatraFreeSpec

import java.net.{InetAddress, URLEncoder}
import java.util.UUID

/**
 * Virkailijan asiointikieli haetaan vain CAS-tiketin validoinnin yhteydessä, joten selaimen sulkeminen hukkasi
 * istuntoevästeenä olevan lang-evästeen mutta säilytti pysyvän koskiUser-evästeen: istunto jatkui ilman uutta
 * tikettiä ja käyttöliittymä jäi suomeksi. Nämä testit kattavat puuttuvan evästeen täydentämisen.
 */
class VirkailijanKielivalintaSpec extends ScalatraFreeSpec with TestEnvironment {

  // Testiympäristö ei ole Local eikä servlet-kontekstista löydy buildversion.txt:tä, jolloin sivun renderöinti
  // kaatuisi ScriptCacheBusterissa.
  addServlet(new IndexServlet()(KoskiApplicationForTests) {
    override lazy val buildVersion: Option[String] = Some("VirkailijanKielivalintaSpec")
  }, "/koski/*")

  // Valpas jakaa saman lang-evästeen, saman kirjautumisen ja saman istunnon Kosken kanssa, mutta sen HTML tulee
  // staattisesta servletistä. Hook on siksi SPA:n käynnistyskutsussa.
  addServlet(new ValpasBootstrapServlet()(KoskiApplicationForTests), "/koski/valpas/localization/*")

  private val kaatuvaDirectoryClient = new DirectoryClient {
    def findUser(username: String): Option[DirectoryUser] = throw new RuntimeException("ONR ei vastaa")
    def authenticate(userid: String, wrappedPassword: Password): Boolean = false
  }

  addServlet(new ScalatraServlet {
    get("/kieli") {
      UserLanguage.setLanguageCookieFromUserIfNecessary(
        AuthenticationUser("1.2.246.562.24.99999999494", "kaatuva", "kaatuva", None),
        kaatuvaDirectoryClient,
        request,
        response
      ).toString
    }
  }, "/testi/*")

  private val ruotsinkielinen = MockUsers.ruotsinkielinenKatselija

  "Virkailijan kielivalinta" - {
    "lang-eväste täydennetään asiointikielestä, kun istunto jatkuu ilman evästettä" in {
      withVirkailijaSession(ruotsinkielinen.ldapUser.oid, ruotsinkielinen.username) { koskiUser =>
        get("/koski/virkailija", headers = Map("Cookie" -> koskiUser)) {
          status should equal(200)
          langCookieValue should equal(Some("sv"))
        }
      }
    }

    "täydennetty kieli näkyy heti samalla sivulatauksella" in {
      withVirkailijaSession(ruotsinkielinen.ldapUser.oid, ruotsinkielinen.username) { koskiUser =>
        get("/koski/virkailija", headers = Map("Cookie" -> koskiUser)) {
          // Vastaukseen asetettu eväste ei näy saman pyynnön request.cookiesissa, joten kieli välitetään
          // renderöintiin request-attribuutilla. Ilman sitä ensimmäinen sivulataus jäisi suomeksi.
          body should include("""<html lang="sv"""")
        }
      }
    }

    "olemassa olevaa lang-evästettä ei ylikirjoiteta" in {
      withVirkailijaSession(ruotsinkielinen.ldapUser.oid, ruotsinkielinen.username) { koskiUser =>
        get("/koski/virkailija", headers = Map("Cookie" -> s"$koskiUser; lang=fi")) {
          status should equal(200)
          langCookieValue should equal(None)
          body should include("""<html lang="fi"""")
        }
      }
    }

    "evästettä ei aseteta, jos asiointikieltä ei saada haettua" in {
      withVirkailijaSession("1.2.246.562.24.99999999494", "tuntematon-" + UUID.randomUUID()) { koskiUser =>
        get("/koski/virkailija", headers = Map("Cookie" -> koskiUser)) {
          // Väärän arvon kirjoittaminen tekisi ohimenevästä virheestä pysyvän: eväste olisi jatkossa olemassa,
          // eikä täydennys enää laukeaisi. Ks. UserLanguage.setLanguageCookieFromUserIfNecessary
          langCookieValue should equal(None)
        }
      }
    }


    "Valpas täydentää evästeen samalla tavalla" in {
      withVirkailijaSession(ruotsinkielinen.ldapUser.oid, ruotsinkielinen.username) { koskiUser =>
        get("/koski/valpas/localization/window-properties", headers = Map("Cookie" -> koskiUser)) {
          status should equal(200)
          langCookieValue should equal(Some("sv"))
        }
      }
    }

    "evästettä ei aseteta, jos asiointikielen haku heittää poikkeuksen" in {
      get("/testi/kieli") {
        status should equal(200)
        body should equal("None")
        langCookieValue should equal(None)
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
