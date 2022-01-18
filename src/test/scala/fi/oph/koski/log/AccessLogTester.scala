package fi.oph.koski.log

import org.apache.logging.log4j.core.LogEvent
import org.scalatest.matchers.should.Matchers

object AccessLogTester extends Matchers with LogTester {
  override def appenderName = "Access"

  def getLatestMatchingAccessLog(str: String): String = {
    val timeoutMs = 5000
    val timeoutAt = System.currentTimeMillis() + timeoutMs
    var log: Option[String] = None
    while(log.isEmpty) {
      log = getLogMessages.reverse.find(msg => msg.contains(str))
      if (System.currentTimeMillis > timeoutAt) {
        throw new RuntimeException("Wait timed out at " + timeoutMs + " milliseconds")
      }
      Thread.sleep(100)
    }
    log.get
  }
}
