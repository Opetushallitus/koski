package fi.oph.koski.log

import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggingEvent
import org.scalatest.matchers.should.Matchers

object AccessLogTester extends Matchers with LogTester {
  override def getLogger: Logger = Logger.getLogger("org.eclipse.jetty.server.RequestLog")

  def getLatestMatchingAccessLog(str: String) = {
    val timeoutMs = 5000
    val timeoutAt = System.currentTimeMillis() + timeoutMs
    var log: Option[LoggingEvent] = None
    while(log.isEmpty) {
      log = getLogMessages.reverse.find(msg => msg.getMessage.toString.contains(str))
      if (System.currentTimeMillis > timeoutAt) {
        throw new RuntimeException("Wait timed out at " + timeoutMs + " milliseconds")
      }
      Thread.sleep(100)
    }
    log.get.getMessage.toString
  }
}
