package fi.oph.koski.ytr

import java.time.LocalDate
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.typesafe.config.ConfigFactory
import fi.oph.koski.TestEnvironment
import org.json4s.DefaultFormats
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.json4s.jackson.Serialization.write

class YtrClientSpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterAll {
  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues

  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |ytr.url = "http://localhost:9877"
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
        "points" -> "100",
        "copyOfExamPaper" -> "1.pdf"
      ),
      Map(
        "period" -> "2015S",
        "examId" -> "BB",
        "grade" -> "A",
        "copyOfExamPaper" -> null
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
        points = Some(100),
        copyOfExamPaper = Some("1.pdf")
      ),
      YtrExam(
        period = "2015S",
        examId = "BB",
        grade = "A",
        points = None,
        copyOfExamPaper = None
    )),
    certificateSchoolOphOid = Some("1.2.3.4.5"),
    hasCompletedMandatoryExams = true
  )

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    mockEndpoints()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  "YtrClient" - {
    "palauttaa ytr oppijan" in {
      val result = mockClient.oppijaByHetu(YtrSsnWithPreviousSsns(hetu))
      result.get should equal(expectedOppija)
    }
    "ei välitä ylimääräisistä kentistä" in {
      mockEndpoints(defaultOppijaResponse + ("Foo" -> "Bar"))
      val result = mockClient.oppijaByHetu(YtrSsnWithPreviousSsns(hetu))
      result.get should equal(expectedOppija)
    }
  }

  def mockEndpoints(oppijaResponse: Map[String, Any]  = defaultOppijaResponse) =  {
    val ytrJsonUrl = s"/api/oph-koski/student"

    wireMockServer.stubFor(
      WireMock.post(urlPathEqualTo(ytrJsonUrl))
        .willReturn(ok().withBody(write(oppijaResponse)))
    )
  }
}
