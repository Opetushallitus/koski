package tor

import io.gatling.core.Predef._
import tor.Scenarios._

import scala.concurrent.duration._


class ValidateOppijaSimulation extends TorSimulation {
  setUp(
    validateOppija.inject(atOnceUsers(1))
  )
}


