package fi.oph.koski.servlet

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.util.BuildVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.IOException
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
    "Lukee vcsRevision-kentän ja poistaa ympäröivät välilyönnit" in {
      val metadata = Some(BuildVersion(Some("local"), Some(s"  ${"a" * 40}  "), Some("2026-09-18")))
      StatusApiServlet.gitCommitHashFromMetadata(metadata) should be("a" * 40)
    }

    "Puuttuva tai tyhjä metadata palauttaa unknown" in {
      StatusApiServlet.gitCommitHashFromMetadata(None) should be("unknown")
      Seq(None, Some(""), Some("   ")).foreach { revision =>
        StatusApiServlet.gitCommitHashFromMetadata(Some(BuildVersion(Some("local"), revision, None))) should be("unknown")
      }
    }

    "Metadatan lukuvirhe palauttaa unknown" in {
      StatusApiServlet.gitCommitHashFromMetadata(throw new IOException("unavailable")) should be("unknown")
    }
  }
}
