package fi.oph.koski.log

import org.scalatest.matchers.should.Matchers

object ReportUriLogTester extends Matchers with LogTester {
  override def appenderName = "ReportUri"
}
