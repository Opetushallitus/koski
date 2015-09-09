package fi.oph.tor.jettylauncher

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.{HandlerList, ContextHandlerCollection, ResourceHandler}
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends App {
  new JettyLauncher(System.getProperty("tor.port","7021").toInt).start.join
}

class JettyLauncher(val port: Int, profile: Option[String] = None) {
  val server = new Server(port)

  val context = new WebAppContext()
  context.setResourceBase("src/main/webapp")
  context.setDescriptor("src/main/webapp/WEB-INF/web.xml")

  val all = new HandlerList
  all.setHandlers(List(
    staticResources("./web/static"),
    staticResources("./web/dist"),
    context).toArray)

  server.setHandler(all)

  def staticResources(path: String) = {
    val staticResources = new ResourceHandler()
    staticResources.setResourceBase(path)
    staticResources
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
}