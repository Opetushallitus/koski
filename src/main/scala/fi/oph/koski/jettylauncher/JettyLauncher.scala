package fi.oph.koski.jettylauncher

import java.lang.management.ManagementFactory
import java.nio.file.{Files, Paths}
import javax.management.ObjectName

import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.cache.JMXCacheManager
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.Pools
import fi.oph.koski.log.{LogConfiguration, Logging}
import fi.oph.koski.util.PortChecker
import io.prometheus.client.exporter.MetricsServlet
import org.eclipse.jetty.jmx.MBeanContainer
import org.eclipse.jetty.server.handler.{HandlerCollection, StatisticsHandler}
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends App with Logging {
  lazy val globalPort = System.getProperty("koski.port","7021").toInt
  try {
    new JettyLauncher(globalPort).start.join
  } catch {
    case e: Throwable =>
      logger.error(e)("Error in server startup")
      System.exit(1)
  }
}

class JettyLauncher(val port: Int, overrides: Map[String, String] = Map.empty) extends Logging {

  private val config = overrides.toList.foldLeft(KoskiApplication.defaultConfig)({ case (config, (key, value)) => config.withValue(key, fromAnyRef(value)) })
  val application = new KoskiApplication(config, new JMXCacheManager)

  private val threadPool = new ManagedQueuedThreadPool(Pools.jettyThreads, 10);

  private val server = new Server(threadPool)

  application.masterDatabase // <- force evaluation to make sure DB is up

  configureLogging
  setupConnector

  private val handlers = new HandlerCollection()

  server.setHandler(handlers)

  setupKoskiApplicationContext
  setupGzipForStaticResources
  setupJMX
  setupPrometheusMetrics

  def start = {
    server.start
    server
  }

  def baseUrl = "http://localhost:" + port + "/koski"

  private def setupConnector = {
    val httpConfig = new HttpConfiguration()
    httpConfig.addCustomizer( new ForwardedRequestCustomizer() )
    val connectionFactory = new HttpConnectionFactory( httpConfig )

    val connector = new ServerConnector(server, connectionFactory)
    connector.setPort(port)
    server.addConnector(connector)
  }

  protected def configureLogging = {
    LogConfiguration.configureLoggingWithFileWatch
    val requestLog = new Slf4jRequestLog()
    requestLog.setLogLatency(true)
    server.setRequestLog(requestLog)
  }

  private def setupKoskiApplicationContext = {
    val context = new WebAppContext()
    context.setAttribute("koski.application", application)
    context.setParentLoaderPriority(true)
    context.setContextPath("/koski")
    context.setResourceBase(resourceBase)
    context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false")
    handlers.addHandler(context)
  }

  private def resourceBase = if (isRunningAws) {
    JettyLauncher.getClass.getClassLoader.getResource("webapp").toExternalForm
  } else {
    val base = System.getProperty("resourcebase", "./target/webapp")
    if (!Files.exists(Paths.get(base))) {
      throw new RuntimeException("WebApplication resource base: " + base + " does not exist.")
    }
    base
  }

  private def setupGzipForStaticResources = {
    val gzip = new GzipHandler
    gzip.setIncludedMimeTypes("text/css", "text/html", "application/javascript")
    gzip.setIncludedPaths("/koski/css/*", "/koski/external_css/*", "/koski/js/*", "/koski/json-schema-viewer/*")
    gzip.setHandler(server.getHandler())
    server.setHandler(gzip)
  }

  private def setupJMX = {
    val mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer())
    server.addBean(mbContainer)

    val stats = new StatisticsHandler
    stats.setHandler(server.getHandler())
    server.setHandler(stats)
  }

  private def setupPrometheusMetrics = {
    val context = new ServletContextHandler()
    val pathSpec = if (isRunningAws) "/koski-metrics" else "/metrics"
    context.setContextPath("/")
    context.addServlet(new ServletHolder(new MetricsServlet), pathSpec)
    handlers.addHandler(context)
  }

  private def isRunningAws = System.getProperty("uberjar", "false").equals("true")
}

object TestConfig {
  val overrides = Map("db.name" -> "koskitest", "fixtures.use" -> "true", "authenticationFailed.initialDelay" -> "1s", "authenticationFailed.resetAfter" -> "1s", "mockoidgenerator" -> "true")
}

object SharedJetty extends JettyLauncher(PortChecker.findFreeLocalPort, TestConfig.overrides)

class ManagedQueuedThreadPool(maxThreads: Int, minThreads: Int) extends QueuedThreadPool(maxThreads, minThreads) with QueuedThreadPoolMXBean

trait QueuedThreadPoolMXBean {
  def getQueueSize: Int
  def getBusyThreads: Int
  def getMinThreads: Int
  def getMaxThreads: Int
}