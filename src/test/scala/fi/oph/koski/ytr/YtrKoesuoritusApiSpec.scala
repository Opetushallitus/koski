package fi.oph.koski.ytr

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec

class YtrKoesuoritusApiSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethods {
  private implicit val context: ExtractionContext = strictDeserialization

  private def readExams: List[ExamResponse] =
    SchemaValidatingExtractor.extract[List[ExamResponse]](JsonMethods.parse(body)).right.get

  private val expected = List(
    ExamResponse(period = "2012K", examId = "A", copyOfExamPaper = Some("2345K_XX_12345.pdf")),
    ExamResponse(period = "2012K", examId = "BB", copyOfExamPaper = Some("not-found-from-s3.pdf")),
    ExamResponse(period = "2012K", examId = "EA", copyOfExamPaper = Some("1.pdf")),
    ExamResponse(period = "2012K", examId = "GE", copyOfExamPaper = Some("2.pdf")),
    ExamResponse(period = "2012K", examId = "N", copyOfExamPaper = Some("1234S_YY_420.html"))
  )

  "Kansalainen" - {
    "voi hakea koesuorituslistauksen" in {
      post("api/ytrkoesuoritukset/" + KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid, headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.ylioppilasLukiolainen.hetu.get) ++ jsonContent) {
        verifyResponseStatusOk()
        readExams should equal (expected)
      }
    }

    "Koesuoritusta haettaessa lähetetään YTR:iin myös oppijan vanhat hetut" in {
      KoskiApplicationForTests.cacheManager.invalidateAllCaches
      MockYtrClient.latestOppijaJsonByHetu = None

      val oppija = KoskiSpecificMockOppijat.aikuisOpiskelija
      oppija.vanhatHetut.length should be >(0)

      post("api/ytrkoesuoritukset/" + oppija.oid, headers = kansalainenLoginHeaders(oppija.hetu.get) ++ jsonContent) {
        MockYtrClient.latestOppijaJsonByHetu should be(
          Some(YtrSsnWithPreviousSsns(oppija.hetu.get, oppija.vanhatHetut))
        )
      }
    }

    "ei voi hakea toisen henkilön koesuorituslistausta, jos tämä ei ole huolettava" in {
      post("api/ytrkoesuoritukset/" + KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid, headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.aikuisOpiskelija.hetu.get) ++ jsonContent) {
        verifyResponseStatus(403, Nil)
      }
    }

    "voi hakea huollettavan koesuorituslistauksen" in {
      post("api/ytrkoesuoritukset/" + KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid, headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.faija.hetu.get) ++ jsonContent) {
        verifyResponseStatusOk()
        readExams should equal (expected)
      }
    }
  }

  "Viranomainen" - {
    "ei voi hakea koesuorituslistausta" in {
      post("api/ytrkoesuoritukset/", headers = authHeaders() ++ jsonContent) {
        verifyResponseStatus(403, Nil)
      }
    }
  }
}
