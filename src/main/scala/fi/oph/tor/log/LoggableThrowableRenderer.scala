package fi.oph.tor.log

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.log4j.DefaultThrowableRenderer
import org.apache.log4j.spi.ThrowableRenderer

class LoggableThrowableRenderer extends ThrowableRenderer {

  val renderer = new DefaultThrowableRenderer

  override def doRender(t: Throwable): Array[String] = ExceptionUtils.getRootCause(t) match {
    case t: Loggable => omitStackTrace(t)
    case t: java.util.concurrent.TimeoutException => omitStackTrace(t)
    case t: java.sql.SQLTimeoutException => omitStackTrace(t)
    case _ => renderer.doRender(t)
  }

  def omitStackTrace(t: Throwable) = Array(t.getClass.getName + ": " + t.toString)
}
