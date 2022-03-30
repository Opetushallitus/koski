package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.util.WithWarnings
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OpiskeluoikeusRepositorySpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Oppijan haku väliaikaisella henkilötunnuksella" in {
    val oppijaValiaikaisellaHetulla = KoskiSpecificMockOppijat.defaultOppijat.find(_.henkilö.oid == "1.2.246.562.24.20170814999").get.henkilö
    val oppijanOpiskeluoikeudet = KoskiApplicationForTests.opiskeluoikeusRepository
      .findByOppija(oppijaValiaikaisellaHetulla, true, true)(KoskiSpecificSession.systemUser)
    oppijanOpiskeluoikeudet match {
      case WithWarnings(opiskeluoikeudet, warnings) =>
        opiskeluoikeudet should not be empty
        warnings shouldBe empty
    }
  }
}
