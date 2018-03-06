package fi.oph.koski.koskiuser

import org.scalatest.{FreeSpec, Matchers}

class KoskiSessionSpec extends FreeSpec with Matchers {
  "Client ip" - {
    "clientIp contains single address" in {
      mkSession("127.0.0.1").firstClientIp should be("127.0.0.1")
    }

    "clientIp contains multiple addresses" in {
      mkSession("127.0.0.1, 192.168.0.1").firstClientIp should be("127.0.0.1")
      mkSession("127.0.0.1,192.168.0.1").firstClientIp should be("127.0.0.1")
      mkSession(" 127.0.0.1    ,  192.168.0.1  ").firstClientIp should be("127.0.0.1")
    }

    "clientIp is empty" in {
      mkSession("").firstClientIp should be("")
    }
  }

  def mkSession(ip: String) = new KoskiSession(AuthenticationUser("", "", "", None), "fi", ip, "", Set())
}
