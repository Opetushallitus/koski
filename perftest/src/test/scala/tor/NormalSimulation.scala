package tor

import io.gatling.core.Predef._
import tor.Scenarios._

import scala.concurrent.duration._

class NormalSimulation extends TorSimulation {
  setUp(
    prepareForFind.inject(atOnceUsers(1)),
    prepareForUpdateOppija.inject(atOnceUsers(1)),
    prepareForQuery.inject(atOnceUsers(1)),


    findOppija.inject(nothingFor(50 seconds), constantUsersPerSec(1) during(1 minute) randomized),
    updateOppija.inject(nothingFor(60 seconds), constantUsersPerSec(1) during(1 minute) randomized),
    queryOppijat.inject(nothingFor(70 seconds), constantUsersPerSec(0.1) during(1 minute) randomized)
  )
}


