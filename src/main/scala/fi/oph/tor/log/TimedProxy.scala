package fi.oph.tor.log

import fi.oph.tor.util.{Proxy, Timing}
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

object TimedProxy {
  def apply[S <: AnyRef](service: S, thresholdMs: Int = 5)(implicit tag: ClassTag[S]) = {
    val logger = LoggerFactory.getLogger(service.getClass)

    Proxy.createProxy[S](service, { invocation =>
      Timing.timed(invocation.toString, thresholdMs, logger) {invocation.invoke}
    })
  }
}
