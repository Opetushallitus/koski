package fi.oph.koski.mydata

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.LoggerWithContext
import fi.oph.koski.servlet.{InvalidRequestException, MyDataSupport}
import javax.servlet.http.HttpServletRequest
import org.log4s.Logger
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.JavaConverters._


class MyDataSupportTest extends FreeSpec with Matchers with MockFactory {

  val memberId = "hsl"
  val lang = "fi"

  def support: MyDataSupport = support(mock[HttpServletRequest])

  def support(mockRequest: HttpServletRequest): MyDataSupport = {
    new MyDataSupport {
      override implicit val request: HttpServletRequest = mockRequest
      override def application = KoskiApplicationForTests
    }
  }

  "MyDataSupport" - {
    "Palauttaa oikean Korhopankki-URL:n sisään loggautumattomille" in {
      val mockRequest = mock[HttpServletRequest]
      (mockRequest.getQueryString _).expects().returning("callback=http://www.hsl.fi").repeat(2)
      (mockRequest.getRequestURI _).expects().returning("/koski/omadata/hsl")

      support(mockRequest).getLoginUrlForMember(lang, memberId) should
        equal("/koski/login/shibboleth?login=/koski/user/omadatalogin%3FonLoginSuccess%3D%2Fkoski%2Fomadata%2Fhsl%3Fcallback%3Dhttp%3A%2F%2Fwww.hsl.fi")

    }
    "Palauttaa oikean URL:n sisään loganneille" in {
      val mockRequest = mock[HttpServletRequest]
      (mockRequest.getQueryString _).expects().returning("callback=http://www.hsl.fi/alennus").repeat(2)
      (mockRequest.getRequestURI _).expects().returning("/koski/omadata/hsl")

      support(mockRequest).getLoginSuccessTarget(memberId) should
        equal("/koski/user/omadatalogin?onLoginSuccess=/koski/omadata/hsl?callback=http://www.hsl.fi/alennus")
    }
    "Palauttaa oikean member ID:n" in {
      val request: HttpServletRequest = stub[HttpServletRequest]
      (request.getAttribute _).when("MultiParamsRead").returns("")
      (request.getParameterMap _).when().returns(mapAsJavaMap(Map("memberCode" -> Array("hsl"))))

      support(request).memberCode should equal("hsl")
    }

    "Heittää exceptionin väärällä member ID:llä" in {
      assertThrows[InvalidRequestException] {
        support.getConfigForMember("000000-00")
      }
    }
  }
}
