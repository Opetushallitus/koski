package fi.oph.koski.ytr

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.util.ClasspathResource
import org.scalatest.freespec.AnyFreeSpec

import scala.collection.Iterator.continually

class YtrKoesuoritusSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethods {
  "Kansalainen" - {
    "näkee koesuorituksensa (PDF)" in {
      get("koesuoritus/2345K_XX_12345.pdf", headers = kansalainenLoginHeaders("080698-703Y")) {
        verifyResponseStatusOk()
        response.getHeader("Content-Type") should equal("application/pdf;charset=utf-8")
        bodyBytes should equal(resourceAsByteArray(s"/mockdata/ytr/2345K_XX_12345.pdf"))
      }
    }

    "näkee koesuorituksensa (HTML)" in {
      get("koesuoritus/1234S_YY_420.html", headers = kansalainenLoginHeaders("080698-703Y")) {
        verifyResponseStatusOk()
        response.getHeader("Content-Type") should equal("text/html;charset=utf-8")
        bodyBytes should equal(resourceAsByteArray(s"/mockdata/ytr/1234S_YY_420.html"))
      }
    }

    "näkee huolletavansa koesuorituksen" in {
      get(s"koesuoritus/2345K_XX_12345.pdf?huollettava=${KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid}", headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.faija.hetu.get)) {
        verifyResponseStatusOk()
        bodyBytes should equal(resourceAsByteArray(s"/mockdata/ytr/2345K_XX_12345.pdf"))
      }
    }

    "ei näe toisten koesuoritusta" in {
      get("koesuoritus/2345K_XX_12345.pdf", headers = kansalainenLoginHeaders("210244-374K")) {
        verifyResponseStatus(404, Nil)
      }
    }

    "ei näe toisen huollettavan koesuoritusta" in {
      get(s"koesuoritus/2345K_XX_12345.pdf?huollettava=${KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid}", headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.amis.hetu.get)) {
        verifyResponseStatus(404, Nil)
      }
    }

    "ei näe koesuoritusta jota ei ole olemassa" in {
      get("koesuoritus/not-found-from-s3.pdf", headers = kansalainenLoginHeaders("080698-703Y")) {
        verifyResponseStatus(404, Nil)
      }
    }

    "oman koesuorituksen haku aiheuttaa auditlogin" in {
      AuditLogTester.clearMessages
      get("koesuoritus/2345K_XX_12345.pdf", headers = kansalainenLoginHeaders("080698-703Y")) {
        verifyResponseStatusOk()
        bodyBytes should equal(resourceAsByteArray(s"/mockdata/ytr/2345K_XX_12345.pdf"))
      }
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_YLIOPPILASKOE_HAKU"))
    }

    "huollettavan koesuorituksen haku aiheuttaa auditlogin" in {
      AuditLogTester.clearMessages
      get(s"koesuoritus/2345K_XX_12345.pdf?huollettava=${KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid}", headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.faija.hetu.get)) {
        verifyResponseStatusOk()
        bodyBytes should equal(resourceAsByteArray(s"/mockdata/ytr/2345K_XX_12345.pdf"))
      }
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_HUOLTAJA_YLIOPPILASKOE_HAKU"))
    }
  }

  "Viranomainen" - {
    "ei näe koesuoritusta" in {
      authGet("koesuoritus/2345K_XX_12345.pdf", defaultUser) {
        verifyResponseStatus(403, Nil)
      }
    }
  }

  private def resourceAsByteArray(resourceName: String): Array[Byte] =
    ClasspathResource.resourceSerializer(resourceName)(inputStream => continually(inputStream.read).takeWhile(_ != -1).map(_.toByte).toArray).get
}
