package fi.oph.koski.ytr

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class YtrKoesuoritusApiServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with RequiresKansalainen {
  post("/") {
    examResponse
  }

  private def examResponse: List[ExamResponse] = {
    val (ytrHenkilö, examResponse) = getExams
    if (shouldHaveExams(ytrHenkilö) && examResponse.forall(_.copyOfExamPaper.isEmpty)) {
      logger.warn(s"Oppija ${koskiSession.oid} with graduadion date ${ytrHenkilö.flatMap(_.graduationDate).mkString} has an empty exam list")
    }
    examResponse
  }

  private def getExams = {
    val ytrHenkilö: List[YtrOppija] = getHenkilö
    (ytrHenkilö, ytrHenkilö.flatMap(_.exams).map(toExamResponse))
  }

  private def getHenkilö = {
    val henkilö: Option[HenkilönTunnisteet] = if (isHuollettava) {
      application.huoltajaService.getSelectedHuollettava(koskiSession.oid)
    } else {
      application.henkilöRepository.findByOid(koskiSession.oid)
    }
    henkilö.flatMap(application.ytrRepository.findByTunnisteet).toList
  }

  private def shouldHaveExams(ytrHenkilö: List[YtrOppija]): Boolean =
    ytrHenkilö.exists(_.graduationDate.exists(_.isAfter(shouldHaveExamsDate)))

  private lazy val shouldHaveExamsDate = LocalDate.of(2019, 9, 30)

  private def toExamResponse(exam: YtrExam) = ExamResponse(period = exam.period, examId = exam.examId, copyOfExamPaper = exam.copyOfExamPaper)

  private def isHuollettava: Boolean = withJsonBody(JsonSerializer.extract[Map[String, Boolean]](_))().getOrElse("huollettava", false)
}

case class ExamResponse(period: String, examId: String, copyOfExamPaper: Option[String] = None)
