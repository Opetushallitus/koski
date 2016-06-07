package fi.oph.koski.jettylauncher

import java.nio.file.{Paths, Files}

import fi.oph.koski.jettylauncher.JettyLauncher.staticResourcesRoot
import fi.oph.koski.util.{Pools, PortChecker}
import org.eclipse.jetty.server.handler.{ContextHandler, HandlerList, ResourceHandler}
import org.eclipse.jetty.server.{Server, ServerConnector, Slf4jRequestLog}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends App {
  lazy val globalPort = System.getProperty("tor.port","7021").toInt
  new JettyLauncher(globalPort).start.join

  def staticResourcesRoot = if (Files.exists(Paths.get("./web/dist"))) "./web/dist" else "./web"
}

class JettyLauncher(val port: Int, overrides: Map[String, String] = Map.empty) {
  lazy val threadPool = new QueuedThreadPool(Pools.jettyThreads, 10);
  lazy val server = new Server(threadPool)
  lazy val requestLog = new Slf4jRequestLog()
  requestLog.setLogLatency(true)
  server.setRequestLog(requestLog);
  lazy val connector: ServerConnector = new ServerConnector(server)
  connector.setPort(port)
  server.addConnector(connector)

  val context = new WebAppContext()
  context.setContextPath("/koski")
  context.setResourceBase("src/main/webapp")
  context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
  context.setAttribute("tor.overrides", overrides)

  val all = new HandlerList

  all.setHandlers(List(staticResources("/koski"), context).toArray)

  server.setHandler(all)

  def staticResources(contextPath: String) = {
    val staticResources = new ResourceHandler()
    staticResources.setResourceBase(staticResourcesRoot)
    val contextHandler = new ContextHandler(contextPath)
    contextHandler.setHandler(staticResources)
    contextHandler
  }

  def start = {
    server.start
    server
  }

  def withJetty[T](block: => T) = {
    val server = start
    try {
      block
    } finally {
      server.stop
    }
  }

  def baseUrl = "http://localhost:" + port + "/koski"
}

object SharedJetty extends JettyLauncher(PortChecker.findFreeLocalPort, Map("db.name" -> "tortest", "fixtures.use" -> "true"))