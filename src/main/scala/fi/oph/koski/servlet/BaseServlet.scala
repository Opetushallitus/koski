package fi.oph.koski.servlet

import fi.oph.koski.http.{ErrorCategory, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Session}
import fi.oph.koski.log.{LoggerWithContext, Logging}
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import org.eclipse.jetty.http.BadMessageException
import org.scalatra._

import java.time.LocalDate
import java.time.format.DateTimeParseException
import scala.reflect.runtime.{universe => ru}
import scala.xml.Elem

trait BaseServlet extends ScalatraServlet with Logging {
  override protected def logger: LoggerWithContext = {
    try {
      logger(koskiSessionOption)
    } catch {
      case e: Throwable => super.logger
    }
  }

  private def getNonOptionalWith[T](name: String, fn: (String) => Option[T]): T = {
    fn(name).getOrElse(throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.missing, "Missing " + name))
  }

  def getIntegerParam(name: String): Int = {
    params.getAs[Int](name) match {
      case Some(id) if id >= 0 =>
        id
      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.format.number, "Invalid " + name + " : " + params(name))
    }
  }

  def getOptionalIntegerParam(name: String): Option[Int] = {
    params.get(name).map(_ => getIntegerParam(name))
  }

  def getOptionalStringParam(name: String): Option[String] = params.get(name)

  def getStringParam(name: String): String = getNonOptionalWith(name, getOptionalStringParam)

  def getOptionalBooleanParam(name: String): Option[Boolean] = params.getAs[Boolean](name)

  def getBooleanParam(name: String): Boolean = getNonOptionalWith(name, getOptionalBooleanParam)

  def getLocalDateParam(param: String): LocalDate = {
    try {
      LocalDate.parse(getStringParam(param))
    } catch {
      case e: DateTimeParseException => haltWithStatus(KoskiErrorCategory.badRequest.format.pvm())
    }
  }

  error {
    case e: BadMessageException =>
      haltWithStatus(KoskiErrorCategory.badRequest(e.getReason))
    case InvalidRequestException(detail) =>
      haltWithStatus(detail)
    case e: Throwable =>
      haltWithInternalError(e)
  }

  notFound {
    // disable Scalatra's default "not found" page
    haltWithStatus(KoskiErrorCategory.notFound())
  }

  override protected def renderPipeline: RenderPipeline = ({
    case s: HttpStatus =>
      renderStatus(s)
    case e: Elem =>
      renderHtml(e)
    case e: Throwable =>
      haltWithInternalError(e)
    case s: String =>
      haltWithInternalError(new RuntimeException(s"Unexpected string returned to render pipeline: $s"))
    case o: Any =>
      haltWithInternalError(new RuntimeException(s"Unexpected object returned to render pipeline: ${o.toString}"))
  }: RenderPipeline) orElse super.renderPipeline

  private def haltWithInternalError(e: Throwable): Nothing = {
    try {
      logger.error(e)("Error while processing request " + logSafeDescription(request))
    } catch {
      case e: Throwable => println(s"Logging failed: $e")
    }
    haltWithStatus(KoskiErrorCategory.internalError())
  }

  def renderOption[T: ru.TypeTag](errorCategory: ErrorCategory)(result: Option[T]): Unit = {
    result match {
      case Some(x) => renderObject(x)
      case _ => haltWithStatus(errorCategory())
    }
  }

  def renderEither[T: ru.TypeTag](result: Either[HttpStatus, T]): Unit = {
    result match {
      case Right(x) => renderObject(x)
      case Left(status) => haltWithStatus(status)
    }
  }

  def koskiSessionOption: Option[Session]

  def renderStatus(status: HttpStatus): Unit

  def renderHtml(e: scala.xml.NodeSeq): Unit = {
    contentType = "text/html"
    response.writer.print("<!DOCTYPE html>\n" + e.toString)
  }

  def renderObject[T: ru.TypeTag](x: T): Unit

  def haltWithStatus(status: HttpStatus): Nothing = {
    halt(status.statusCode, status)
  }


  def callsOnlyFrom(acceptedHosts: List[String]): Unit = {
    if (!acceptedHosts.contains(request.getRemoteHost)) {
      logger.error("Kutsu ei ole sallittu osoitteesta: " + request.getRemoteHost)
      haltWithStatus(KoskiErrorCategory.forbidden(""))
    }
  }
}

trait KoskiSpecificBaseServlet extends BaseServlet {
  def koskiSessionOption: Option[KoskiSpecificSession]
}
