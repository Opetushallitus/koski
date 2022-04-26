package fi.oph.koski.frontendvalvonta

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.log.{ReportToLogTester, ReportUriLogTester}
import org.json4s.{JArray, JBool, JObject, JValue}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class FrontendValvontaServletSpec extends AnyFreeSpec with KoskiHttpSpec with HttpSpecification with Matchers {

  "FrontendValvontaServlet" - {
    "ReportUri route" - {
      "APIin postaaminen tuottaa logimerkinnän samalla sisällöllä" in {
        ReportUriLogTester.clearMessages

        post(
          "api/frontendvalvonta/report-uri",
          reportUriSample(),
          headers = jsonContent
        ){
          verifyResponseStatusOk()
          val logMessages = ReportUriLogTester.getLogMessages
          logMessages.length should be(1)

          val expected = JsonMethods.parse(reportUriSample())
            .merge(JObject("cspreporturi" -> JBool(true)))
          JsonMethods.parse(logMessages(0)) should equal(expected)
        }
      }

      "API ei hyväksy requestiä väärällä content-typellä" in {
        ReportUriLogTester.clearMessages

        post(
          "api/frontendvalvonta/report-uri",
          reportUriSample(),
          headers = Map(("Content-type" -> "application/text"))
        ){
          verifyResponseStatus(400, Nil)

          val logMessages = ReportUriLogTester.getLogMessages
          logMessages.length should be(0)
        }
      }

      "API ei hyväksy yli 32000 merkkiä pitkää inputtia DOS-hyökkäysten estämiseksi" in {
        ReportUriLogTester.clearMessages

        post(
          "api/frontendvalvonta/report-uri",
          reportUriSample("F" * 32000),
          headers = jsonContent
        ){
          verifyResponseStatus(403, Nil)

          val logMessages = ReportUriLogTester.getLogMessages
          logMessages.length should be(0)
        }
      }
    }

    "ReportTo" - {
      "APIin yhden CSP-entryn postaaminen tuottaa logimerkinnän samalla sisällöllä" in {
        ReportToLogTester.clearMessages

        post(
          "api/frontendvalvonta/report-to",
          reportToSample(),
          headers = jsonContent
        ){
          verifyResponseStatusOk()
          val logMessages = ReportToLogTester.getLogMessages
          logMessages.length should be(1)

          val expected: JValue = (for {
            JArray(objList) <- JsonMethods.parse(reportToSample())
            objValue <- objList
          } yield objValue.merge(JObject("frontendreportto" -> JBool(true))))
            .head

          JsonMethods.parse(logMessages(0)) should equal(expected)
        }
      }

      "API ei hyväksy requestiä väärällä content-typellä" in {
        ReportToLogTester.clearMessages

        post(
          "api/frontendvalvonta/report-to",
          reportToSample(),
          headers = Map(("Content-type" -> "application/text"))
        ){
          verifyResponseStatus(400, Nil)

          val logMessages = ReportToLogTester.getLogMessages
          logMessages.length should be(0)
        }
      }

      "APIin usean entry postaaminen tuottaa logimerkinnät entryistä samalla sisällöllä" in {
        ReportToLogTester.clearMessages

        post(
          "api/frontendvalvonta/report-to",
          reportToSampleMany,
          headers = jsonContent
        ){
          verifyResponseStatusOk()
          val logMessages = ReportToLogTester.getLogMessages
          logMessages.length should be(2)
        }
      }

      "API ei hyväksy yli 32000 merkkiä pitkää inputtia DOS-hyökkäysten estämiseksi" in {
        ReportToLogTester.clearMessages

        post(
          "api/frontendvalvonta/report-to",
          reportToSample("F" * 32000),
          headers = jsonContent
        ){
          verifyResponseStatus(403, Nil)

          val logMessages = ReportToLogTester.getLogMessages
          logMessages.length should be(0)
        }
      }
    }
  }

  private def reportUriSample(scriptSample: String =""): String =
    s"""
       |{
       |  "csp-report": {
       |    "document-uri": "https://virkailija.opintopolku.fi/koski/virkailija/foo",
       |    "referrer": "",
       |    "violated-directive": "frame-src",
       |    "effective-directive": "frame-src",
       |    "original-policy": "default-src 'self'; frame-src https://*.foo.org; report-uri /report",
       |    "disposition": "enforce",
       |    "blocked-uri": "https://bar.org",
       |    "line-number": 1,
       |    "source-file": "https://virkailija.opintopolku.fi/koski/virkailija/foo",
       |    "status-code": 200,
       |    "script-sample": "${scriptSample}"
       |  }
       |}
       |""".stripMargin


  private def reportToSample(scriptSample: String =""): String =
    s"""
       |[{
       |  "age": 12345,
       |  "body": {
       |    "blocked-uri": "https://bar.org",
       |    "disposition": "enforce",
       |    "document-uri": "https://virkailija.opintopolku.fi/koski/virkailija/foo",
       |    "effective-directive": "frame-src",
       |    "line-number": 1,
       |    "original-policy": "default-src 'self'; frame-src https://*.foo.org; report-uri /report; report-to csp-endpoint;",
       |    "referrer": "",
       |    "script-sample": "${scriptSample}",
       |    "sourceFile": "https://virkailija.opintopolku.fi/koski/virkailija/foo",
       |    "violated-directive": "frame-src"
       |  },
       |  "type": "csp",
       |  "url": "https://foo.bar:1234/report-to",
       |  "user_agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36"
       |}]
       |""".stripMargin

  private def reportToSampleMany: String =
    s"""
       |[{
       |  "age": 12345,
       |  "body": {
       |    "blocked-uri": "https://bar.org",
       |    "disposition": "enforce",
       |    "document-uri": "https://virkailija.opintopolku.fi/koski/virkailija/foo",
       |    "effective-directive": "frame-src",
       |    "line-number": 1,
       |    "original-policy": "default-src 'self'; frame-src https://*.foo.org; report-uri /report; report-to csp-endpoint;",
       |    "referrer": "",
       |    "script-sample": "",
       |    "sourceFile": "https://virkailija.opintopolku.fi/koski/virkailija/foo",
       |    "violated-directive": "frame-src"
       |  },
       |  "type": "csp",
       |  "url": "https://foo.bar:1234/report-to",
       |  "user_agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36"
       | },
       | {
       |   "age": 656,
       |   "body": {
       |     "blocked-uri": "https://foobar.org",
       |     "disposition": "enforce",
       |     "document-uri": "https://virkailija.opintopolku.fi/koski/virkailija/foobar",
       |     "effective-directive": "frame-src",
       |     "line-number": 1,
       |     "original-policy": "default-src 'self'; frame-src https://*.foo.org; report-uri /report; report-to csp-endpoint;",
       |     "referrer": "",
       |     "script-sample": "",
       |     "sourceFile": "https://virkailija.opintopolku.fi/koski/virkailija/foobar",
       |     "violated-directive": "frame-src"
       |   },
       |   "type": "csp",
       |   "url": "https://foo.bar:1234/report-to",
       |   "user_agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36"
       | }]
       |""".stripMargin
}
