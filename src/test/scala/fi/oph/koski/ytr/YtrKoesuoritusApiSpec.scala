package fi.oph.koski.ytr

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.jackson.JsonMethods
import org.scalatest.FreeSpec

class YtrKoesuoritusApiSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  "Kansalainen" - {
    "voi hakea koesuorituslistauksen" in {
      post("api/ytrkoesuoritukset/" + MockOppijat.ylioppilasLukiolainen.oid, headers = kansalainenLoginHeaders(MockOppijat.ylioppilasLukiolainen.hetu.get) ++ jsonContent) {
        verifyResponseStatusOk()
        readExams should equal (expected)
      }
    }

    "ei voi hakea toisen henkilön koesuorituslistausta, jos tämä ei ole huolettava" in {
      post("api/ytrkoesuoritukset/" + MockOppijat.ylioppilasLukiolainen.oid, headers = kansalainenLoginHeaders(MockOppijat.aikuisOpiskelija.hetu.get) ++ jsonContent) {
        verifyResponseStatus(403, Nil)
      }
    }

    "voi hakea huollettavan koesuorituslistauksen" in {
      post("api/ytrkoesuoritukset/" + MockOppijat.ylioppilasLukiolainen.oid, headers = kansalainenLoginHeaders(MockOppijat.faija.hetu.get) ++ jsonContent) {
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

  lazy val huoltaja = JsonSerializer.writeWithRoot(Map("huollettava" -> false))
  lazy val huollettava = JsonSerializer.writeWithRoot(Map("huollettava" -> true))

  import fi.oph.koski.schema.KoskiSchema.deserializationContext
  private def readExams: List[ExamResponse] =
    SchemaValidatingExtractor.extract[List[ExamResponse]](JsonMethods.parse(body)).right.get

  private val expected = List(
    ExamResponse(period = "2012K", examId = "A", copyOfExamPaper = Some("2345K_XX_12345.pdf")),
    ExamResponse(period = "2012K", examId = "BB", copyOfExamPaper = Some("not-found-from-s3.pdf")),
    ExamResponse(period = "2012K", examId = "EA", copyOfExamPaper = Some("1.pdf")),
    ExamResponse(period = "2012K", examId = "GE", copyOfExamPaper = Some("2.pdf")),
    ExamResponse(period = "2012K", examId = "N", copyOfExamPaper = Some("1234S_YY_420.html"))
  )
}
