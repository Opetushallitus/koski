package fi.oph.koski.koskiuser

import jakarta.servlet.http.HttpServletRequest
import org.mockito.Mockito.{mock, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.util.Collections

class LuovutuspalveluClientIpSpec extends AnyFreeSpec with Matchers {
  "LuovutuspalveluClientIp" - {
    "käyttää X-Forwarded-For-headerin ainoaa arvoa" in {
      LuovutuspalveluClientIp(request("1.1.1.1")) should equal(Some("1.1.1.1"))
    }

    "käyttää pilkuilla erotellun listan oikeanpuoleisinta arvoa" in {
      LuovutuspalveluClientIp(request("0.0.0.0, 1.1.1.1 ,2.2.2.2")) should equal(Some("2.2.2.2"))
    }

    "käyttää useasta headeririvistä viimeisen rivin oikeanpuoleisinta arvoa" in {
      LuovutuspalveluClientIp(request("0.0.0.0", "1.1.1.1, 2.2.2.2")) should equal(Some("2.2.2.2"))
    }

    "ei ohita tyhjää viimeistä arvoa" in {
      LuovutuspalveluClientIp(request("1.1.1.1,")) should equal(Some(""))
    }

    "ei käytä yhteyden osoitetta, jos X-Forwarded-For-headeria ei ole" in {
      LuovutuspalveluClientIp(request()) should equal(None)
    }
  }

  private def request(xForwardedFor: String*): HttpServletRequest = {
    val req = mock(classOf[HttpServletRequest])
    when(req.getHeaders("X-Forwarded-For")).thenReturn(Collections.enumeration(java.util.List.of(xForwardedFor: _*)))
    when(req.getRemoteAddr).thenReturn("9.9.9.9")
    req
  }
}
