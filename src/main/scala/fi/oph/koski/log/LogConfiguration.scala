package fi.oph.koski.log

import java.io.File
import java.net.URL

object LogConfiguration {
  // The actual technical limit is currently docker's 16kB for line length in stdout. Set the limit to 15000, since
  // log4j might also add data to the entries.
  val logMessageMaxLength: Int = 15000

  private val LocalLog4jConfigPath = "src/main/resources/log4j2-local.xml"
  private val TestLog4jConfigPath = "src/main/resources/log4j2-test.xml"

  def configureLogging(isTestEnvironment: Boolean = false): Unit = {
    val log4jConfig = Option(System.getProperty("log4j.configuration"))
      .map(path => new URL(path))
      .getOrElse({
        val configPath = if (isTestEnvironment) TestLog4jConfigPath else LocalLog4jConfigPath
        new URL("file://" + new File(configPath).getCanonicalPath)
      })

    if (log4jConfig.getProtocol.equalsIgnoreCase("file")) {
      System.setProperty("log4j2.configurationFile", log4jConfig.getFile)
    }
  }
}
