package fi.oph.tor.log

import fi.oph.tor.util.{Proxy, Timing}
import org.log4s._
import scala.reflect.ClassTag

object TimedProxy {
  def apply[S <: AnyRef](service: S, thresholdMs: Int = 5)(implicit tag: ClassTag[S]) = {
    val logger = getLogger(service.getClass)

    Proxy.createProxy[S](service, { invocation =>
      Timing.timed(invocation.toString, thresholdMs, logger) {invocation.invoke}
    })
  }
}
