package fi.oph.koski.servlet

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.JsonSerializer
import org.json4s._
import org.scalatra._
import scala.reflect.runtime.{universe => ru}

trait ApiServlet extends KoskiBaseServlet with Logging with TimedServlet with GZipSupport {
  def withJsonBody(block: JValue => Any)(parseErrorHandler: HttpStatus => Any = haltWithStatus) = {
    JsonBodySnatcher.getJsonBody(request) match {
      case Right(x) => block(x)
      case Left(status: HttpStatus) => parseErrorHandler(status)
    }
  }

  def renderStatus(status: HttpStatus) = {
    response.setStatus(status.statusCode)
    writeJson(Json.write(status.errors))
  }

  def renderObject[T: ru.TypeTag](x: T): Unit = {
    writeJson(toJsonString(x))
  }

  def toJsonString[T: ru.TypeTag](x: T): String = Json.write(x.asInstanceOf[AnyRef])

  private def writeJson(str: String): Unit = {
    contentType = "application/json;charset=utf-8"
    response.writer.print(str)
  }
}

trait ApiServletRequiringAuthentication extends ApiServlet with RequiresAuthentication {
  override def toJsonString[T: ru.TypeTag](x: T): String = {
    JsonSerializer.write(x)
  }
}

