package fi.oph.koski.log

import fi.oph.common.log.Loggable
import org.apache.log4j.DefaultThrowableRenderer
import org.apache.log4j.spi.ThrowableRenderer

// TODO: Siirrä common:iin, vaatii samalla aws-infra -muutoksen
class LoggableThrowableRenderer extends ThrowableRenderer {
  val renderer = new DefaultThrowableRenderer

  def getRootCause(t: Throwable): Throwable = t.getCause match {
    case null => t
    case cause: Throwable => getRootCause(cause)
  }

  override def doRender(t: Throwable): Array[String] = {
    getRootCause(t) match {
      case t: Loggable => omitStackTrace(t)
      case t: java.sql.SQLTimeoutException => omitStackTrace(t)
      case _ => renderer.doRender(t)
    }
  }

  def omitStackTrace(t: Throwable) = Array(signature(t))

  def signature(t: Throwable): String = t match {
    case t: Loggable => t.getClass.getName + ": " + t.logString
    case t: Throwable => t.getClass.getName + ": " + t.toString
  }
}
