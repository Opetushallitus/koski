package fi.oph.koski.servlet

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.JsonSerializer
import org.json4s._
import org.scalatra._
import rx.lang.scala.Observable

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

  def get[T: ru.TypeTag](s: String)(action: => T): Route =
    super.get(s)(render(action))

  def post[T: ru.TypeTag](s: String)(action: => T): Route =
    super.post(s)(render(action))

  def put[T: ru.TypeTag](s: String)(action: => T): Route =
    super.put(s)(render(action))

  def delete[T: ru.TypeTag](s: String)(action: => T): Route =
    super.delete(s)(render(action))

  def render[T: ru.TypeTag](action: => T): Any = {
    action match {
      case _: Unit => ()
      case s: HttpStatus => renderStatus(s)
      case in: Observable[AnyRef @unchecked] => in
      case x => renderObject(x)
    }
  }
}

trait ApiServletRequiringAuthentication extends ApiServlet with RequiresAuthentication {
  override def toJsonString[T: ru.TypeTag](x: T): String = {
    JsonSerializer.write(x)
  }
}

