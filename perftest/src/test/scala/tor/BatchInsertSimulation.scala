package tor

import io.gatling.core.Predef._
import tor.Scenarios._

import scala.concurrent.duration._

class BatchInsertSimulation extends TorSimulation {
  setUp(
    prepareForInsertOppija.inject(atOnceUsers(1)),
    batchInsertOppija.inject(nothingFor(10 seconds), constantUsersPerSec(1) during(10 seconds) randomized)
  )
}
