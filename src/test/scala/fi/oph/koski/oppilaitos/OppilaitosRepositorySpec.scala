package fi.oph.koski.oppilaitos

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.config.KoskiApplication
import org.scalatest.{FreeSpec, Matchers}

class OppilaitosRepositorySpec extends FreeSpec with Matchers {
  "Oppilaitoshaku oppilaitosnumerolla" in {
    KoskiApplicationForTests.oppilaitosRepository.findByOppilaitosnumero("01901").flatMap(_.nimi.map(_.get("fi"))) should equal(Some("Helsingin yliopisto"))
  }
}
