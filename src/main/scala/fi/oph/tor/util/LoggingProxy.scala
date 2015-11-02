package fi.oph.tor.util

import org.slf4j.LoggerFactory
import scala.reflect.ClassTag

object LoggingProxy {
  def apply[T <: AnyRef](target: T)(implicit tag: ClassTag[T]): T = {
    val logger = LoggerFactory.getLogger(target.getClass)

    Proxy.createProxy[T](target, { case (invocation, defaultHandler) =>
      logger.info(invocation.toString)
      val result: AnyRef = defaultHandler(invocation)
      logger.info("==> " + result)
      result
    })
  }
}