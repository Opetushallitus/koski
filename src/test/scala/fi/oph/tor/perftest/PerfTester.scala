package fi.oph.tor.perftest

import fi.vm.sade.utils.Timer
import fi.vm.sade.utils.http.DefaultHttpClient

object PerfTester extends App {
  val count = 1000
  Timer.timed("Haettu " + count + " suoritusta") {
    for (i <- 1 to count) {
      fetchSuoritukset("oppija-" + (100000 + i))
    }
  }

  def fetchSuoritukset(oppijaId: String): Unit = {
    DefaultHttpClient.httpGet("http://localhost:7021/tor/suoritus/?oppijaId=" + oppijaId + "&status=suoritettu").responseWithHeaders match {
      case (200, _, content) =>
      case (errorCode, _, content) =>
        throw new Exception("Error " + errorCode)
    }
  }
}
