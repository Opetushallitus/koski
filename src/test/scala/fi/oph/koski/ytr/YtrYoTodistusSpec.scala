package fi.oph.koski.ytr

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.util.ClasspathResource
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.{JNull, JObject, JString}
import org.scalatest.freespec.AnyFreeSpec

import java.time.{ZoneId, ZoneOffset, ZonedDateTime}
import scala.collection.Iterator.continually

class YtrYoTodistusSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethods {
  "YTR-rajapinnan vastaukset" - {
    implicit val deserializationContext: ExtractionContext =
      ExtractionContext(KoskiSchema.schemaFactory).copy(validate = false)

    "NOT_STARTED" in {
      val json = JObject(List(("status", JString("NOT_STARTED"))))
      val response = SchemaValidatingExtractor.extract[YtrCertificateResponse](json)
      response should equal(Right(YtrCertificateNotStarted()))
    }

    "IN_PROGRESS" in {
      val json = JObject(List(
        ("status", JString("IN_PROGRESS")),
        ("certificateUrl", JNull),
        ("requestedTime", JString("2023-03-24T08:30:26.632Z")),
      ))

      val response = SchemaValidatingExtractor.extract[YtrCertificateResponse](json)

      response should equal(Right(YtrCertificateInProgress(
        requestedTime = ZonedDateTime.of(2023, 3, 24, 8, 30, 26, 632000000, ZoneOffset.UTC),
      )))
    }

    "COMPLETED" in {

      val json = JObject(List(
        ("status", JString("COMPLETED")),
        ("certificateUrl", JString("s3://yo-test.integration.certificates/5645a3f1-ebbc-4034-8426-d63cc4c5a901-fi-signed.pdf")),
        ("requestedTime", JString("2023-03-23T13:44:04.398Z")),
        ("completionTime", JString("2023-03-23T13:44:07.644Z"))
      ))

      val response = SchemaValidatingExtractor.extract[YtrCertificateResponse](json)

      response should equal(Right(YtrCertificateCompleted(
        requestedTime = ZonedDateTime.of(2023, 3, 23, 13, 44, 4, 398000000, ZoneOffset.UTC),
        completionTime = ZonedDateTime.of(2023, 3, 23, 13, 44, 7, 644000000, ZoneOffset.UTC),
        certificateUrl = "s3://yo-test.integration.certificates/5645a3f1-ebbc-4034-8426-d63cc4c5a901-fi-signed.pdf",
      )))
    }
  }

  "Kansalainen" - {
    "näkee oman todistuksensa" in {
      yoTodistusHappyPath("080698-967F", "1.2.246.562.24.00000000049")
    }

    "näkee huollettavansa todistuksen" in {
      yoTodistusHappyPath("030300-5215", "1.2.246.562.24.00000000049")
    }

    "ei näe toisen oppijan todistus" - {
      "status-api" in {
        get("api/yotodistus/status/fi/1.2.246.562.24.00000000050", headers = kansalainenLoginHeaders("080698-967F")) {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized())
        }
      }

      "generate-api" in {
        get("api/yotodistus/generate/fi/1.2.246.562.24.00000000050", headers = kansalainenLoginHeaders("080698-967F")) {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized())
        }
      }

      "download-api" in {
        get("api/yotodistus/download/fi/1.2.246.562.24.00000000050/pampam.pdf", headers = kansalainenLoginHeaders("080698-967F")) {
          verifyResponseStatus(503, KoskiErrorCategory.unavailable.yoTodistus.notCompleteOrNoAccess())
        }
      }
    }
  }

  private def yoTodistusHappyPath(katsojanHetu: String, oppijaOid: String) = {
    AuditLogTester.clearMessages()
    val headers = kansalainenLoginHeaders(katsojanHetu)
    get(s"api/yotodistus/status/fi/$oppijaOid", headers = headers) {
      verifyResponseStatusOk()
      get(s"api/yotodistus/generate/fi/$oppijaOid", headers = headers) {
        verifyResponseStatus(204)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "YTR_YOTODISTUKSEN_LUONTI"))
        Thread.sleep(3000)
        get(s"api/yotodistus/download/fi/$oppijaOid/foobar.pdf", headers = headers) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "YTR_YOTODISTUKSEN_LATAAMINEN"))
          response.getHeader("Content-Type") should equal("application/pdf;charset=utf-8")
          bodyBytes should equal(resourceAsByteArray(s"/mockdata/yotodistus/mock-yotodistus.pdf"))
        }
      }
    }
  }

  private def resourceAsByteArray(resourceName: String): Array[Byte] =
    ClasspathResource.resourceSerializer(resourceName)(inputStream => continually(inputStream.read).takeWhile(_ != -1).map(_.toByte).toArray).get
}
