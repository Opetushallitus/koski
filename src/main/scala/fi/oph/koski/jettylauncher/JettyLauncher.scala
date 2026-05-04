package fi.oph.koski.jettylauncher

import java.lang.management.ManagementFactory
import java.nio.file.{Files, Paths}
import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.cache.JMXCacheManager
import fi.oph.koski.config.{AppConfig, Environment, KoskiApplication}
import fi.oph.koski.executors.Pools
import fi.oph.koski.log.{LogConfiguration, Logging, MaskedSlf4jRequestLogWriter}
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet
import org.eclipse.jetty.jmx.MBeanContainer
import org.eclipse.jetty.http.UriCompliance
import org.eclipse.jetty.compression.gzip.GzipCompression
import org.eclipse.jetty.compression.server.{CompressionConfig, CompressionHandler}
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.StatisticsHandler
import org.eclipse.jetty.ee10.servlet.{ResourceServlet, ServletContextHandler, ServletHolder, ServletMapping}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.ee10.webapp.WebAppContext

object JettyLauncher extends App with Logging {
  LogConfiguration.configureLogging()
  verifyTimezone()

  private val globalPort = System.getProperty("koski.port", "7021").toInt

  private val config: Config = if (Environment.usesAwsAppConfig) {
    logger.info("Ladataan konfiguraatio...")
    try {
      AppConfig
        .loadConfig
        .map(ConfigFactory.load)
        .getOrElse {
          throw new RuntimeException("Konfiguraatiota ei saatu ladattua")
        }
    } catch {
      case e: Any => {
        logger.error(s"Konfiguraation lataus epäonnistui: ${e.getMessage}")
        throw e
      }
    }
  } else {
    ConfigFactory.load
  }

  logger.info(s"Starting koski in ${Environment.currentEnvironment(config)}")

  try {
    val application = new KoskiApplication(config, new JMXCacheManager)
    new JettyLauncher(globalPort, application).start().join()
  } catch {
    case e: Throwable =>
      logger.error(e)("Error in server startup")
      System.exit(1)
  }

  private def verifyTimezone(): Unit = {
    val tz = java.util.TimeZone.getDefault
    if (tz.getID != "Europe/Helsinki") {
      throw new RuntimeException(
        s"JVM timezone is ${tz.getID}, expected Europe/Helsinki. " +
        s"Set -Duser.timezone=Europe/Helsinki in JVM flags."
      )
    }
  }
}

class JettyLauncher(val port: Int, val application: KoskiApplication) extends Logging {
  val hostUrl: String = "http://localhost:" + port

  val baseUrl: String = hostUrl + "/koski"

  private val config = application.config

  private val threadPool = new ManagedQueuedThreadPool(Pools.jettyThreads)

  private val server = new Server(threadPool)

  configureLogging()

  application.masterDatabase // <- force evaluation to make sure DB is up

  setupConnector()

  private val metricsContext = setupPrometheusMetrics()
  private val appContext = setupKoskiApplicationContext()

  server.setHandler(
    setupJMX(
      setupGzipForStaticResources(
        new Handler.Sequence(metricsContext, appContext)
      )
    )
  )

  def start(): Server = {
    server.start()
    logger.info(s"Running in port $port")
    server
  }

  private def setupConnector(): Unit = {
    val httpConfig = new HttpConfiguration()
    httpConfig.addCustomizer( new ForwardedRequestCustomizer() )
    // Allow encoded slashes (%2F) and non-ASCII bytes in path segments. Required by
    // ePerusteet 3-part diaarinumero IDs ("104%2F011%2F2014" = "104/011/2014") and
    // Finnish-character schema class names exposed via /api/types/constraints and
    // /api/editor/preferences. Removing this requires reshaping those routes.
    val uriCompliance = {
      val allowed = new java.util.HashSet(UriCompliance.LEGACY.getAllowed)
      allowed.add(UriCompliance.Violation.ILLEGAL_PATH_CHARACTERS)
      UriCompliance.from(allowed)
    }
    httpConfig.setUriCompliance(uriCompliance)
    val connectionFactory = new HttpConnectionFactory( httpConfig )
    val connector = new ServerConnector(server, connectionFactory)
    connector.setPort(port)
    val idleTimeoutMs = config.getLong("jettyIdleTimeoutSeconds") * 1000
    logger.info(s"Setting Jetty idle connection timeout to $idleTimeoutMs ms")
    connector.setIdleTimeout(idleTimeoutMs)
    server.addConnector(connector)
  }

