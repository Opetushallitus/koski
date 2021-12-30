package fi.oph.koski.log

trait LogTester {
  def getLogMessages: Seq[String] = StubLogs.getLogs(appenderName)
  def clearMessages(): Unit = StubLogs.clear()
  def appenderName: String = "Root"
}
