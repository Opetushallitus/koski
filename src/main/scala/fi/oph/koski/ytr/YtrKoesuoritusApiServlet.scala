package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class YtrKoesuoritusApiServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with RequiresKansalainen {
  get("/") {
    exams
  }

  private def exams: List[ExamResponse] =
    application.henkilöRepository.findByOid(koskiSession.oid)
      .flatMap(application.ytrRepository.findByTunnisteet).toList
      .flatMap(_.exams)
      .map(toExamResponse)

  private def toExamResponse(exam: YtrExam) = ExamResponse(period = exam.period, examId = exam.examId, copyOfExamPaper = exam.copyOfExamPaper)
}

case class ExamResponse(period: String, examId: String, copyOfExamPaper: Option[String])
