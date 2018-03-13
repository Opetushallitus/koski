package fi.oph.koski.koskiuser

import java.net.InetAddress.{getByName => inetAddress}

import fi.oph.koski.log.LogUserContext
import org.scalatest.{FreeSpec, Matchers}

class KoskiSessionSpec extends FreeSpec with Matchers {
  "Client ip" - {
    "clientIp contains single address" in {
      mkSession("127.0.0.1").clientIp should be(inetAddress("127.0.0.1"))
    }

    "clientIp contains multiple addresses" in {
      mkSession("127.0.0.1, 192.168.0.1").clientIp should be(inetAddress("127.0.0.1"))
      mkSession("127.0.0.1,192.168.0.1").clientIp should be(inetAddress("127.0.0.1"))
      mkSession(" 127.0.0.1    ,  192.168.0.1  ").clientIp should be(inetAddress("127.0.0.1"))
    }
  }

  def mkSession(ipStr: String) = {
    val ip = LogUserContext.toInetAddress(ipStr)
    new KoskiSession(AuthenticationUser("", "", "", None), "fi", ip, "", Set())
  }
}
