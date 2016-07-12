package fi.oph.koski.servlet
import javax.servlet.http.HttpServletRequest

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import org.json4s._
import org.json4s.jackson.JsonMethods
import org.scalatra.servlet.ServletApiImplicits._

object JsonBodySnatcher {
  def getJsonBody[T](request: HttpServletRequest): Either[HttpStatus, JValue] = {
    (request.contentType.map(_.split(";")(0).toLowerCase), request.characterEncoding.map(_.toLowerCase)) match {
      case (Some("application/json"), Some("utf-8")) =>
        try {
          Right(JsonMethods.parse(request.body))
        } catch {
          case e: Exception => Left(KoskiErrorCategory.badRequest.format.json("Invalid JSON"))
        }
      case _ =>
        Left(KoskiErrorCategory.unsupportedMediaType.jsonOnly())
    }
  }
}
