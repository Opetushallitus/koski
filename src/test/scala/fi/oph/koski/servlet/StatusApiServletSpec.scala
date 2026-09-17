package fi.oph.koski.servlet

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.util.BuildVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.IOException

class StatusApiServletSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec {
  "Status API" - {
    // buildversion.txt syntyy exec-pluginin process-resources-vaiheessa (pom.xml, frontend-profiili,
    // activeByDefault). mvn:llä ajettaessa resurssi on siis aina olemassa ja commitHash on oikea SHA.
    // IDEAsta ajettaessa käännös voi ohittaa exec-pluginin, jolloin resurssi puuttuu ja vastaus on
    // "unknown".
    "Vastaus ei vaadi kirjautumista, ei mene välimuistiin ja kertoo paketoidun Git-kommitin" in {
      get("api/status/") {
        verifyResponseStatusOk()
        response.header("Cache-Control") should be("no-store, no-cache, must-revalidate")
        response.header("Pragma") should be("no-cache")
        val status = JsonSerializer.parse[Map[String, String]](body)
        if (getClass.getResource("/buildversion.txt") == null) {
          status("commitHash") should be("unknown")
        } else {
          status("commitHash") should fullyMatch regex "[0-9a-f]{40}"
        }
      }
    }
  }

  "Kommittitunnisteen muodostaminen" - {
    "Lukee vcsRevision-kentän ja poistaa ympäröivät välilyönnit" in {
      val buildVersion = Some(BuildVersion(Some("local"), Some(s"  ${"a" * 40}  "), Some("2026-09-18")))
      StatusApiServlet.commitHashFromBuildVersion(buildVersion) should be("a" * 40)
    }

    "Puuttuva tai tyhjä metadata palauttaa unknown" in {
      StatusApiServlet.commitHashFromBuildVersion(None) should be("unknown")
      Seq(None, Some(""), Some("   ")).foreach { revision =>
        StatusApiServlet.commitHashFromBuildVersion(Some(BuildVersion(Some("local"), revision, None))) should be("unknown")
      }
    }

    "Metadatan lukuvirhe palauttaa unknown" in {
      StatusApiServlet.commitHashFromBuildVersion(throw new IOException("unavailable")) should be("unknown")
    }
  }
}
