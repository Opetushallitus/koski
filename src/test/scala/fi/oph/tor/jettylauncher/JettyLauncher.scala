package fi.oph.tor.jettylauncher

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends App {
  new JettyLauncher(System.getProperty("tor.port","7021").toInt).start.join
}

class JettyLauncher(val port: Int, profile: Option[String] = None) {
  val server = new Server(port)
  val context = new WebAppContext()
  context.setResourceBase("src/main/webapp")
  context.setContextPath("/tor")
  context.setDescriptor("src/main/webapp/WEB-INF/web.xml")
  profile.foreach (context.setAttribute("tor.profile", _))
  server.setHandler(context)

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