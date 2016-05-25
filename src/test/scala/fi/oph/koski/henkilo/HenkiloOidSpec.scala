package fi.oph.koski.henkilo

import org.scalatest.{Matchers, FreeSpec}

class HenkiloOidSpec extends FreeSpec with Matchers {
  "Henkilö oid -formaatti" - {
    "1.2.246.562.24.12345678901 OK" in {
      HenkiloOid.isValidHenkilöOid("1.2.246.562.24.12345678901") should equal(true)
    }
    "1.2.246.562.24.x2345678901 NOT OK" in {
      HenkiloOid.isValidHenkilöOid("1.2.246.562.24.x2345678901") should equal(false)
    }
  }
}
