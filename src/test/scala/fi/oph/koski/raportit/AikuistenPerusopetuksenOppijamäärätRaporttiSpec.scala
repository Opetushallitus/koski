package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportit.aikuistenperusopetus.{AikuistenPerusopetuksenOppijamäärätRaportti, AikuistenPerusopetuksenOppijamäärätRaporttiRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

class AikuistenPerusopetuksenOppijamäärätRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {
  private val application = KoskiApplicationForTests
  private val raporttiBuilder = AikuistenPerusopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val t: LocalizationReader = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val raportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), date(2010, 1, 1), t)(session(defaultUser)).rows.map(_.asInstanceOf[AikuistenPerusopetuksenOppijamäärätRaporttiRow])

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  "Aikuisten perusopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/aikuistenperusopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2010-01-01&password=salasana&lang=fi") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="aikuisten_perusopetuksen_vos_raportti-2010-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=aikuistenperusopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2010-01-01&lang=fi")))
      }
    }

    "Raportin kolumnit" in {
      lazy val r = findSingle(raportti)

      r.oppilaitosOid should equal(jyväskylänNormaalikoulu)
      r.oppilaitosNimi should equal("Jyväskylän normaalikoulu")
      r.opetuskieli should equal("suomi")
      r.oppilaidenMääräYhteensä should equal(8)
      r.oppilaidenMääräVOS should equal(4)
      r.oppilaidenMääräMuuKuinVOS should equal(4)
      r.oppimääränSuorittajiaYhteensä should equal(5)
      r.oppimääränSuorittajiaVOS should equal(2)
      r.oppimääränSuorittajiaMuuKuinVOS should equal(3)
      r.aineopiskelijoitaYhteensä should equal(3)
      r.aineopiskelijoitaVOS should equal(2)
      r.aineopiskelijoitaMuuKuinVOS should equal(1)
      r.vieraskielisiäYhteensä should equal(2)
      r.vieraskielisiäVOS should equal(1)
      r.vieraskielisiäMuuKuinVOS should equal(1)
    }
  }

  private def findSingle(rows: Seq[AikuistenPerusopetuksenOppijamäärätRaporttiRow]) = {
    val found = rows.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
    found.length should be(1)
    found.head
  }

  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
