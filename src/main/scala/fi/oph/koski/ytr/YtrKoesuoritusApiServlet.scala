package fi.oph.koski.ytr

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class YtrKoesuoritusApiServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with RequiresKansalainen {
  post("/:oid") {
    renderEither(examResponse(params("oid")))
  }

  private def examResponse(oid: String) = {
    if (session.user.oid == oid || session.isUsersHuollettava(oid)) {
      val (ytrHenkilö, examResponse) = getExams(oid)
      if (shouldHaveExams(ytrHenkilö) && examResponse.forall(_.copyOfExamPaper.isEmpty)) {
        logger.warn(s"Oppija ${session.oid} with graduadion date ${ytrHenkilö.flatMap(_.graduationDate).mkString} has an empty exam list")
      }
      Right(examResponse)
    }
    else {
      Left(KoskiErrorCategory.forbidden())
    }
  }

  private def getExams(oid: String) = {
    val ytrHenkilö: List[YtrOppija] = getHenkilö(oid)
    (ytrHenkilö, ytrHenkilö.flatMap(_.exams).map(toExamResponse))
  }

  private def getHenkilö(oid: String) = {
    val henkilö: Option[HenkilönTunnisteet] = application.henkilöRepository.findByOid(oid)
    henkilö.flatMap(application.ytrRepository.findByTunnisteet).toList
  }

  private def shouldHaveExams(ytrHenkilö: List[YtrOppija]): Boolean =
    ytrHenkilö.exists(_.graduationDate.exists(_.isAfter(shouldHaveExamsDate)))

  private lazy val shouldHaveExamsDate = LocalDate.of(2019, 9, 30)

  private def toExamResponse(exam: YtrExam) = ExamResponse(period = exam.period, examId = exam.examId, copyOfExamPaper = exam.copyOfExamPaper)
}

case class ExamResponse(period: String, examId: String, copyOfExamPaper: Option[String] = None)
