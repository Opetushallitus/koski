package fi.oph.koski.util

import fi.oph.koski.log.LoggerWithContext
import fi.oph.koski.util.ChainingSyntax.eitherChainingOps

import scala.util.{Try, Using}

object TryWithLogging {
  def apply[T](logger: LoggerWithContext, f: => T): Either[Throwable, T] =
    handle(logger, Try(f))

  def andResources[T](logger: LoggerWithContext, f: Using.Manager => T): Either[Throwable, T] =
    handle(logger, Using.Manager(f))

  private def handle[T](logger: LoggerWithContext, t: Try[T]): Either[Throwable, T] =
    t.toEither.tapLeft(e => logger.error(e)(e.getMessage))
}
