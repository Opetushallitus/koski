package fi.oph.koski.log

import org.log4s._

trait Logging {
  protected lazy val defaultLogger: LoggerWithContext = LoggerWithContext(getClass)
  protected def logger : LoggerWithContext = defaultLogger
  protected def logger(user: LogUserContext): LoggerWithContext = defaultLogger.copy(context = Some(user))
  protected def logger(user: Option[LogUserContext]): LoggerWithContext = defaultLogger.copy(context = user)

  protected def tryCatch(thing: String)(task: => Unit): Unit = {
    try {
      task
    } catch {
      case e: Exception => logger.error(e)(thing + " epäonnistui: " + e.getMessage)
    }
  }
}

case class LoggerWithContext(
  logger: Logger,
  context: Option[LogUserContext],
) {
  def debug(msg: => String) = logger.debug(fmt(msg))
  def info(msg: => String) = logger.info(fmt(msg))
  def warn(msg: => String) = logger.warn(fmt(msg))
  def warn(e: Throwable)(msg: => String) = logger.warn(e)(fmt(msg))
  def error(msg: => String) = logger.error(fmt(msg))
  def error(e: Throwable)(msg: => String) = logger.error(e)(fmt(msg))

  def withUserContext(context: LogUserContext) = this.copy(context = Some(context))

  private def fmt(msg: => String) = {
    val cutMsg = cutToMaxLength(msg)

    context match {
      case Some(ctx) => ctx.userOption match {
        case Some(user) => s"${user.username}(${user.oid})@${ctx.clientIp} " + cutMsg
        case None =>  ctx.clientIp + " " + cutMsg
      }
      case None => cutMsg
    }
  }

  private def cutToMaxLength(msg: => String) = {
    if (msg.length > LogConfiguration.logMessageMaxLength) {
      msg.take(LogConfiguration.logMessageMaxLength - 3) + "..."
    } else {
      msg
    }
  }
}

object LoggerWithContext {
  def apply(klass: Class[_], context: Option[LogUserContext] = None): LoggerWithContext = LoggerWithContext(getLogger(klass), context)
}
