package fi.oph.koski.mydata

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.servlet.MyDataSupport
import javax.servlet.http.HttpServletRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FreeSpec, Matchers}

class MyDataSupportTest extends FreeSpec with Matchers with MockFactory {

  val memberId = "hsl"
  val lang = "fi"

  "MyDataSupport" - {
    "Palauttaa oikean Korhopankki-URL:n sisään loggautumattomille" in {
      val mockRequest = mock[HttpServletRequest]
      (mockRequest.getQueryString _).expects().returning("callback=http://www.hsl.fi").repeat(2)
      (mockRequest.getRequestURI _).expects().returning("/koski/omadata/hsl")

      def support: MyDataSupport = new MyDataSupport {
        override def application = KoskiApplicationForTests
        override def getCurrentURL(implicit httpServletRequest: HttpServletRequest): String = super.getCurrentURL(mockRequest)
      }

      support.getLoginUrlForMember(memberId, lang) should
        equal("/koski/login/shibboleth?login=/koski/user/omadatalogin/hsl%3FonLoginSuccess%3D%2Fkoski%2Fomadata%2Fhsl%3Fcallback%3Dhttp%3A%2F%2Fwww.hsl.fi")

    }
    "Palauttaa oikean URL:n sisään loganneille" in {
      val mockRequest = mock[HttpServletRequest]
      (mockRequest.getQueryString _).expects().returning("callback=http://www.hsl.fi").repeat(2)
      (mockRequest.getRequestURI _).expects().returning("/koski/omadata/hsl")

      def support: MyDataSupport = new MyDataSupport {
        override def application = KoskiApplicationForTests
        override def getCurrentURL(implicit httpServletRequest: HttpServletRequest): String = super.getCurrentURL(mockRequest)
      }

      support.getLoginSuccessTarget(memberId, lang) should
        equal("/koski/user/omadatalogin/hsl?onLoginSuccess=/koski/omadata/hsl?callback=http://www.hsl.fi")
    }
  }
}
