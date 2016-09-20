package fi.oph.koski.jettylauncher

import java.lang.management.ManagementFactory
import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.cache.JMXCacheManager
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.LogConfiguration
import fi.oph.koski.util.{Pools, PortChecker}
import org.eclipse.jetty.jmx.MBeanContainer
import org.eclipse.jetty.server.handler.StatisticsHandler
import org.eclipse.jetty.server.{Server, ServerConnector, Slf4jRequestLog}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends App {
  lazy val globalPort = System.getProperty("tor.port","7021").toInt
  new JettyLauncher(globalPort).start.join
}

class JettyLauncher(val port: Int, overrides: Map[String, String] = Map.empty) {
  val config = overrides.toList.foldLeft(KoskiApplication.defaultConfig)({ case (config, (key, value)) => config.withValue(key, fromAnyRef(value)) })
  val application = new KoskiApplication(config, new JMXCacheManager)
  application.database // <- force evaluation to make sure DB is up

  lazy val threadPool = new QueuedThreadPool(Pools.jettyThreads, 10);
  lazy val server = new Server(threadPool)

  configureLogging

  lazy val connector: ServerConnector = new ServerConnector(server)
  connector.setPort(port)
  server.addConnector(connector)

  def resourceBase = System.getProperty("resourcebase", "./target/webapp")

  if(!Files.exists(Paths.get(resourceBase))) {
    throw new RuntimeException("WebApplication resource base: " + resourceBase + " does not exist.")
  }

  val context = new WebAppContext()

  context.setParentLoaderPriority(true)
  context.setContextPath("/koski")
  context.setResourceBase(resourceBase)
  context.setAttribute("koski.application", application)
  server.setHandler(context)

  val mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer())
  server.addBean(mbContainer)

  val stats = new StatisticsHandler
  stats.setHandler(server.getHandler())
  server.setHandler(stats)

  def start = {
    server.start
    server
  }

  def baseUrl = "http://localhost:" + port + "/koski"

  def configureLogging: Unit = {
    LogConfiguration.configureLoggingWithFileWatch
    val requestLog = new Slf4jRequestLog()
    requestLog.setLogLatency(true)
    server.setRequestLog(requestLog)
  }
}

object SharedJetty extends JettyLauncher(PortChecker.findFreeLocalPort, Map("db.name" -> "tortest", "fixtures.use" -> "true"))