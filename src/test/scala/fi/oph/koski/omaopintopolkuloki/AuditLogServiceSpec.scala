package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.{Koulutustoimija, Oppija, Oppilaitos}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class AuditLogServiceSpec extends AnyFreeSpec with Matchers {
  "allowedOrganisaatiot" - {
    "kerää koulutustoimija- ja oppilaitos-oidit opiskeluoikeuksista" in {
      val oo1 = AmmatillinenExampleData.opiskeluoikeus().copy(
        oppilaitos = Some(Oppilaitos("1.2.246.562.10.111")),
        koulutustoimija = Some(Koulutustoimija("1.2.246.562.10.222"))
      )
      val oo2 = AmmatillinenExampleData.opiskeluoikeus().copy(
        oppilaitos = Some(Oppilaitos("1.2.246.562.10.333")),
        koulutustoimija = Some(Koulutustoimija("1.2.246.562.10.222"))
      )
      val oppija = Oppija(
        KoskiSpecificMockOppijat.amis.toHenkilötiedotJaOid,
        List(oo1, oo2)
      )

      val result = AuditLogService.allowedOrganisaatiot(oppija)
      result.koulutustoimijat should equal(Set("1.2.246.562.10.222"))
      result.oppilaitokset should equal(Set("1.2.246.562.10.111", "1.2.246.562.10.333"))
    }

    "sivuuttaa puuttuvat koulutustoimija- ja oppilaitos-viitteet" in {
      val oo = AmmatillinenExampleData.opiskeluoikeus().copy(
        oppilaitos = Some(Oppilaitos("1.2.246.562.10.444")),
        koulutustoimija = None
      )
      val oppija = Oppija(
        KoskiSpecificMockOppijat.amis.toHenkilötiedotJaOid,
        List(oo)
      )

      val result = AuditLogService.allowedOrganisaatiot(oppija)
      result.koulutustoimijat shouldBe empty
      result.oppilaitokset should equal(Set("1.2.246.562.10.444"))
    }

    "palauttaa tyhjän joukon kun opiskeluoikeuksia ei ole" in {
      val oppija = Oppija(
        KoskiSpecificMockOppijat.amis.toHenkilötiedotJaOid,
        List.empty
      )

      val result = AuditLogService.allowedOrganisaatiot(oppija)
      result.isEmpty shouldBe true
    }
  }

  "AllowedOrganisaatiot.priority" - {
    val allowed = AllowedOrganisaatiot(
      koulutustoimijat = Set("1.2.246.562.10.222"),
      oppilaitokset = Set("1.2.246.562.10.111")
    )

    "OPH saa prioriteetin 0" in {
      allowed.priority(Opetushallitus.organisaatioOid) shouldBe 0
    }
    "koulutustoimija saa prioriteetin 1" in {
      allowed.priority("1.2.246.562.10.222") shouldBe 1
    }
    "oppilaitos saa prioriteetin 2" in {
      allowed.priority("1.2.246.562.10.111") shouldBe 2
    }
    "tuntematon organisaatio saa prioriteetin 3" in {
      allowed.priority("1.2.246.562.10.999") shouldBe 3
    }
  }
}
