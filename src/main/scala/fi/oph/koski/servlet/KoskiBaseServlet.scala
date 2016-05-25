package fi.oph.koski.servlet

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.log.{LoggerWithContext, Logging}
import fi.oph.koski.koskiuser.{KoskiUser, KoskiUser$}
import org.json4s._
import org.scalatra._

import scala.xml.Elem

trait KoskiBaseServlet extends ScalatraServlet with Logging {
  override protected def logger: LoggerWithContext = logger(torUserOption)

  def getIntegerParam(name: String) = {
    params.getAs[Int](name) match {
      case Some(id) if id > 0 => id
      case _ =>
        // TODO: ensure handling
        throw new InvalidRequestException(KoskiErrorCategory.badRequest.format.number, "Invalid " + name + " : " + params(name))
    }
  }

  def torUserOption: Option[KoskiUser] = None

  error {
    case InvalidRequestException(detail) =>
      haltWithStatus(detail)
    case e: Throwable =>
      haltWithInternalError(e)
  }

  override protected def renderPipeline: RenderPipeline = ({
    case s: HttpStatus =>
      renderStatus(s)
    case e: Elem =>
      super.renderPipeline(e)
    case x: AnyRef =>
      renderObject(x)
  }: RenderPipeline) orElse super.renderPipeline

  def haltWithInternalError(e: Throwable) = {
    logger.error(e)("Error while processing request " + describeRequest)
    haltWithStatus(KoskiErrorCategory.internalError())
  }

  def describeRequest: String = {
    val query: String = if (request.getQueryString == null) {""} else {"?" + request.getQueryString}
    val requestDescription: String = request.getMethod + " " + request.getServletPath + query + " " + maskRequestBody
    requestDescription
  }

  private def maskRequestBody = {
    (request.body, request.contentType) match {
      case ("", _) => ""
      case (body, Some(contentType)) if (contentType.contains("application/json")) =>
        try {
          val parsedJson: JValue = org.json4s.jackson.JsonMethods.parse(request.body)
          val maskedJson: JValue = maskSensitiveInformation(parsedJson)
          Json.write(maskedJson)
        } catch {
          case e: Exception => body
        }
      case (body, _) => body
    }
  }

  def renderStatus(status: HttpStatus): Unit

  def renderObject(x: AnyRef): Unit

  def haltWithStatus(status: HttpStatus) = {
    halt(status.statusCode, status)
  }
}
