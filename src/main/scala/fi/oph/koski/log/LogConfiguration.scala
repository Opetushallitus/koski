package fi.oph.koski.log

import java.io.File
import java.net.{MalformedURLException, URL}

import org.apache.log4j.PropertyConfigurator

object LogConfiguration {
  lazy val configureLoggingWithFileWatch: Unit = {
    var prop = System.getProperty("log4j.configuration", "src/main/resources/log4j.properties");
    val log4jConfig = try {
      new URL(prop)
    } catch {
      case e: MalformedURLException => new URL("file://" + new File(prop).getCanonicalPath)
    }
    if (log4jConfig.getProtocol().equalsIgnoreCase("file")) {
      PropertyConfigurator.configureAndWatch(log4jConfig.getFile(), 1000);
    }
  }
}
