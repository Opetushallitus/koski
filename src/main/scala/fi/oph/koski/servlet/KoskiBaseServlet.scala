package fi.oph.koski.servlet

import fi.oph.koski.http.{ErrorCategory, HttpStatus, KoskiErrorCategory}
import fi.oph.common.koskiuser.KoskiSession
import fi.oph.common.log.{LoggerWithContext, Logging}
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import org.eclipse.jetty.http.BadMessageException
import org.scalatra._

import scala.reflect.runtime.{universe => ru}
import scala.xml.Elem

trait KoskiBaseServlet extends ScalatraServlet with Logging {
  override protected def logger: LoggerWithContext = {
    try {
      logger(koskiSessionOption)
    } catch {
      case e: Throwable => super.logger
    }
  }

  def getIntegerParam(name: String): Int = {
    params.getAs[Int](name) match {
      case Some(id) if id >= 0 =>
        id
      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.format.number, "Invalid " + name + " : " + params(name))
    }
  }

  def getStringParam(name: String): String = {
    params.get(name).getOrElse(throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.missing, "Missing " + name))
  }

  def getOptionalStringParam(name: String): Option[String] = params.get(name) map { _ =>
    getStringParam(name)
  }

  def getOptionalIntegerParam(name: String) = params.get(name) map { _ =>
    getIntegerParam(name)
  }

  def getBooleanParam(name: String, defaultValue: Boolean = false): Boolean = {
    params.getAs[Boolean](name).getOrElse(defaultValue)
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
    case x: AnyRef =>
      renderObject(x)
  }: RenderPipeline) orElse super.renderPipeline

  def haltWithInternalError(e: Throwable) = {
    logger.error(e)("Error while processing request " + logSafeDescription(request))
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

  def koskiSessionOption: Option[KoskiSession]

  def renderStatus(status: HttpStatus): Unit

  def renderHtml(e: scala.xml.NodeSeq): Unit = {
    contentType = "text/html"
    response.writer.print("<!DOCTYPE html>\n" + e.toString)
  }

  def renderObject[T: ru.TypeTag](x: T): Unit

  def haltWithStatus(status: HttpStatus): Nothing = {
    halt(status.statusCode, status)
  }

  def noRemoteCallsExpectFor(expectFor: String*): Unit = {
    if (expectFor.exists(request.pathInfo.endsWith(_))) {
      //ok
    } else {
      noRemoteCalls()
    }
  }
  def noRemoteCalls(): Unit = {
    if (!List("127.0.0.1", "[0:0:0:0:0:0:0:1]").contains(request.getRemoteHost)) {
      haltWithStatus(KoskiErrorCategory.forbidden(""))
    }
  }
}


