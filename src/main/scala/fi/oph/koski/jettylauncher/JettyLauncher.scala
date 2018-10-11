package fi.oph.koski.jettylauncher

import java.lang.management.ManagementFactory
import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import fi.oph.koski.cache.JMXCacheManager
import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.executors.Pools
import fi.oph.koski.log.{LogConfiguration, Logging, MaskedSlf4jRequestLog}
import io.prometheus.client.exporter.MetricsServlet
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.jmx.MBeanContainer
import org.eclipse.jetty.proxy.ProxyServlet
import org.eclipse.jetty.server.handler.{HandlerCollection, StatisticsHandler}
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.ssl.SslContextFactory

object JettyLauncher extends App with Logging {
  lazy val globalPort = System.getProperty("koski.port","7021").toInt
  try {
    val application = new KoskiApplication(KoskiApplication.defaultConfig, new JMXCacheManager)
    new JettyLauncher(globalPort, application).start.join
  } catch {
    case e: Throwable =>
      logger.error(e)("Error in server startup")
      System.exit(1)
  }
}

class JettyLauncher(val port: Int, val application: KoskiApplication) extends Logging {
  private val config = application.config

  private val threadPool = new ManagedQueuedThreadPool(Pools.jettyThreads, 10)

  private val server = new Server(threadPool)

  application.masterDatabase // <- force evaluation to make sure DB is up

  configureLogging
  setupConnector

  private val handlers = new HandlerCollection()
  private val rootContext = new ServletContextHandler()

  server.setHandler(handlers)

  setupKoskiApplicationContext
  setupGzipForStaticResources
  setupJMX
  setupPrometheusMetrics
  if (Environment.isLocalDevelopmentEnvironment && config.hasPath("oppijaRaamitProxy")) {
    setupOppijaRaamitProxy
  }
  if (Environment.isLocalDevelopmentEnvironment && config.hasPath("virkailijaRaamitProxy")) {
    setupVirkailijaRaamitProxy
  }
  handlers.addHandler(rootContext)

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
    val requestLog = new MaskedSlf4jRequestLog()
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
    if (Environment.isLocalDevelopmentEnvironment) {
      // Avoid random SIGBUS errors when static files memory-mapped by Jetty (and being sent to client)
      // are modified (by "make watch"). Can be reproduced somewhat reliably with Java 8 by editing
      // a .less file and quickly doing a reload in the browser.
      context.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false")
    }
    handlers.addHandler(context)
  }

  private def resourceBase = if (isRunningAws) {
    Resource.setDefaultUseCaches(false) // avoid "zip file is closed" exceptions in some situations (exact cause unknown)
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
    val pathSpec = if (isRunningAws) "/koski-metrics" else "/metrics"
    rootContext.addServlet(new ServletHolder(new MetricsServlet), pathSpec)
  }

  private def setupOppijaRaamitProxy = {
    val holder = rootContext.addServlet(classOf[HttpsSupportingTransparentProxyServlet], "/oppija-raamit/*")
    holder.setInitParameter("proxyTo", config.getString("oppijaRaamitProxy"))
    holder.setInitParameter("prefix", "/oppija-raamit")
  }

  private def setupVirkailijaRaamitProxy = {
    val holder = rootContext.addServlet(classOf[HttpsSupportingTransparentProxyServlet], "/virkailija-raamit/*")
    holder.setInitParameter("proxyTo", config.getString("virkailijaRaamitProxy"))
    holder.setInitParameter("prefix", "/virkailija-raamit")
  }

  private def isRunningAws = System.getProperty("uberjar", "false").equals("true")
}

object TestConfig {
  val overrides = ConfigFactory.parseString(
    """
      |db.name = koskitest
      |fixtures.use = true
      |authenticationFailed.initialDelay = 1s
      |authenticationFailed.resetAfter = 1s
      |mockoidgenerator = true
      |oppijavuosiraportti.enabledForUsers = ["kalle", "omnia-tallentaja", "evira"]
      |features.luovutuspalvelu = true
    """.stripMargin)
}

class ManagedQueuedThreadPool(maxThreads: Int, minThreads: Int) extends QueuedThreadPool(maxThreads, minThreads) with QueuedThreadPoolMXBean

trait QueuedThreadPoolMXBean {
  def getQueueSize: Int
  def getBusyThreads: Int
  def getMinThreads: Int
  def getMaxThreads: Int
}

class HttpsSupportingTransparentProxyServlet extends ProxyServlet.Transparent {
  override protected def newHttpClient() = {
    new HttpClient(new SslContextFactory())
  }
}
