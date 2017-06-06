package fi.oph.koski.servlet

import fi.oph.koski.http.{ErrorCategory, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.{LoggerWithContext, Logging}
import fi.oph.koski.servlet.RequestDescriber.logSafeDescription
import org.scalatra._

import scala.xml.Elem

trait KoskiBaseServlet extends ScalatraServlet with Logging {
  override protected def logger: LoggerWithContext = {
    try {
      logger(koskiSessionOption)
    } catch {
      case e: Throwable => super.logger
    }
  }

  def lang = params.get("lang").getOrElse("fi")

  def getIntegerParam(name: String): Int = {
    params.getAs[Int](name) match {
      case Some(id) if id >= 0 =>
        id
      case None =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.missing(s"""Kyselyparametri $name puuttuu"""))
      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.format.number, "Invalid " + name + " : " + params(name))
    }
  }

  def getOptionalIntegerParam(name: String) = params.get(name) map { _ =>
    getIntegerParam(name)
  }

  error {
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
      super.renderPipeline(e)
    case x: AnyRef =>
      renderObject(x)
  }: RenderPipeline) orElse super.renderPipeline

  def haltWithInternalError(e: Throwable) = {
    logger.error(e)("Error while processing request " + logSafeDescription(request))
    haltWithStatus(KoskiErrorCategory.internalError())
  }

  def renderOption[T <: AnyRef](errorCategory: ErrorCategory)(result: Option[T]) = {
    result match {
      case Some(x) => renderObject(x)
      case _ => haltWithStatus(errorCategory())
    }
  }

  def renderEither[T <: AnyRef](result: Either[HttpStatus, T]) = {
    result match {
      case Right(x) => renderObject(x)
      case Left(status) => haltWithStatus(status)
    }
  }

  def koskiSessionOption: Option[KoskiSession]

  def renderStatus(status: HttpStatus): Unit

  def renderObject(x: AnyRef): Unit

  def haltWithStatus(status: HttpStatus): Nothing = {
    halt(status.statusCode, status)
  }
}


