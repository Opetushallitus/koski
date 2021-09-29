package fi.oph.koski.oppilaitos

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OppilaitosRepositorySpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Oppilaitoshaku oppilaitosnumerolla" in {
    KoskiApplicationForTests.oppilaitosRepository
      .findByOppilaitosnumero("01901")
      .flatMap(_.nimi.map(_.get("fi"))) should equal(Some("Helsingin yliopisto"))
  }
}
