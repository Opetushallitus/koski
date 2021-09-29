package fi.oph.koski

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.jettylauncher.JettyLauncher
import fi.oph.koski.log.{AccessLogTester, AuditLogTester, Logging, RootLogTester}

trait LocalJettyHttpSpec extends HttpSpecification {
  LocalJettyHttpSpec.setup()

  override def baseUrl: String = LocalJettyHttpSpec.baseUrl
}

object LocalJettyHttpSpec extends Logging {
  private def externalJettyPort = Option(System.getProperty("test.externalJettyPort")).map(_.toInt)

  private lazy val jetty: JettyLauncher = externalJettyPort match {
    case None =>
      SharedJetty.start()
      SharedJetty
    case Some(port) =>
      logger.info(s"Using external jetty on port $port")
      new JettyLauncher(port, KoskiApplicationForTests)
  }

  var running = false

  def setup(): Unit = synchronized {
    if (!running) {
      running = true
      jetty // Evaluate to start jetty
      AuditLogTester.setup
      AccessLogTester.setup
      RootLogTester.setup
    }
  }

  def baseUrl: String = jetty.baseUrl
}
