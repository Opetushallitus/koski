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

  def waitUntilFreeLocalPort(port: Int) = Wait.until(isFreeLocalPort(port))

  def waitUntilReservedLocalPort(port: Int) = Wait.until(!isFreeLocalPort(port))

  def findFreeLocalPort: Int = {
    // Browserstack doesn't support all ports on Safari, see https://www.browserstack.com/question/664
    val range = 9200 to 9400
    val port = ((range(new Random().nextInt(range length))))
    if (isFreeLocalPort(port)) {
      port
    } else {
      findFreeLocalPort
    }
  }
}
