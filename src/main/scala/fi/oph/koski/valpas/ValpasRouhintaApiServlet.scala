package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.UserLanguage.sanitizeLanguage
import fi.oph.koski.raportit.{ExcelWriter, OppilaitosRaporttiResponse}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.log.ValpasAuditLog.auditLogRouhintahakuHetulistalla
import fi.oph.koski.valpas.rouhinta.{HeturouhinnanTulos, ValpasRouhintaService}
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession
import org.json4s.JValue
import org.scalatra.{Cookie, CookieOptions}
import fi.oph.koski.util.ChainingSyntax._

class ValpasRouhintaApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private val rouhinta = new ValpasRouhintaService(application)

  post("/hetut") {
    withJsonBody { (body: JValue) =>
      val result = extractHetuList(body)
        .flatMap(input => {
          if (jsonRequested) {
            rouhinta.haeHetulistanPerusteella(input.hetut)
              .tap(_ => auditLogRouhintahakuHetulistalla(input.hetut))
          } else {
            val language = input.lang.orElse(langFromCookie).getOrElse("fi")
            rouhinta.haeHetulistanPerusteellaExcel(input.hetut, language, input.password)
              .tap(_ => auditLogRouhintahakuHetulistalla(input.hetut))
          }
        })
      renderResult(result)
    } (parseErrorHandler = haltWithStatus)
  }

  private def jsonRequested = request.header("accept").contains("application/json")

  private def extractHetuList(input: JValue) =
    application.validatingAndResolvingExtractor.extract[HetuhakuInput](strictDeserialization)(input)

  private def renderResult(result: Either[HttpStatus, Any]): Unit = {
    result match {
      case Right(r) => r match {
        case r: OppilaitosRaporttiResponse => writeExcel(r)
        case r: HeturouhinnanTulos => renderObject(r)
        case _ => haltWithStatus(ValpasErrorCategory.internalError())
      }
      case Left(e) => haltWithStatus(e)
    }
  }

  private def writeExcel(raportti: OppilaitosRaporttiResponse): Unit = {
    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    response.setHeader("Content-Disposition", s"""attachment; filename="${raportti.filename}"""")
    raportti.downloadToken.foreach { t => response.addCookie(Cookie("valpasDownloadToken", t)(CookieOptions(path = "/", maxAge = 600))) }
    ExcelWriter.writeExcel(
      raportti.workbookSettings,
      raportti.sheets,
      response.getOutputStream
    )
  }

  def langFromCookie: Option[String] = sanitizeLanguage(request.cookies.get("lang"))
}

case class HetuhakuInput(
  hetut: List[String],
  lang: Option[String],
  password: Option[String],
)
