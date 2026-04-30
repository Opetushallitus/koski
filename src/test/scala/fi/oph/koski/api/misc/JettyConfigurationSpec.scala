package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI
import java.net.http.{HttpClient => JdkHttpClient, HttpRequest, HttpResponse}

class JettyConfigurationSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  "URL-polut" - {
    "OID polulla toimii" in {
      val oid = KoskiSpecificMockOppijat.eero.oid
      authGet(s"api/oppija/${oid}") {
        response.status should (be(200) or be(404))
        response.status should not be 400
        response.status should not be 500
      }
    }

    "Prosenttikoodattu kauttaviiva polulla ei aiheuta virhettä" in {
      authGet("api/editor/koodit/koskiopiskeluoikeudentila%2Fvalmistunut") {
        response.status should not be 400
        response.status should not be 500
      }
    }
  }

  "Staattiset resurssit" - {
    "Resurssit tarjotaan oikein alipoluista" in {
      get("images/loader.svg") { verifyResponseStatusOk() }
      get("js/koski-main.js") { verifyResponseStatusOk() }
    }

    "Hakemistolistaus on estetty" in {
      get("js/") { verifyResponseStatusOk(403) }
    }
  }

  "Gzip-pakkaus" - {
    // Käytetään JDK:n HttpClientiä, koska Scalatra-testikehyksen alla oleva
    // Apache HttpComponents purkaa gzip-vastaukset läpinäkyvästi ja poistaa
    // Content-Encoding-headerin ennen kuin testi näkee vastauksen.
    val jdkClient = JdkHttpClient.newHttpClient()

    def fetchRaw(path: String, acceptEncoding: Option[String]): HttpResponse[Array[Byte]] = {
      val builder = HttpRequest.newBuilder(URI.create(s"$baseUrl/$path"))
      acceptEncoding.foreach(enc => builder.header("Accept-Encoding", enc))
      jdkClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray())
    }

    "Staattisille resursseille palautetaan gzip-pakattu sisältö, kun client sitä pyytää" in {
      val res = fetchRaw("js/koski-main.js", Some("gzip"))
      res.statusCode should be(200)
      res.headers.firstValue("Content-Encoding").orElse("") should equal("gzip")
    }

    "Pakkausta ei tehdä, jos client ei sitä pyydä" in {
      val res = fetchRaw("js/koski-main.js", None)
      res.statusCode should be(200)
      res.headers.firstValue("Content-Encoding").isPresent should be(false)
    }

    "Pakkausta ei tehdä konfiguroitujen polkujen ulkopuolella" in {
      val res = fetchRaw("images/loader.svg", Some("gzip"))
      res.statusCode should be(200)
      res.headers.firstValue("Content-Encoding").isPresent should be(false)
    }
  }

  "Ei-ASCII query-parametrit" - {
    "Skandit query-parametrin nimissä toimivat" in {
      authGet("api/opiskeluoikeus?opiskeluoikeusPäättynytAikaisintaan=2016-01-01&opiskeluoikeusPäättynytViimeistään=2016-12-31") {
        response.status should not be 400
      }
    }

    "Skandit query-parametrin arvoissa toimivat" in {
      authGet("api/opiskeluoikeus?opiskeluoikeusAlkanutAikaisintaan=2016-01-01&opiskeluoikeusAlkanutViimeistään=2016-12-31") {
        response.status should not be 400
      }
    }
  }
}
