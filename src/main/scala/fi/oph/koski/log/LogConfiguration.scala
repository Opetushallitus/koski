package fi.oph.koski.log

import java.io.File
import java.net.URL

object LogConfiguration {
  // The actual technical limit is currently docker's 16kB for line length in stdout. Set the limit to 5000, since
  // log4j might also add data to the entries and especially stack traces may take a lot space.
  val logMessageMaxLength: Int = 5000

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
      // Optional override file, merged on top via log4j2's Composite Configuration.
      // For example, enable some custom debug level logging for local development:
      // export KOSKI_LOG4J_CONF_OVERRIDE_FILE=log4j2-debug.xml
      val configOverride = Option(System.getenv("KOSKI_LOG4J_CONF_OVERRIDE_FILE"))
      val configurationFiles = (log4jConfig.getFile +: configOverride.toList).mkString(",")
      System.setProperty("log4j2.configurationFile", configurationFiles)
    }

    // Lisää logiviesteihin kentät file, line_number, class ja method
    System.setProperty("log4j.layout.jsonTemplate.locationInfoEnabled", "true")
  }
}
