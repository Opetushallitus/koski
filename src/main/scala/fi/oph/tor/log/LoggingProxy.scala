package fi.oph.tor.log

import fi.oph.tor.util.Proxy
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

object LoggingProxy {
  def apply[T <: AnyRef](target: T)(implicit tag: ClassTag[T]): T = {
    val logger = LoggerFactory.getLogger(target.getClass)

    Proxy.createProxy[T](target, { invocation =>
      logger.info(invocation.toString)
      val result: AnyRef = invocation.invoke
      logger.info("==> " + result)
      result
    })
  }
}