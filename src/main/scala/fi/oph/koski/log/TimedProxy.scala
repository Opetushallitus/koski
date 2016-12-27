package fi.oph.koski.log

import fi.oph.koski.util.{Proxy, Timing}

import scala.reflect.ClassTag

object TimedProxy {
  def apply[S <: AnyRef](service: S, thresholdMs: Int = 5)(implicit tag: ClassTag[S]) = {
    Proxy.createProxy[S](service, { invocation =>
      Timing.timed(invocation.f.name, thresholdMs, service.getClass) {invocation.invoke}
    })
  }
}

