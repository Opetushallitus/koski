package fi.oph.koski.ytr

import java.time.LocalDate

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.typesafe.config.ConfigFactory
import org.json4s.DefaultFormats
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import org.json4s.jackson.Serialization.write

class YtrClientSpec extends FreeSpec with Matchers with BeforeAndAfterAll {
  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues

  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |ytr.url = "http://localhost:9877"
      |ytr.insecure = false
      |ytr.username  = "foo"
      |ytr.password = "bar"
    """.stripMargin)

  private val mockClient = YtrClient(config)

  private val wireMockServer = new WireMockServer(wireMockConfig().port(9877))

  private val hetu = "080640-881R"

  private val defaultOppijaResponse = Map(
    "lastname" -> "Pouta",
    "firstnames" -> "Pekka Pilvi",
    "graduationDate" -> "2016-01-01",
    "exams" -> Seq(
      Map(
        "period" -> "2016K",
        "examId" -> "N",
        "grade" -> "L",
        "points" -> "100"
      ),
      Map(
        "period" -> "2015S",
        "examId" -> "BB",
        "grade" -> "A"
      )
    ),
    "certificateSchoolOphOid" -> "1.2.3.4.5",
    "hasCompletedMandatoryExams" -> true
  )

  private val expectedOppija = YtrOppija(
    lastname = "Pouta",
    firstnames = "Pekka Pilvi",
    graduationDate = Some(LocalDate.of(2016, 1, 1)),
    exams = List(
      YtrExam(
        period = "2016K",
        examId = "N",
        grade = "L",
        points = Some(100)
      ),
      YtrExam(
        period = "2015S",
        examId = "BB",
        grade = "A",
        points = None
    )),
    certificateSchoolOphOid = Some("1.2.3.4.5"),
    hasCompletedMandatoryExams = true
  )

  override def beforeAll: Unit = {
    wireMockServer.start()
    mockEndpoints()
  }

  override def afterAll {
    wireMockServer.stop()
  }

  "YtrClient" - {

    "palauttaa ytr oppijan" in {
      val result = mockClient.oppijaByHetu(hetu)
      result.get should equal(expectedOppija)
    }
    "ignoraa ylimääräiset kentät" in {
      mockEndpoints(defaultOppijaResponse + ("Foo" -> "Bar"))
      val result = mockClient.oppijaByHetu(hetu)
      result.get should equal(expectedOppija)
    }
  }

  def mockEndpoints(oppijaResponse: Map[String, Any]  = defaultOppijaResponse) =  {
    val ytrJsonUrl = s"/api/oph-transfer/student/${hetu}"

    wireMockServer.stubFor(
      WireMock.get(urlPathEqualTo(ytrJsonUrl))
        .willReturn(ok().withBody(write(oppijaResponse)))
    )
  }
}
