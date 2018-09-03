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
      (mockRequest.getRequestURI _).expects().returning("/koski/omadata/valtuutus/hsl")

      support(mockRequest).getShibbolethLoginURL(lang = lang) should
        equal("/koski/login/shibboleth?login=%2Fkoski%2Fuser%2Fshibbolethlogin%3FonSuccess%3D%2Fkoski%2Fomadata%2Fvaltuutus%2Fhsl%3Fcallback%3Dhttp%3A%2F%2Fwww.hsl.fi&redirect=%2Fkoski%2Fomadata%2Fvaltuutus%2Fhsl%3Fcallback%3Dhttp%3A%2F%2Fwww.hsl.fi")
    }
    "Palauttaa oikean URL:n sisään loganneille" in {
      val mockRequest = mock[HttpServletRequest]
      (mockRequest.getQueryString _).expects().returning("callback=http://www.hsl.fi/alennus").repeat(2)
      (mockRequest.getRequestURI _).expects().returning("/koski/omadata/valtuutus/hsl")

      support(mockRequest).getLoginURL() should
        equal("/koski/user/shibbolethlogin?onSuccess=/koski/omadata/valtuutus/hsl?callback=http://www.hsl.fi/alennus")
    }
    "Palauttaa oikean member ID:n" in {
      val request: HttpServletRequest = stub[HttpServletRequest]
      (request.getAttribute _).when("MultiParamsRead").returns("")
      (request.getParameterMap _).when().returns(mapAsJavaMap(Map("memberCode" -> Array("hsl"))))

      support(request).memberCodeParam should equal("hsl")
    }

    "Heittää exceptionin väärällä member ID:llä" in {
      assertThrows[InvalidRequestException] {
        support.getConfigForMember("000000-00")
      }
    }
  }
}
