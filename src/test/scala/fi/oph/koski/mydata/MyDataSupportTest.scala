package fi.oph.koski.mydata

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.servlet.InvalidRequestException

import javax.servlet.http.HttpServletRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._


class MyDataSupportTest extends AnyFreeSpec with TestEnvironment with Matchers with MockFactory {

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
      (() => mockRequest.getQueryString).expects().returning("callback=http://www.hsl.fi").repeat(2)
      (() => mockRequest.getRequestURI).expects().returning("/koski/omadata/valtuutus/hsl")

      support(mockRequest).getCasLoginURL(lang = lang) should
        equal(s"/koski/login/oppija?locale=${lang}&service=/koski/user/login?onSuccess=/koski/omadata/valtuutus/hsl?callback=http://www.hsl.fi&valtuudet=false&redirect=%2Fkoski%2Fomadata%2Fvaltuutus%2Fhsl%3Fcallback%3Dhttp%3A%2F%2Fwww.hsl.fi")
    }
    "Palauttaa oikean URL:n sisään loganneille" in {
      val mockRequest = mock[HttpServletRequest]
      (() => mockRequest.getQueryString).expects().returning("callback=http://www.hsl.fi/alennus").repeat(2)
      (() => mockRequest.getRequestURI).expects().returning("/koski/omadata/valtuutus/hsl")

      support(mockRequest).getLoginURL() should
        equal("/koski/user/login?onSuccess=/koski/omadata/valtuutus/hsl?callback=http://www.hsl.fi/alennus")
    }
    "Palauttaa oikean member ID:n" in {
      val request: HttpServletRequest = stub[HttpServletRequest]
      (request.getAttribute _).when("MultiParamsRead").returns("")
      (() => request.getParameterMap).when().returns(Map("memberCode" -> Array("hsl")).asJava)

      support(request).memberCodeParam should equal("hsl")
    }

    "Heittää exceptionin väärällä member ID:llä" in {
      assertThrows[InvalidRequestException] {
        support.getConfigForMember("000000-00")
      }
    }

    "Tunnistaa sallitun callback URL:n" in {
      support.isWhitelistedCallbackURL("http://localhost") should equal(true)
      support.isWhitelistedCallbackURL("http://localhost:8080/index.html") should equal(true)
      support.isWhitelistedCallbackURL("https://localhost") should equal(true)
      support.isWhitelistedCallbackURL("https://localhost/index.html?parameter=value") should equal(true)
    }

    "Tunnistaa ei-sallitun callback URL:n" in {
      support.isWhitelistedCallbackURL("mycustomapp://input") should equal(false)
      support.isWhitelistedCallbackURL("https://www.google.com") should equal(false)
    }

  }
}
