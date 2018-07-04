package fi.oph.koski.henkilo

import fi.oph.koski.schema.Henkilö
import org.scalatest.{FreeSpec, Matchers}

class HenkiloOidSpec extends FreeSpec with Matchers {
  "Henkilö oid -formaatti" - {
    "1.2.246.562.24.12345678901 OK" in {
      Henkilö.isValidHenkilöOid("1.2.246.562.24.12345678901") should equal(true)
    }
    "1.2.246.562.24.x2345678901 NOT OK" in {
      Henkilö.isValidHenkilöOid("1.2.246.562.24.x2345678901") should equal(false)
    }
  }
}
