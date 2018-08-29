package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoKatselija
import fi.oph.koski.log.AuditLogTester

class RaportitSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  private val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  "Oppijavuosiraportti" - {
    "näyttää oikeat tiedot" in {
      val result = Oppijavuosiraportti.buildOppijavuosiraportti(raportointiDatabase, MockOrganisaatiot.stadinAmmattiopisto, LocalDate.parse("2016-01-01"), LocalDate.parse("2016-12-31"))

      val aarnenOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.ammattilainen.oid).oid.get
      val aarnenRivi = result.find(_.opiskeluoikeusOid == aarnenOpiskeluoikeusOid)
      aarnenRivi shouldBe defined
      val rivi = aarnenRivi.get

      rivi.koulutusmoduulit should equal("361902")
      rivi.osaamisalat should equal(Some("1590"))
      rivi.viimeisinOpiskeluoikeudenTila should equal("valmistunut")
      rivi.opintojenRahoitukset should equal(Some("4"))
      rivi.opiskeluoikeusPäättynyt should equal(true)
      rivi.läsnäPäivät should equal(31 + 29 + 31 + 30 + 30) // Aarne graduated 31.5.2016, so count days from 1.1.2016 to 30.5.2016
    }

    "Excel-tiedoston muodostus toimii (ja tuottaa audit log viestin)" in {
      val queryString = s"oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31"
      authGet(s"api/raportit/oppijavuosiraportti?$queryString") {
        verifyResponseStatusOk()
        val ZIP_FILE_PREFIX = Array(0x50, 0x4b, 0x03, 0x04)
        response.bodyBytes.take(4) should equal(ZIP_FILE_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=oppijavuosiraportti&$queryString")))
      }
    }

    "Excel-tiedoston haku on sallittu vain pilottikäyttäjille" in {
      authGet(s"api/raportit/oppijavuosiraportti?oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&alku=2016-01-01&loppu=2016-12-31", user = stadinAmmattiopistoKatselija) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden("Ei sallittu tälle käyttäjälle"))
      }
    }
  }

  override def beforeAll(): Unit = {
    authGet("api/raportointikanta/clear") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/opiskeluoikeudet") { verifyResponseStatusOk() }
    authGet("api/raportointikanta/henkilot") { verifyResponseStatusOk() }
  }
}
