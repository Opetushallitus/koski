package fi.oph.koski.log

import org.apache.log4j.Logger
import org.scalatest.Matchers

object AccessLogTester extends Matchers with LogTester {
  override def getLogger: Logger = Logger.getLogger("org.eclipse.jetty.server.RequestLog")
}
