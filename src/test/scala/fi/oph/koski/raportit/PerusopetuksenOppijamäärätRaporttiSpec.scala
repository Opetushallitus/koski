package fi.oph.koski.raportit

import java.time.LocalDate.{of => date}
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUser}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PerusopetuksenOppijamäärätRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private val application = KoskiApplicationForTests
  private val raporttiBuilder = PerusopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val raportti = raporttiBuilder
    .build(Seq(jyväskylänNormaalikoulu), date(2012, 1, 1), t)(session(defaultUser))
    .rows.map(_.asInstanceOf[PerusopetuksenOppijamäärätRaporttiRow])

  "Perusopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="perusopetus_vos_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=fi"
            )
          )
        )
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="perusopetus_vos_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=sv"
            )
          )
        )
      }
    }

    "Raportin sarakkeet" in {
      val rows = raportti.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
      rows.length should be(4)
      rows.toList should equal(List(
        PerusopetuksenOppijamäärätRaporttiRow(
          oppilaitosNimi = "Jyväskylän normaalikoulu",
          organisaatioOid = "1.2.246.562.10.14613773812",
          opetuskieli = "suomi",
          vuosiluokka = "6",
          oppilaita = 2,
          vieraskielisiä = 0,
          pidennettyOppivelvollisuusJaVaikeastiVammainen = 0,
          pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = 0,
          virheellisestiSiirretytVaikeastiVammaiset = 0,
          virheellisestiSiirretytMuutKuinVaikeimminVammaiset = 0,
          erityiselläTuella = 0,
          majoitusetu = 0,
          kuljetusetu = 0,
          sisäoppilaitosmainenMajoitus = 0,
          koulukoti = 0,
          joustavaPerusopetus = 0,
          kotiopetus = 1
        ),
        PerusopetuksenOppijamäärätRaporttiRow(
          oppilaitosNimi = "Jyväskylän normaalikoulu",
          organisaatioOid = "1.2.246.562.10.14613773812",
          opetuskieli = "suomi",
          vuosiluokka = "7",
          oppilaita = 2,
          vieraskielisiä = 1,
          pidennettyOppivelvollisuusJaVaikeastiVammainen = 1,
          pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = 1,
          virheellisestiSiirretytVaikeastiVammaiset = 0,
          virheellisestiSiirretytMuutKuinVaikeimminVammaiset = 0,
          erityiselläTuella = 2,
          majoitusetu = 1,
          kuljetusetu = 1,
          sisäoppilaitosmainenMajoitus = 1,
          koulukoti = 1,
          joustavaPerusopetus = 1,
          kotiopetus = 0
        ),
        PerusopetuksenOppijamäärätRaporttiRow(
          oppilaitosNimi = "Jyväskylän normaalikoulu",
          organisaatioOid = "1.2.246.562.10.14613773812",
          opetuskieli = "suomi",
          vuosiluokka = "8",
          oppilaita = 1,
          vieraskielisiä = 0,
          pidennettyOppivelvollisuusJaVaikeastiVammainen = 1,
          pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = 0,
          virheellisestiSiirretytVaikeastiVammaiset = 0,
          virheellisestiSiirretytMuutKuinVaikeimminVammaiset = 0,
          erityiselläTuella = 1,
          majoitusetu = 1,
          kuljetusetu = 1,
          sisäoppilaitosmainenMajoitus = 1,
          koulukoti = 1,
          joustavaPerusopetus = 1,
          kotiopetus = 0
        ),
        PerusopetuksenOppijamäärätRaporttiRow(
          oppilaitosNimi = "Jyväskylän normaalikoulu",
          organisaatioOid = "1.2.246.562.10.14613773812",
          opetuskieli = "suomi",
          vuosiluokka = "Kaikki vuosiluokat yhteensä",
          oppilaita = 5,
          vieraskielisiä = 1,
          pidennettyOppivelvollisuusJaVaikeastiVammainen = 2,
          pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = 1,
          virheellisestiSiirretytVaikeastiVammaiset = 0,
          virheellisestiSiirretytMuutKuinVaikeimminVammaiset = 0,
          erityiselläTuella = 3,
          majoitusetu = 2,
          kuljetusetu = 2,
          sisäoppilaitosmainenMajoitus = 2,
          koulukoti = 2,
          joustavaPerusopetus = 2,
          kotiopetus = 1
        )
      ))
    }
  }
}
