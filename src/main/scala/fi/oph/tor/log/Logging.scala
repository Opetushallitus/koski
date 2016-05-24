package fi.oph.tor.log

import fi.oph.tor.toruser.TorUser
import org.log4s._

trait Logging {
  private lazy val log4sLogger: Logger = getLogger(getClass)

  protected def logger : LoggerWithContext = LoggerWithContext(log4sLogger, None)
  protected def logger(user: TorUser): LoggerWithContext = LoggerWithContext(log4sLogger, Some(user))
  protected def logger(user: Option[TorUser]): LoggerWithContext = LoggerWithContext(log4sLogger, user)
}

case class LoggerWithContext(logger: Logger, user: Option[TorUser]) {
  def info(msg: => String) = logger.info(fmt(msg))
  def warn(msg: => String) = logger.warn(fmt(msg))
  def error(msg: => String) = logger.error(fmt(msg))
  def error(e: Throwable)(msg: => String) = logger.error(e)(fmt(msg))
  def warn(e: Throwable)(msg: => String) = logger.warn(e)(fmt(msg))

  private def fmt(msg: => String) = user match {
    case Some(user) => s"${user.oid}@${user.clientIp} " + msg
    case None => msg
  }
}

