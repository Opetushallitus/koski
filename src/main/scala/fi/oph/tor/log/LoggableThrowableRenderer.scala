package fi.oph.tor.log

import org.apache.log4j.DefaultThrowableRenderer
import org.apache.log4j.spi.ThrowableRenderer
import org.http4s.ParseException

class LoggableThrowableRenderer extends ThrowableRenderer {
  val renderer = new DefaultThrowableRenderer

  def getRootCause(t: Throwable): Throwable = t.getCause match {
    case null => t
    case cause: Throwable => getRootCause(cause)
  }

  override def doRender(t: Throwable): Array[String] = {
    getRootCause(t) match {
      case t: Loggable => omitStackTrace(t)
      case t: java.util.concurrent.TimeoutException => omitStackTrace(t)
      case t: java.sql.SQLTimeoutException => omitStackTrace(t)
      case t: ParseException => renderer.doRender(t) ++ Array("Details :" +  t.failure.details)
      case _ => renderer.doRender(t)
    }
  }

  def omitStackTrace(t: Throwable) = Array(signature(t))

  def signature(t: Throwable): String = t.getClass.getName + ": " + t.toString
}
