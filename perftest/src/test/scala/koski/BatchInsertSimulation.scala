package koski

import io.gatling.core.Predef._
import koski.Scenarios._

import scala.concurrent.duration._

class BatchInsertSimulation extends KoskiSimulation {
  setUp(
    prepareForInsertOppija.inject(atOnceUsers(1)),
    batchInsertOppija.inject(nothingFor(10 seconds), constantUsersPerSec(1) during(10 seconds) randomized)
  )
}
