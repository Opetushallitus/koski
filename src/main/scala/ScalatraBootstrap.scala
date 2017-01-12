import javax.servlet.ServletContext

import fi.oph.koski.config.{KoskiApplication, KoskiWebApplication}
import fi.oph.koski.db._
import fi.oph.koski.log.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext {
  override def init(context: ServletContext) {
    val application = Option(context.getAttribute("koski.application").asInstanceOf[KoskiWebApplication]).getOrElse(new KoskiWebApplication(KoskiApplication.apply))

    application.mountTo(context)
  }
}
