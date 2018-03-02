package fi.oph.koski.servlet

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.util.PaginatedResponse
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.scalatra._

import scala.reflect.runtime.universe.{TypeRefApi, TypeTag}

trait ApiServlet extends KoskiBaseServlet with Logging with TimedServlet with ContentEncodingSupport with CacheControlSupport {
  def withJsonBody(block: JValue => Any)(parseErrorHandler: HttpStatus => Any = haltWithStatus) = {
    JsonBodySnatcher.getJsonBody(request) match {
      case Right(x) => block(x)
      case Left(status: HttpStatus) => parseErrorHandler(status)
    }
  }

  def renderStatus(status: HttpStatus) = {
    response.setStatus(status.statusCode)
    renderObject(status.errors)
  }

  def renderObject[T: TypeTag](x: T): Unit = {
    writeJson(toJsonString(x))
  }

  def toJsonString[T: TypeTag](x: T): String = {
    implicit val session = koskiSessionOption getOrElse KoskiSession.untrustedUser
    // Ajax request won't have "text/html" in Accept header, clicking "JSON" button will
    val pretty = Option(request.getHeader("accept")).exists(_.contains("text/html"))
    val tag = implicitly[TypeTag[T]]
    tag.tpe match {
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
      case t =>
        JsonSerializer.write(x, pretty)
    }
  }

  private def writeJson(str: String): Unit = {
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
}
