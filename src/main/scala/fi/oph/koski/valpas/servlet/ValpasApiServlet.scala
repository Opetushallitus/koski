package fi.oph.koski.valpas.servlet

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.UserLanguage.sanitizeLanguage
import fi.oph.koski.servlet.ApiServlet
import fi.oph.koski.util.PaginatedResponse
import fi.oph.koski.valpas.valpasuser.ValpasSession

import scala.reflect.runtime.universe.{TypeRefApi, TypeTag}

trait ValpasApiServlet extends ApiServlet with ValpasBaseServlet {
  def toJsonString[T: TypeTag](x: T): String = {
    implicit val session = koskiSessionOption getOrElse ValpasSession.untrustedUser
    // Ajax request won't have "text/html" in Accept header, clicking "JSON" button will
    val pretty = Option(request.getHeader("accept")).exists(_.contains("text/html"))
    val tag = implicitly[TypeTag[T]]
    tag.tpe match {
      case t: TypeRefApi if (t.typeSymbol.asClass.fullName == classOf[PaginatedResponse[_]].getName) =>
        throw new RuntimeException("PaginatedResponsen serialisointia ei toteutettu")
      case t =>
        JsonSerializer.write(x, pretty)
    }
  }

  def langFromCookie: Option[String] = sanitizeLanguage(request.cookies.get("lang"))
}
