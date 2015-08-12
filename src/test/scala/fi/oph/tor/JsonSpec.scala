package fi.oph.tor

import fi.oph.tor.fixture.SuoritusTestData
import fi.oph.tor.json.Json
import org.scalatest.{FlatSpec, Matchers}

class JsonSpec extends FlatSpec with Matchers {
  "Tutkintosuoritus" should "map into nice JSON" in {
    Json.write(SuoritusTestData.tutkintosuoritus1)
  }
}
