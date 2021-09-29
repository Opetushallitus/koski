package fi.oph.koski.oppilaitos

import fi.oph.koski.KoskiApplicationForTests
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OppilaitosRepositorySpec extends AnyFreeSpec with Matchers {
  "Oppilaitoshaku oppilaitosnumerolla" in {
    KoskiApplicationForTests.oppilaitosRepository
      .findByOppilaitosnumero("01901")
      .flatMap(_.nimi.map(_.get("fi"))) should equal(Some("Helsingin yliopisto"))
  }
}
