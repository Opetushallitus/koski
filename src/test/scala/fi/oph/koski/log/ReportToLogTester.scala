package fi.oph.koski.log

import org.scalatest.matchers.should.Matchers

object ReportToLogTester extends Matchers with LogTester {
  override def appenderName = "ReportTo"
}
