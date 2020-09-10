package fi.oph.koski.raportit

import java.sql.Date.{valueOf => sqlDate}
import java.time.LocalDate.{of => localDate}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, MockOppijat}
import fi.oph.koski.koskiuser.MockUsers.{helsinkiTallentaja, tornioTallentaja}
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.{helsinginKaupunki, jyväskylänNormaalikoulu, päiväkotiMajakka, päiväkotiTouhula}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.Organisaatio.Oid
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class EsiopetuksenOppijamäärätRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {
  private val application = KoskiApplicationForTests
  private val raporttiBuilder = EsiopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val raportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), sqlDate("2007-01-01"))(session(defaultUser)).rows.map(_.asInstanceOf[EsiopetuksenOppijamäärätRaporttiRow])

  override def beforeAll(): Unit = loadRaportointikantaFixtures

  "Esiopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/esiopetuksenoppijamäärätraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2018-01-01&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="esiopetuksen_oppijamäärät_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetuksenoppijamäärätraporttioppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01")))
      }
    }

    "Raportin kolumnit" in {
      lazy val r = findSingle(raportti, MockOppijat.eskari)

      r.oppilaitosNimi should equal("Jyväskylän normaalikoulu")
      r.opetuskieli should equal("suomi")
      r.esiopetusoppilaidenMäärä should equal(2)
      r.vieraskielisiä should equal(0)
      r.koulunesiopetuksessa should equal(2)
      r.päiväkodinesiopetuksessa should equal(0)
      r.viisivuotiaita should equal(0)
      r.viisivuotiaitaEiPidennettyäOppivelvollisuutta should equal(0)
      r.pidennettyOppivelvollisuusJaVaikeastiVammainen should equal(0)
      r.pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen should equal(0)
      r.virheellisestiSiirretytVaikeastiVammaiset should equal(0)
      r.virheellisestiSiirretytMuutKuinVaikeimminVammaiset should equal(0)
      r.erityiselläTuella should equal(0)
      r.majoitusetu should equal(0)
      r.kuljetusetu should equal(0)
      r.sisäoppilaitosmainenMajoitus should equal(0)
    }
  }

  private def findSingle(rows: Seq[EsiopetuksenOppijamäärätRaporttiRow], oppija: LaajatOppijaHenkilöTiedot) = {
    val found = rows.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
    found.length should be(1)
    found.head
  }

  private def session(user: MockUser)= user.toKoskiUser(application.käyttöoikeusRepository)
}
