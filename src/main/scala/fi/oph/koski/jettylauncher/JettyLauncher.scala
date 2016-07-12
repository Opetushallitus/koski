package fi.oph.koski.jettylauncher

import java.nio.file.{Files, Paths}

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiDatabaseConfig
import fi.oph.koski.log.LogConfiguration
import fi.oph.koski.util.{Pools, PortChecker}
import org.eclipse.jetty.server.session.{SessionHandler, JDBCSessionManager, JDBCSessionIdManager}
import org.eclipse.jetty.server.{Server, ServerConnector, Slf4jRequestLog}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends App {
  lazy val globalPort = System.getProperty("tor.port","7021").toInt
  new JettyLauncher(globalPort).start.join
}

class JettyLauncher(val port: Int, overrides: Map[String, String] = Map.empty) {
  val config = overrides.toList.foldLeft(KoskiApplication.defaultConfig)({ case (config, (key, value)) => config.withValue(key, fromAnyRef(value)) })
  LogConfiguration.configureLoggingWithFileWatch

  lazy val threadPool = new QueuedThreadPool(Pools.jettyThreads, 10);
  lazy val server = new Server(threadPool)
  lazy val requestLog = new Slf4jRequestLog()


  requestLog.setLogLatency(true)
  server.setRequestLog(requestLog);

  lazy val connector: ServerConnector = new ServerConnector(server)
  connector.setPort(port)
  server.addConnector(connector)

  def resourceBase = System.getProperty("resourcebase", "./target/webapp")

  if(!Files.exists(Paths.get(resourceBase))) {
    throw new RuntimeException("WebApplication resource base: " + resourceBase + " does not exist.")
  }

  val sessionPersistence = SessionPersistence(config, server)

  val context = sessionPersistence.configure(new WebAppContext())

  context.setParentLoaderPriority(true)
  context.setContextPath("/koski")
  context.setResourceBase(resourceBase)
  context.setAttribute("koski.config", config)

  server.setHandler(context)

  def start = {
    server.start
    server
  }

  def baseUrl = "http://localhost:" + port + "/koski"
}

case class SessionPersistence(config: Config, server: Server) {
  val idMgr = new JDBCSessionIdManager(server);
  idMgr.setWorkerName("node1") // TODO
  private val dbConfig: KoskiDatabaseConfig = KoskiDatabaseConfig(config)
  idMgr.setDriverInfo(dbConfig.jdbcDriverClassName, dbConfig.jdbcUrl)
  idMgr.setScavengeInterval(600)
  server.setSessionIdManager(idMgr)

  def configure(context: WebAppContext) = {
    val jdbcMgr = new JDBCSessionManager();
    jdbcMgr.setSessionIdManager(server.getSessionIdManager());
    val sessionHandler = new SessionHandler(jdbcMgr);
    context.setSessionHandler(sessionHandler);
    context
  }
}

object SharedJetty extends JettyLauncher(PortChecker.findFreeLocalPort, Map("db.name" -> "tortest", "fixtures.use" -> "true"))