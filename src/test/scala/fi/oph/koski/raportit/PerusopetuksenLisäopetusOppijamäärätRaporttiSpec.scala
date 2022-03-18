package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

class PerusopetuksenLisäopetusOppijamäärätRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private val application = KoskiApplicationForTests
  private val raporttiBuilder = PerusopetuksenLisäopetusOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val raportti = raporttiBuilder
    .build(Seq(jyväskylänNormaalikoulu), date(2012, 1, 1), t)(session(defaultUser))
    .rows.map(_.asInstanceOf[PerusopetuksenLisäopetusOppijamäärätRaporttiRow])

  "Perusopetuksen lisäopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenlisaopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&password=salasana&lang=fi") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="lisaopetus_vos_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenlisaopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=fi"
            )
          )
        )
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenlisaopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&password=salasana&lang=sv") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="lisaopetus_vos_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenlisaopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=sv"
            )
          )
        )
      }
    }

    "Raportin sarakkeet" in {
      val rows = raportti.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
      rows.length should be(1)
      rows.toList should equal(List(
        PerusopetuksenLisäopetusOppijamäärätRaporttiRow(
          oppilaitosNimi = "Jyväskylän normaalikoulu",
          organisaatioOid = "1.2.246.562.10.14613773812",
          opetuskieli = "suomi",
          oppilaita = 6,
          vieraskielisiä = 1,
          pidennettyOppivelvollisuusJaVaikeastiVammainen = 2,
          pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = 1,
          virheellisestiSiirretytVaikeastiVammaiset = 1,
          virheellisestiSiirretytMuutKuinVaikeimminVammaiset = 1,
          erityiselläTuella = 1,
          majoitusetu = 2,
          kuljetusetu = 2,
          sisäoppilaitosmainenMajoitus = 2,
          koulukoti = 2
        )
      ))
    }
  }
}
