package fi.oph.koski.todistus.tiedote

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.typesafe.config.ConfigFactory
import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

/**
 * Tiedote-POSTin uudelleenyrityskäyttäytyminen.
 *
 * Uudelleenyritys nojaa siihen, että tiedotuspalvelun rajapinta on idempotentti idempotencyKeyn kautta,
 * joten "sama idempotencyKey joka yrityksellä" -testi on tämän muutoksen turvallisuusperustelu koodina.
 */
class RemoteTiedotuspalveluClientSpec
  extends AnyFreeSpec
    with TestEnvironment
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  private val port = 9878
  private val path = "/api/v1/tiedote/kielitutkintotodistus"

  private val wireMockServer = new WireMockServer(wireMockConfig().port(port))

  private val client = new RemoteTiedotuspalveluClient(ConfigFactory.parseString(
    s"""
       |env = "unittest"
       |otuvaTokenEndpoint = "mock"
       |tiedote.baseUrl = "http://localhost:$port"
     """.stripMargin))

  private val oppijaOid = "1.2.246.562.24.12345678901"
  private val opiskeluoikeusOid = "1.2.246.562.15.12345678901"
  private val idempotencyKey = s"$opiskeluoikeusOid-initial"

  private val maxAttempts = TiedotuspalveluRetryBudget.maxRetries + 1

  private def send(): Either[fi.oph.koski.http.HttpStatus, Unit] =
    client.sendKielitutkintoTodistusTiedote(
      oppijanumero = oppijaOid,
      opiskeluoikeusOid = opiskeluoikeusOid,
      idempotencyKey = idempotencyKey,
      todistusBucket = Some("koski-tiedotuspalvelu-local"),
      todistusKey = Some("todistukset/tiedote.pdf"),
      kituExamineeDetails = None
    )

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.resetAll()
  }

  "RemoteTiedotuspalveluClient" - {
    "yrittää POSTia uudelleen ohimenevän virheen jälkeen" in {
      wireMockServer.stubFor(post(urlEqualTo(path))
        .inScenario("ohimenevä virhe")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(aResponse().withStatus(503))
        .willSetStateTo("epäonnistunut kerran"))
      wireMockServer.stubFor(post(urlEqualTo(path))
        .inScenario("ohimenevä virhe")
        .whenScenarioStateIs("epäonnistunut kerran")
        .willReturn(ok()))

      send() should equal(Right(()))
      wireMockServer.verify(2, postRequestedFor(urlEqualTo(path)))
    }

    "lähettää saman idempotencyKeyn jokaisella yrityksellä" in {
      wireMockServer.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(503)))

      send() should matchPattern { case Left(_) => }

      wireMockServer.verify(
        maxAttempts,
        postRequestedFor(urlEqualTo(path))
          .withRequestBody(matchingJsonPath("$.idempotencyKey", WireMock.equalTo(idempotencyKey)))
      )
    }

    "luovuttaa ja palauttaa Leftin kun palvelu on pysyvästi alhaalla" in {
      wireMockServer.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(503)))

      send() should matchPattern { case Left(_) => }
      wireMockServer.verify(maxAttempts, postRequestedFor(urlEqualTo(path)))
    }

    "ei yritä uudelleen 4xx-vastauksella" in {
      wireMockServer.stubFor(post(urlEqualTo(path)).willReturn(aResponse().withStatus(400)))

      send() should matchPattern { case Left(_) => }
      wireMockServer.verify(1, postRequestedFor(urlEqualTo(path)))
    }

    "ei yritä uudelleen onnistuneen vastauksen jälkeen" in {
      wireMockServer.stubFor(post(urlEqualTo(path)).willReturn(ok()))

      send() should equal(Right(()))
      wireMockServer.verify(1, postRequestedFor(urlEqualTo(path)))
    }

    // Naulaa rajapintasopimuksen tiedotuspalvelun kanssa: kenttien nimet ja arvot sellaisina kuin ne
    // lähtevät pyynnön rungossa. Mikään muu testi ei kata tätä – TiedotuspalveluClientSpec rakentaa
    // case classin suoraan ja workflow-testit tarkistavat mock-clientin, joka ei serialisoi pyyntöä.
    "sijoittaa kentät oikeisiin kohtiin pyynnön rungossa" in {
      wireMockServer.stubFor(post(urlEqualTo(path)).willReturn(ok()))

      send() should equal(Right(()))

      wireMockServer.verify(1, postRequestedFor(urlEqualTo(path))
        .withRequestBody(matchingJsonPath("$.oppijanumero", WireMock.equalTo(oppijaOid)))
        .withRequestBody(matchingJsonPath("$.opiskeluoikeusOid", WireMock.equalTo(opiskeluoikeusOid)))
        .withRequestBody(matchingJsonPath("$.idempotencyKey", WireMock.equalTo(idempotencyKey)))
        .withRequestBody(matchingJsonPath("$.todistusBucket", WireMock.equalTo("koski-tiedotuspalvelu-local")))
        .withRequestBody(matchingJsonPath("$.todistusKey", WireMock.equalTo("todistukset/tiedote.pdf"))))
    }
  }

  "TiedotuspalveluRetryBudget" - {
    // Http.runRequest kääräisee .timeout(httpTimeout):n koko retry-loopin ympärille, joten uloimman
    // timeoutin on oltava pisin – muuten uudelleenyritykset katkeavat hiljaisesti kesken.
    "uloin timeout on pidempi kuin yritysten yhteiskesto" in {
      import TiedotuspalveluRetryBudget._
      val yritystenYhteiskesto = retryTimeout * (maxRetries + 1) + maxWaitBetweenRetries * maxRetries
      httpTimeout.toMillis should be > yritystenYhteiskesto.toMillis
    }
  }
}
