package fi.oph.koski.http

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import fi.oph.koski.http.Http.UriInterpolator
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.freespec.AnyFreeSpec

class OAuth2ClientSpec extends AnyFreeSpec with BeforeAndAfterAll with  BeforeAndAfterEach {

  private val mockAuthorizationServer = new WireMockServer(wireMockConfig().port(9875))
  private val mockResourceServer = new WireMockServer(wireMockConfig().port(9876))


  private def setupMocks() = {
    mockAuthorizationServer.stubFor(WireMock.post("/oauth2/token").withRequestBody(WireMock.containing("client_id=testaaja&client_secret=test"))
      .willReturn(WireMock.okJson(JsonMethods.compact(JsonMethods.render("access_token" -> "t0ken1")))))

    mockResourceServer.stubFor(WireMock.get("/").withHeader("Authorization", WireMock.equalTo("Bearer t0ken1")).willReturn(WireMock.ok()))
  }
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    mockAuthorizationServer.start()
    mockResourceServer.start()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    mockAuthorizationServer.stop()
    mockResourceServer.stop()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    mockAuthorizationServer.resetAll()
    mockResourceServer.resetAll()
    setupMocks()
  }

  "OAuth2 client" - {
    "Hakee tokenin kerran ja tekee pyynnöt sillä" in {
      val factory = new OtuvaOAuth2ClientFactory(OtuvaOAuth2Credentials("testaaja", "test"), "http://localhost:9875/oauth2/token")
      val client = factory.apply("http://localhost:9876", Http.retryingClient("/"))
      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))
      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))
      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))
      mockAuthorizationServer.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/oauth2/token")))
      mockResourceServer.verify(3, WireMock.getRequestedFor(WireMock.urlEqualTo("/")).withHeader("Authorization", WireMock.equalTo("Bearer t0ken1")))
    }

    "Hakee uuden tokenin kun vanha expiroituu" in {
      val factory = new OtuvaOAuth2ClientFactory(OtuvaOAuth2Credentials("testaaja", "test"), "http://localhost:9875/oauth2/token")
      val client = factory.apply("http://localhost:9876", Http.retryingClient("/"))
      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))
      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))
      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))

      mockAuthorizationServer.stubFor(WireMock.post("/oauth2/token").withRequestBody(WireMock.containing("client_id=testaaja&client_secret=test"))
        .willReturn(WireMock.okJson(JsonMethods.compact(JsonMethods.render("access_token" -> "t0ken2")))))

      mockResourceServer.stubFor(WireMock.get("/").withHeader("Authorization", WireMock.equalTo("Bearer t0ken1")).willReturn(WireMock.status(401)))
      mockResourceServer.stubFor(WireMock.get("/").withHeader("Authorization", WireMock.equalTo("Bearer t0ken2")).willReturn(WireMock.ok()))

      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))
      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))
      Http.runIO(client.get(uri = uri"/")( (i, s, r) => s))

      mockAuthorizationServer.verify(2, WireMock.postRequestedFor(WireMock.urlEqualTo("/oauth2/token")))
      mockResourceServer.verify(4, WireMock.getRequestedFor(WireMock.urlEqualTo("/")).withHeader("Authorization", WireMock.equalTo("Bearer t0ken1")))
      mockResourceServer.verify(3, WireMock.getRequestedFor(WireMock.urlEqualTo("/")).withHeader("Authorization", WireMock.equalTo("Bearer t0ken2")))
    }

  }

}
