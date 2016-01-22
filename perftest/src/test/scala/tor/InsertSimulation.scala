package tor

import io.gatling.core.Predef._
import tor.Scenarios._

import scala.concurrent.duration._

class InsertSimulation extends TorSimulation {
  setUp(
    prepareForInsertOppija.inject(atOnceUsers(1)),
    insertOppija.inject(nothingFor(10 seconds), constantUsersPerSec(10) during(10 seconds) randomized)
  )
}
