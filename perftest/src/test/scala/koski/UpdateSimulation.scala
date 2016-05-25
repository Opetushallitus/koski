package koski

import io.gatling.core.Predef._
import koski.Scenarios._

import scala.concurrent.duration._

class UpdateSimulation extends KoskiSimulation {
  setUp(
    prepareForUpdateOppija.inject(atOnceUsers(1)),
    updateOppija.inject(nothingFor(60 seconds), constantUsersPerSec(10) during(10 seconds) randomized)
  )
}


