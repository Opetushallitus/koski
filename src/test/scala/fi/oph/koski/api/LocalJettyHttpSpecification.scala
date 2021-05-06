package fi.oph.koski.api

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.jettylauncher.JettyLauncher
import fi.oph.koski.log.{AccessLogTester, AuditLogTester, Logging, RootLogTester}
import fi.oph.koski.util.PortChecker

trait LocalJettyHttpSpecification extends HttpSpecification {
  LocalJettyHttpSpec.setup()

  override def baseUrl: String = LocalJettyHttpSpec.baseUrl
}

object SharedJetty extends JettyLauncher(PortChecker.findFreeLocalPort, KoskiApplicationForTests)

object LocalJettyHttpSpec extends Logging {
  private lazy val myJetty = externalJettyPort match {
    case None => SharedJetty
    case Some(port) =>
      logger.info(s"Using external jetty on port $port")
      new JettyLauncher(port, KoskiApplicationForTests)
  }

  var running = false

  def setup(): Unit = synchronized {
    if (!running) {
      running = true
      if (externalJettyPort.isEmpty) SharedJetty.start
      AuditLogTester.setup
      AccessLogTester.setup
      RootLogTester.setup
    }
  }

  def baseUrl = myJetty.baseUrl

  def externalJettyPort = Option(System.getProperty("test.externalJettyPort")).map(_.toInt)
  def portForSharedJetty = externalJettyPort.getOrElse(PortChecker.findFreeLocalPort)
}
