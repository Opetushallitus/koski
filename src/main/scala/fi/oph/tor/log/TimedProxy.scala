package fi.oph.tor.log

import fi.oph.tor.util.Proxy
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.ClassTag

object TimedProxy {
  def apply[S <: AnyRef](service: S, thresholdMs: Int = 5)(implicit tag: ClassTag[S]) = {
    val logger = LoggerFactory.getLogger(service.getClass)

    Proxy.createProxy[S](service, { invocation =>
      timed(invocation.toString, thresholdMs, logger) {invocation.invoke}
    })
  }

  private def timed[R](blockname: => String = "", thresholdMs: Int, logger: Logger)(block: => R): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    val time: Long = (t1 - t0) / 1000000
    if (time >= thresholdMs) logger.info(blockname + " call took: " + time + " ms")
    result
  }
}
