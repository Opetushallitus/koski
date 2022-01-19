package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.Kunta
import fi.oph.koski.koskiuser.UserLanguage.sanitizeLanguage
import fi.oph.koski.raportit.{AhvenanmaanKunnat, ExcelWriter, OppilaitosRaporttiResponse}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.log.ValpasAuditLog.{auditLogRouhintahakuHetulistalla, auditLogRouhintahakuKunnalla}
import fi.oph.koski.valpas.rouhinta.{HeturouhinnanTulos, KuntarouhinnanTulos, ValpasRouhintaService}
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession
import org.json4s.JValue
import org.scalatra.{Cookie, CookieOptions}
import fi.oph.koski.util.ChainingSyntax._

class ValpasRouhintaApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasSession {
  private val rouhinta = new ValpasRouhintaService(application)
  private val koodistoPalvelu = application.koodistoPalvelu
  private val organisaatiot = application.organisaatioService

  post("/hetut") {
    withJsonBody { (body: JValue) =>
      val result = extractHetuList(body)
        .flatMap(input => {
          if (jsonRequested) {
            rouhinta.haeHetulistanPerusteella(input.hetut)
              .tap(tulos => auditLogRouhintahakuHetulistalla(input.hetut, tulos.palautetutOppijaOidit))
          } else {
            val language = input.lang.orElse(langFromCookie).getOrElse("fi")
            rouhinta.haeHetulistanPerusteellaExcel(input.hetut, language, input.password)
              .map(tulos => {
                auditLogRouhintahakuHetulistalla(input.hetut, tulos.data.palautetutOppijaOidit)
                tulos.response
              })
          }
        })
      renderResult(result)
    } (parseErrorHandler = haltWithStatus)
  }

  post("/kunta") {
    withJsonBody { (body: JValue) =>
      val result = extractAndValidateKuntakoodi(body)
        .flatMap(input => {
          if (jsonRequested) {
            rouhinta.haeKunnanPerusteella(input.kunta)
              .tap(tulos => auditLogRouhintahakuKunnalla(input.kunta, tulos.palautetutOppijaOidit))
          } else {
            val language = input.original.lang.orElse(langFromCookie).getOrElse("fi")
            rouhinta.haeKunnanPerusteellaExcel(input.kunta, language, input.original.password)
              .map(tulos => {
                auditLogRouhintahakuKunnalla(input.kunta, tulos.data.palautetutOppijaOidit)
                tulos.response
              })
          }
        })
      renderResult(result)
    } (parseErrorHandler = haltWithStatus)

  }

  private def jsonRequested = request.header("accept").contains("application/json")

  private def extractHetuList(input: JValue) =
    application.validatingAndResolvingExtractor.extract[HetuhakuInput](strictDeserialization)(input)

  private def extractAndValidateKuntakoodi(input: JValue) = {
    application.validatingAndResolvingExtractor.extract[KuntaInput](strictDeserialization)(input)
      .flatMap(validateKuntakoodi)
  }

  private def validateKuntakoodi(input: KuntaInput): Either[HttpStatus, ValidatedKuntaInput] =
    organisaatiot
      .haeKuntakoodi(input.kuntaOid)
      .flatMap(kuntakoodi => {
        if (
          Kunta.kuntaExists(kuntakoodi, koodistoPalvelu) &&
          !AhvenanmaanKunnat.onAhvenanmaalainenKunta(kuntakoodi) &&
          !Kunta.onPuuttuvaKunta(kuntakoodi)
        ) {
          Some(ValidatedKuntaInput(original = input, kunta = kuntakoodi))
        } else {
          None
        }
      })
      .toRight(ValpasErrorCategory.badRequest(s"Kunta ${input.kuntaOid} ei ole koodistopalvelun tuntema manner-Suomen kunta"))

  private def renderResult(result: Either[HttpStatus, Any]): Unit = {
    result match {
      case Right(r) => r match {
        case r: OppilaitosRaporttiResponse => writeExcel(r)
        case r: HeturouhinnanTulos => renderObject(r)
        case r: KuntarouhinnanTulos => renderObject(r)
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
}

case class HetuhakuInput(
  hetut: List[String],
  lang: Option[String],
  password: Option[String],
)

case class KuntaInput(
  kuntaOid: String,
  lang: Option[String],
  password: Option[String],
)

case class ValidatedKuntaInput(
  original: KuntaInput,
  kunta: String
)