  private def configureLogging(): Unit = {
    val requestLog = new CustomRequestLog(new MaskedSlf4jRequestLogWriter, CustomRequestLog.NCSA_FORMAT)
    server.setRequestLog(requestLog)
  }

  private def setupKoskiApplicationContext(): WebAppContext = {
    val context = new WebAppContext()
    context.setAttribute("koski.application", application)
    context.setParentLoaderPriority(true)
    context.setContextPath("/")
    context.setBaseResourceAsPath(Paths.get(verifyResourceBase()))
    context.getServletHandler.setDecodeAmbiguousURIs(true)
    context.setDefaultRequestCharacterEncoding("UTF-8")
    context.setDefaultResponseCharacterEncoding("UTF-8")
    context.setAttribute("org.eclipse.jetty.server.Request.queryEncoding", "UTF-8")
    context.setInitParameter("org.eclipse.jetty.ee10.servlet.Default.dirAllowed", "false")
    context.setInitParameter("org.eclipse.jetty.ee10.servlet.Default.etags", "true")

    if (Environment.isLocalDevelopmentEnvironment(config)) {
      // Avoid random SIGBUS errors when static files memory-mapped by Jetty (and being sent to client)
      // are modified (by "make watch"). Can be reproduced somewhat reliably with Java 8 by editing
      // a .less file and quickly doing a reload in the browser.
      context.setInitParameter("org.eclipse.jetty.ee10.servlet.Default.useFileMappedBuffer", "false")
    }

    // Jetty 12 EE10: DefaultServlet cannot be mapped to sub-paths, use ResourceServlet instead
    val staticResourceHolder = new ServletHolder("static-resources", classOf[ResourceServlet])
    staticResourceHolder.setInitParameter("dirAllowed", "false")
    staticResourceHolder.setInitParameter("etags", "true")
    staticResourceHolder.setInitParameter("pathInfoOnly", "false")
    context.getServletHandler.addServlet(staticResourceHolder)
    val staticMapping = new ServletMapping()
    staticMapping.setServletName("static-resources")
    staticMapping.setPathSpecs(Array(
      "/koski/buildversion.txt", "/koski/favicon.ico", "/koski/empty.js",
      "/koski/js/*", "/koski/css/*", "/koski/external_css/*", "/koski/fonts/*",
      "/koski/images/*", "/koski/test/*",
      "/koski/json-schema-viewer/js/*", "/koski/json-schema-viewer/styles/*",
      "/koski/json-schema-viewer/images/*", "/koski/json-schema-viewer/jquery/*",
      "/koski/wsdl/*", "/valpas/assets/*"
    ))
    context.getServletHandler.addServletMapping(staticMapping)

    context
  }

  private def verifyResourceBase(): String = {
    val base = Paths.get(System.getProperty("resourcebase", "./target/webapp")).toAbsolutePath.normalize().toString
    if (!Files.exists(Paths.get(base))) {
      throw new RuntimeException("WebApplication resource base: " + base + " does not exist.")
    }
    base
  }

  private def setupGzipForStaticResources(handler: Handler): CompressionHandler = {
    val compression = new CompressionHandler(handler)
    compression.putCompression(new GzipCompression())
    val config = CompressionConfig.builder().defaults().build()
    Seq(
      "/koski/css/*",
      "/koski/external_css/*",
      "/koski/js/*",
      "/koski/json-schema-viewer/*",
      "/valpas/assets/*",
    ).foreach(path => compression.putConfiguration(path, config))
    compression
  }

  private def setupJMX(handler: Handler): StatisticsHandler = {
    val mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer)
    server.addBean(mbContainer)
    new StatisticsHandler(handler)
  }

  private def setupPrometheusMetrics(): ServletContextHandler = {
    val metricsServletContext = new ServletContextHandler()
    metricsServletContext.setContextPath("/metrics")
    metricsServletContext.addServlet(new ServletHolder(new MetricsServlet), "")
    metricsServletContext
  }
}

class ManagedQueuedThreadPool(maxThreads: Int) extends QueuedThreadPool(maxThreads) with QueuedThreadPoolMXBean

trait QueuedThreadPoolMXBean {
  def getQueueSize: Int
  def getBusyThreads: Int
  def getMinThreads: Int
  def getMaxThreads: Int
}
