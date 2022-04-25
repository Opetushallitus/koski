package fi.oph.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.jettylauncher.JettyLauncher
import fi.oph.koski.log.Logging

trait LocalJettyHttpSpec extends HttpSpecification {
  LocalJettyHttpSpec.setup(defaultKoskiApplication)

  override def baseUrl: String = LocalJettyHttpSpec.baseUrl
  protected def defaultKoskiApplication: KoskiApplication = KoskiApplicationForTests
}

object LocalJettyHttpSpec extends Logging {
  private def externalJettyPort = Option(System.getProperty("test.externalJettyPort")).map(_.toInt)
  private var defaultKoskiApplication: KoskiApplication = KoskiApplicationForTests

  private lazy val jetty: JettyLauncher = externalJettyPort match {
    case None =>
      val sharedJetty = new SharedJetty(defaultKoskiApplication)
      sharedJetty.start()
      sharedJetty
    case Some(port) =>
      logger.info(s"Using external jetty on port $port")
      new JettyLauncher(port, defaultKoskiApplication)
  }

  var running = false

  def setup(koskiApplication: KoskiApplication): Unit = synchronized {
    if (!running) {
      running = true
      defaultKoskiApplication = koskiApplication
      jetty // Evaluate to start jetty
    }
  }

  def baseUrl: String = jetty.baseUrl
}
