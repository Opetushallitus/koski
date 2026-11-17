package fi.oph.koski.koskiuser

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.organisaatio.MockOrganisaatiot.{helsinginKaupunki, tornionKaupunki}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SessionRooliSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

  "hasRoleInOrganisaatio" - {
    "true, kun rooli on annettu kysytyssä organisaatiossa" in {
      session(MockUsers.helsinkiAiemmatOpiskeluoikeudet).hasRoleInOrganisaatio(Rooli.AIEMMAT_OPISKELUOIKEUDET, helsinginKaupunki) shouldBe true
    }
    "false, kun rooli on vain toisessa organisaatiossa, vaikka hasRole olisi true" in {
      val s = session(MockUsers.tornioAiemmatOpiskeluoikeudetHelsinkiKatselija)
      s.hasRole(Rooli.AIEMMAT_OPISKELUOIKEUDET) shouldBe true
      s.hasRoleInOrganisaatio(Rooli.AIEMMAT_OPISKELUOIKEUDET, helsinginKaupunki) shouldBe false
      s.hasRoleInOrganisaatio(Rooli.AIEMMAT_OPISKELUOIKEUDET, tornionKaupunki) shouldBe true
    }
    "false, kun roolia ei ole lainkaan" in {
      session(MockUsers.helsinkiKatselija).hasRoleInOrganisaatio(Rooli.AIEMMAT_OPISKELUOIKEUDET, helsinginKaupunki) shouldBe false
    }
  }
}
