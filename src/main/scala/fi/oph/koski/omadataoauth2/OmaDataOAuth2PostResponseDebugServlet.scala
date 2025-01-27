package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.{KoskiApplication}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport
import org.scalatra.forms._
import org.scalatra.i18n.I18nSupport

class OmaDataOAuth2PostResponseDebugServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with Logging with ContentEncodingSupport with NoCache with FormSupport with I18nSupport with Unauthenticated
{
  post("/") {
    val result = validate(SubmitRequest.formSubmitRequest)(
      (errors: Seq[(String, String)]) => {
        Left(KoskiErrorCategory.badRequest(errors.map { case (a, b) => s"${a}: ${b}" }.mkString(";")))
      },
      (submitRequest: SubmitRequest) => {
        Right(submitRequest)
      }
    )

    renderEither(result)
  }
}

object SubmitRequest {
  val formSubmitRequest: MappingValueType[SubmitRequest] = mapping(
    "code" -> label("code", optional(text())),
    "state" -> label("state", optional(text())),
    "error" -> label("error", optional(text())),
    "error_description" -> label("error_description", optional(text())),
    "error_uri" -> label("error_uri", optional(text())),

  )(SubmitRequest.apply)
}

case class SubmitRequest(
  code: Option[String],
  state: Option[String],
  error: Option[String],
  error_description: Option[String],
  error_uri: Option[String],
)
