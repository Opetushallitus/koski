package fi.oph.koski.userdirectory

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.typesafe.config.ConfigFactory
import fi.oph.koski.TestEnvironment
import fi.oph.koski.sso.CasService
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OpintopolkuDirectoryClientSpec
  extends AnyFreeSpec
    with TestEnvironment
    with Matchers
    with EitherValues
    with OptionValues
    with BeforeAndAfterAll {

  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues
  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |opintopolku.virkailija.url = "http://localhost:9877"
      |opintopolku.virkailija.username = "foo"
      |opintopolku.virkailija.password = "bar"
      |opintopolku.oppija.url = "http://localhost:9877"
      |otuvaTokenEndpoint="mock"
      |oppijanumerorekisteri.baseUrl="http://localhost:9877"
    """.stripMargin)

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9877))
  private val opintopolkuDirectoryClient = new OpintopolkuDirectoryClient(config, new CasService(config))

  "OpintopolkuDirectoryClient" - {
    "login fails" - {
      "when cas returns authentication_exceptions" in {
        opintopolkuDirectoryClient.authenticate("auth", Password("exception")) should be(false)
      }
      "when cas returns error.authentication.credentials.bad" in {
        opintopolkuDirectoryClient.authenticate("credentials", Password("bad")) should be(false)
      }
      "when cas returns error: Locked" in {
        opintopolkuDirectoryClient.authenticate("lukkonen", Password("locked")) should be(false)
      }
    }

    "login succeeds" - {
      "when cas returns ok" in {
        opintopolkuDirectoryClient.authenticate("hyva", Password("passu")) should be(true)
      }
    }
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    mockEndpoints
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  private def mockEndpoints = {
    val ticketUrl = "/cas/v1/tickets"

    wireMockServer.stubFor(
      post(urlPathEqualTo(ticketUrl))
        .withRequestBody(WireMock.matching("username=auth&password=exception"))
        .willReturn(status(400).withBody(write(Map("authentication_exceptions" -> Nil)))))

    wireMockServer.stubFor(
      post(urlPathEqualTo(ticketUrl))
        .withRequestBody(WireMock.matching("username=credentials&password=bad"))
        .willReturn(status(400).withBody(write(Map("error" -> "error.authentication.credentials.bad")))))

    wireMockServer.stubFor(
      post(urlPathEqualTo(ticketUrl))
        .withRequestBody(WireMock.matching("username=lukkonen&password=locked"))
        .willReturn(status(423)
        .withBody(write(Map("status" -> 423, "error" -> "Locked", "message" -> "Access Denied for user [lukkonen] from IP Address [127.0.0.1]", "path" -> "/cas/v1/tickets")))))

    wireMockServer.stubFor(
      post(urlPathEqualTo(ticketUrl))
        .withRequestBody(WireMock.matching("username=hyva&password=passu"))
        .willReturn(WireMock.created().withHeader("Location", "http://localhost/TGT-")))
  }
}

