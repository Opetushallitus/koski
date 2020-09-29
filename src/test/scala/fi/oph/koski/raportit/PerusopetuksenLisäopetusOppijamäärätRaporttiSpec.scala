package fi.oph.koski.raportit

import java.sql.Date.{valueOf => sqlDate}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.MockUser
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class PerusopetuksenLisäopetusOppijamäärätRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {
  override def beforeAll(): Unit = loadRaportointikantaFixtures

  private def session(user: MockUser) = user.toKoskiUser(application.käyttöoikeusRepository)

  private val application = KoskiApplicationForTests
  private val raporttiBuilder = PerusopetuksenLisäopetusOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val raportti = raporttiBuilder
    .build(Set(jyväskylänNormaalikoulu), sqlDate("2012-01-01"))(session(defaultUser))
    .rows.map(_.asInstanceOf[PerusopetuksenLisäopetusOppijamäärätRaporttiRow])

  "Perusopetuksen lisäopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenlisaopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="perusopetuksen_oppijamäärät_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenlisaopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01"
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
          opetuskieli = "suomi",
          oppilaita = 5,
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
