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

  def findFreeLocalPort: Int = {
    val range = 1024 to 60000
    val port = ((range(new Random().nextInt(range length))))
    if (isFreeLocalPort(port)) {
      port
    } else {
      findFreeLocalPort
    }
  }
}