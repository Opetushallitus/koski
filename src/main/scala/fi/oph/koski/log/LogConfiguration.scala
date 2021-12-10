package fi.oph.koski.log

import org.apache.log4j.PropertyConfigurator

import java.io.File
import java.net.URL

object LogConfiguration {
  private val LocalLog4jConfigPath = "src/main/resources/log4j-local.properties"

  def configureLoggingWithFileWatch(): Unit = {
    val log4jConfig = Option(System.getProperty("log4j.configuration")) match {
      case Some(log4jConfigPath) => new URL(log4jConfigPath)
      case None => new URL("file://" + new File(LocalLog4jConfigPath).getCanonicalPath)
    }

    if (log4jConfig.getProtocol.equalsIgnoreCase("file")) {
      PropertyConfigurator.configureAndWatch(log4jConfig.getFile, 1000)
    }
  }

  // The actual technical limit is currently docker's 16kB for line length in stdout. Set the limit to 15000, since
  // log4j might also add data to the entries.
  val logMessageMaxLength: Int = 15000
}
