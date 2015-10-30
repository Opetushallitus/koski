package fi.oph.tor.util

import fi.vm.sade.utils.slf4j.Logging
import org.slf4j.{Logger, LoggerFactory}
import scala.reflect.ClassTag

trait Timed extends Logging {
  def timed[R](blockname: => String = "", thresholdMs: Int = 10, logger: Logger = logger)(block: => R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    val time: Long = (t1 - t0) / 1000000
    if (time >= thresholdMs) logger.info(blockname + " call took: " + time + " ms")
    result
  }

}

object Timed extends Timed {
  def timedProxy[S <: AnyRef](service: S, thresholdMs: Int = 50)(implicit tag: ClassTag[S]) = {
    val logger = LoggerFactory.getLogger(service.getClass)

    Proxy.createProxy[S](service, { case (invocation, defaultHandler) =>
      timed(invocation.toString, thresholdMs, logger){defaultHandler(invocation)}
    })
  }
}
