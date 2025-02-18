package fi.oph.koski.servlet

import fi.oph.koski.db.SuoritusjakoRow
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.util.PaginatedResponse
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.scalatra._

import java.time.LocalDate
import scala.reflect.runtime.universe.{TypeRefApi, TypeTag}
import scala.reflect.runtime.{universe => ru}
import scala.runtime.BoxedUnit

trait ApiServlet extends BaseServlet with Logging with TimedServlet with ContentEncodingSupport with CacheControlSupport {
  def withJsonBody[T: TypeTag](block: JValue => T)(parseErrorHandler: HttpStatus => T = haltWithStatus(_)): T = {
    JsonBodySnatcher.getJsonBody(request) match {
      case Right(x) => block(x)
      case Left(status: HttpStatus) => parseErrorHandler(status)
    }
  }

  def renderWithJsonBody[T: TypeTag](fn: T => Either[HttpStatus, _]): Unit = {
    withJsonBody({ body => renderEither(fn(JsonSerializer.extract[T](body))) })()
  }

  def renderStatus(status: HttpStatus) = {
    response.setStatus(status.statusCode)
    renderObject(status.errors)
  }

  def renderObject[T: TypeTag](x: T): Unit = {
    x match {
      case _: Unit => response.setStatus(204)
      case _: BoxedUnit => response.setStatus(204)
      case _ => writeJson(toJsonString(x))
    }
  }

  def toJsonString[T: TypeTag](x: T): String

  protected def writeJson(str: String): Unit = {
    contentType = "application/json;charset=utf-8"
    response.writer.print(str)
  }

  def get[T: TypeTag](s: String)(action: => T): Route =
    super.get(s)(render(action))

  def post[T: TypeTag](s: String)(action: => T): Route =
    super.post(s)(render(action))

  def put[T: TypeTag](s: String)(action: => T): Route =
    super.put(s)(render(action))

  def delete[T: TypeTag](s: String)(action: => T): Route =
    super.delete(s)(render(action))

  def render[T: TypeTag](action: => T): Any = {
    action match {
      case _: Unit => ()
      case s: HttpStatus => renderStatus(s)
      case x => renderObject(x)
    }
  }

  def includeClassReferences: Boolean =
    Option(request.getParameter("class_refs")).contains("true")

}

trait KoskiSpecificApiServlet extends ApiServlet with KoskiSpecificBaseServlet {

  def renderEither[T: ru.TypeTag](result: Either[HttpStatus, T], sessionOverride: KoskiSpecificSession): Unit = {
    result match {
      case Right(x) => renderObject[T](x, sessionOverride)
      case Left(status) => haltWithStatus(status)
    }
  }

  def renderObject[T: TypeTag](x: T, sessionOverride: KoskiSpecificSession): Unit = {
    x match {
      case _: Unit => response.setStatus(204)
      case _: BoxedUnit => response.setStatus(204)
      case _ => writeJson(toJsonString[T](x, sessionOverride))
    }
  }

  def toJsonString[T: TypeTag](x: T): String = toJsonString(x, KoskiSpecificSession.untrustedUser)

  def toJsonString[T: TypeTag](x: T, sessionOverride: KoskiSpecificSession): String = {
    implicit val session = koskiSessionOption getOrElse sessionOverride
    // Ajax request won't have "text/html" in Accept header, clicking "JSON" button will
    val pretty = Option(request.getHeader("accept")).exists(_.contains("text/html"))
    val tag = implicitly[TypeTag[T]]
    tag.tpe match {
      case t: TypeRefApi if (t.typeSymbol.asClass.fullName == classOf[RawJsonResponse].getName) =>
        x.asInstanceOf[RawJsonResponse].response
      case t: TypeRefApi if (t.typeSymbol.asClass.fullName == classOf[PaginatedResponse[_]].getName) =>
        // Here's some special handling for PaginatedResponse (scala-schema doesn't support parameterized case classes yet)
        val typeArg = t.args.head
        val paginated = x.asInstanceOf[PaginatedResponse[_]]
        val subSchema = KoskiSchema.schemaFactory.createSchema(typeArg)
        JsonMethods.compact(JObject(
          "result" -> JsonSerializer.serialize(paginated.result, subSchema),
          "paginationSettings" -> JsonSerializer.serialize(paginated.paginationSettings),
          "mayHaveMore" -> JBool(paginated.mayHaveMore)
        ))
      case t: Any =>
        JsonSerializer.write(x, pretty, includeClassReferences)
    }
  }
}

case class RawJsonResponse(response: String)
