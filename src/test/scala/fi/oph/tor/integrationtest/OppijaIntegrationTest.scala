package fi.oph.tor.integrationtest

import fi.oph.tor.api.{HttpSpecification, OpiskeluOikeusTestMethods}
import org.scalatest.{Tag, Matchers, FreeSpec}


object TorDevEnvironment extends Tag("tordev")

class OppijaIntegrationTest extends FreeSpec with Matchers with HttpSpecification {
  "Oppijan henkilötiedot" taggedAs(TorDevEnvironment) in {
    throw new IllegalArgumentException("BOOM")
  }
}
