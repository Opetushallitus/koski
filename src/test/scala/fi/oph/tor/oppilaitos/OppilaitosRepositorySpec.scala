package fi.oph.tor.oppilaitos

import fi.oph.tor.config.TorApplication
import org.scalatest.{FreeSpec, Matchers}

class OppilaitosRepositorySpec extends FreeSpec with Matchers {
  "Oppilaitoshaku oppilaitosnumerolla" in {
    TorApplication().oppilaitosRepository.findByOppilaitosnumero("01901").flatMap(_.nimi.map(_.get("fi"))) should equal(Some("Helsingin yliopisto"))
  }
}
