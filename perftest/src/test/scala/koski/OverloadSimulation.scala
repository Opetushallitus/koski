package koski

import io.gatling.core.Predef._
import koski.Scenarios._

import scala.concurrent.duration._

class OverloadSimulation extends KoskiSimulation {
  setUp(
    prepareForFind.inject(atOnceUsers(1)),
    findOppija.inject(nothingFor(60 seconds), constantUsersPerSec(90) during(1 minute) randomized)
  )
}