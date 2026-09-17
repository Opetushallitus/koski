package fi.oph.koski.servlet

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.json.JsonSerializer
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, IOException, InputStream}
import java.nio.charset.StandardCharsets.UTF_8
import java.time.ZonedDateTime
import java.util.Properties
import scala.util.Using

class StatusApiServletSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec {
  "Status palauttaa merkkijonot ilman kirjautumista ja estää välimuistin" in {
    val packagedHash = Using.resource(getClass.getResourceAsStream("/buildversion.txt")) { stream =>
      val properties = new Properties()
      properties.load(stream)
      properties.getProperty("vcsRevision")
    }
    packagedHash should fullyMatch regex "[0-9a-f]{40}"
    get("api/status/") {
      verifyResponseStatusOk()
      val result = JsonSerializer.parse[Map[String, String]](body)
      result("gitCommitHash") should be(packagedHash)
      val serverTime = ZonedDateTime.parse(result("server time"))
      serverTime.getZone.getId should be("Europe/Helsinki")
      serverTime.toString should be(result("server time"))
      response.header("Cache-Control") should be("no-store, no-cache, must-revalidate")
      response.header("Pragma") should be("no-cache")
    }
  }

  "Buildversionin luku" - {
    "Lukee vcsRevision-kentän ja sulkee resurssin" in {
      var closed = false
      val stream = new ByteArrayInputStream(s"version=local\nvcsRevision=${"a" * 40}\n".getBytes(UTF_8)) {
        override def close(): Unit = { closed = true }
      }
      StatusApiServlet.readGitCommitHash(stream) should be("a" * 40)
      closed should be(true)
    }

    "Puuttuva tai tyhjä metadata palauttaa unknown" in {
      StatusApiServlet.readGitCommitHash(null) should be("unknown")
      Seq("", "version=local", "vcsRevision=", "vcsRevision=   ").foreach { content =>
        StatusApiServlet.readGitCommitHash(new ByteArrayInputStream(content.getBytes(UTF_8))) should be("unknown")
      }
    }

    "Lukuvirhe sulkee resurssin ja palauttaa unknown" in {
      var closed = false
      val stream = new InputStream {
        override def read(): Int = throw new IOException("unreadable")
        override def close(): Unit = { closed = true }
      }
      StatusApiServlet.readGitCommitHash(stream) should be("unknown")
      closed should be(true)
      StatusApiServlet.readGitCommitHash(throw new IOException("unavailable")) should be("unknown")
    }
  }
}
