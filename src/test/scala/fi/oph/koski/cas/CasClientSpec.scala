package fi.oph.koski.cas

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import fi.oph.koski.TestEnvironment
import fi.oph.koski.cas.CasClient.CasVirkailija
import org.http4s.client.Client
import org.http4s.{HttpApp, Response, Status}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class CasClientSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  implicit val runtime: IORuntime = IORuntime.global

  private val casClient = new CasClient("http://localhost/cas", Client.fromHttpApp(HttpApp.notFound[IO]), "test")

  "Virkailijan service ticket -vastauksen dekoodaus" - {
    "lukee käyttäjänimen ja käyttäjätyypin" in {
      val response = Response[IO](Status.Ok).withEntity(
        """<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">
          |  <cas:authenticationSuccess>
          |    <cas:user>palvelukayttaja</cas:user>
          |    <cas:attributes>
          |      <cas:kayttajaTyyppi> PALVELU </cas:kayttajaTyyppi>
          |    </cas:attributes>
          |  </cas:authenticationSuccess>
          |</cas:serviceResponse>""".stripMargin
      )

      casClient.decodeVirkailija(response).unsafeRunSync() should equal(
        CasVirkailija("palvelukayttaja", Some("PALVELU"))
      )
    }
  }
}
