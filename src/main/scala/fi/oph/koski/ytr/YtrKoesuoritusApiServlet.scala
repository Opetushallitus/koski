package fi.oph.koski.ytr

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class YtrKoesuoritusApiServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with RequiresKansalainen {
  post("/") {
    exams
  }

  private def exams: List[ExamResponse] = {
    val henkilö: Option[LaajatOppijaHenkilöTiedot] = application.henkilöRepository.findByOid(koskiSession.oid)
    val ytrHenkilö: List[YtrOppija] = henkilö.flatMap(application.ytrRepository.findByTunnisteet).toList
    val examResponse: List[ExamResponse] = ytrHenkilö.flatMap(_.exams).map(toExamResponse)
    if (shouldHaveExams(ytrHenkilö) && examResponse.forall(_.copyOfExamPaper.isEmpty)) {
      logger.warn(s"Oppija ${koskiSession.oid} with graduadion date ${ytrHenkilö.flatMap(_.graduationDate).mkString} has an empty exam list")
    }
    examResponse
  }

  private def shouldHaveExams(ytrHenkilö: List[YtrOppija]): Boolean =
    ytrHenkilö.exists(_.graduationDate.exists(_.isAfter(shouldHaveExamsDate)))

  private lazy val shouldHaveExamsDate = LocalDate.of(2019, 9, 30)

  private def toExamResponse(exam: YtrExam) = ExamResponse(period = exam.period, examId = exam.examId, copyOfExamPaper = exam.copyOfExamPaper)
}

case class ExamResponse(period: String, examId: String, copyOfExamPaper: Option[String] = None)
