package fi.oph.koski.raportit

import java.sql.Date.{valueOf => sqlDate}
import java.time.LocalDate.{of => localDate}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, MockOppijat}
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.{jyväskylänNormaalikoulu, päiväkotiTouhula}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class EsiopetusRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {
  private val application = KoskiApplicationForTests
  private val raporttiService = new EsiopetusRaporttiService(application)
  private val raporttiBuilder = EsiopetusRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val raportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), sqlDate("2007-01-01"))(session(defaultUser)).rows.map(_.asInstanceOf[EsiopetusRaporttiRow])

  override def beforeAll(): Unit = loadRaportointikantaFixtures

  "Esiopetuksen raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/esiopetus?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2018-01-01&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="esiopetus_koski_raportti_${jyväskylänNormaalikoulu}_20180101.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetus&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2018-01-01")))
      }
    }

    "Raportin kolumnit" - {
      lazy val r = findSingle(raportti, MockOppijat.eskari)

      "Perustiedot" in {
        r.koulutustoimijaNimi should equal(Some("Jyväskylän yliopisto"))
        r.oppilaitosNimi should equal(Some("Jyväskylän normaalikoulu"))
        r.toimipisteNimi should equal(Some("Jyväskylän normaalikoulu"))

        r.opiskeluoikeudenAlkamispäivä should equal(localDate(2006, 8, 13))
        r.opiskeluoikeudenViimeisinTila should equal("valmistunut")

        r.yksilöity should equal(true)
        r.oppijaOid should equal(MockOppijat.eskari.oid)
        r.etunimet should equal(MockOppijat.eskari.etunimet)
        r.sukunimi should equal(MockOppijat.eskari.sukunimi)
      }

      "Opiskeluoikeuden lisätiedot" in {
        r.pidennettyOppivelvollisuus should equal(false)
        r.tukimuodot should equal(Some("1"))
        r.erityisenTuenPäätös should equal(false)
        r.erityisenTuenPäätösOpiskeleeToimintaAlueittain should equal(false)
        r.erityisenTuenPäätösErityisryhmässä should equal(false)
        r.erityisenTuenPäätösToteutuspaikka should equal(None)
        r.vammainen should equal(false)
        r.vaikeastiVammainen should equal(false)
        r.majoitusetu should equal(false)
        r.kuljetusetu should equal(false)
        r.sisäoppilaitosmainenMajoitus should equal(false)
        r.koulukoti should equal(false)
      }
    }

    "Varhaiskasvatuksen järjestäjä" - {
      "näkee vain omat opiskeluoikeutensa" in {
        val tornionTouhulaExcel = raporttiService.buildRaportti(päiväkotiTouhula, localDate(2006, 8, 13), "", None)(session(MockUsers.tornioTallentaja)).sheets
        tornionTouhulaExcel.collect { case d: DataSheet => d.rows }.flatten should be(empty)

        val helsinginTouhulaExcel = raporttiService.buildRaportti(päiväkotiTouhula, localDate(2006, 8, 13), "", None)(session(MockUsers.helsinkiTallentaja)).sheets
        val rows = helsinginTouhulaExcel.collect { case d: DataSheet => d.rows.collect { case r: EsiopetusRaporttiRow => r } }.flatten
        rows.flatMap(_.oppilaitosNimi).toList should equal(List("Päiväkoti Touhula"))
      }

      "voi hakea kaikki ostopalvelu/palveluseteli-tiedot" in {
        val helsinginOstopalveluExcel = raporttiService.buildOstopalveluRaportti(localDate(2006, 8, 13), "", None)(session(MockUsers.helsinkiTallentaja)).sheets
        val rows = helsinginOstopalveluExcel.collect { case d: DataSheet => d.rows.collect { case r: EsiopetusRaporttiRow => r.oppilaitosNimi }.flatten }.flatten
        rows.toList.sorted should equal(List("Päiväkoti Majakka", "Päiväkoti Touhula"))
      }

      "ei näe muiden ostopalvelu/palveluseteli-tietoja" in {
        val tornionOstopalveluExcel = raporttiService.buildOstopalveluRaportti(localDate(2006, 8, 13), "", None)(session(MockUsers.tornioTallentaja)).sheets
        val rows = tornionOstopalveluExcel.collect { case d: DataSheet => d.rows }.flatten
        rows should be(empty)
      }
    }
  }

  private def findSingle(rows: Seq[EsiopetusRaporttiRow], oppija: LaajatOppijaHenkilöTiedot) = {
    val found = rows.filter(_.hetu.exists(oppija.hetu.contains))
    found.length should be(1)
    found.head
  }

  private def session(user: MockUser)= user.toKoskiUser(application.käyttöoikeusRepository)
}
