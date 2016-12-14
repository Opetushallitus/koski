package fi.oph.koski.util
import java.io.IOException
import java.net.Socket

import scala.util.Random

object PortChecker {
  def isFreeLocalPort(port: Int): Boolean = {
    try {
      val socket = new Socket("127.0.0.1", port)
      socket.close()
      false
    } catch {
      case e:IOException => true
    }
  }

  def waitUntilFreeLocalPort(port: Int) = waitUntil(isFreeLocalPort(port))

  def waitUntilReservedLocalPort(port: Int) = waitUntil(!isFreeLocalPort(port))

  def findFreeLocalPort: Int = {
    val range = 1024 to 60000
    val port = ((range(new Random().nextInt(range length))))
    if (isFreeLocalPort(port)) {
      port
    } else {
      findFreeLocalPort
    }
  }

  def waitUntil(predicate: => Boolean, timeoutMs: Long = 60000): Boolean = {
    val started = System.currentTimeMillis()
    val timeoutAt = started + timeoutMs
    while (!predicate && (System.currentTimeMillis <= timeoutAt)) {
      Thread.sleep(100)
    }
    return predicate
  }
}