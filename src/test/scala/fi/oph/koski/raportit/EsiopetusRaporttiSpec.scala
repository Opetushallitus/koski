package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, MockOppijat}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.{FreeSpec, Matchers}

class EsiopetusRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods {

  lazy val raportti = {
    loadRaportointikantaFixtures
    val raporttiBuilder = EsiopetusRaportti(KoskiApplicationForTests.raportointiDatabase.db)
    val päivä = Date.valueOf("2007-01-01")
    raporttiBuilder.build(MockOrganisaatiot.jyväskylänNormaalikoulu, päivä).rows.map(_.asInstanceOf[EsiopetusRaporttiRow])
  }

  "Esiopetuksen raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      verifyRaportinLataaminen
    }
    "Raportin kolumnit" - {
      lazy val r = findSingle(raportti, MockOppijat.eskari)

      "Perustiedot" in {
        r.koulutustoimijaNimi should equal(Some("Jyväskylän yliopisto"))
        r.oppilaitosNimi should equal(Some("Jyväskylän normaalikoulu"))
        r.toimipisteNimi should equal(Some("Jyväskylän normaalikoulu"))

        r.opiskeluoikeudenAlkamispäivä should equal(LocalDate.of(2006, 8, 13))
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
  }

  private def findSingle(rows: Seq[EsiopetusRaporttiRow], oppija: LaajatOppijaHenkilöTiedot) = {
    val found = rows.filter(_.hetu.exists(oppija.hetu.contains))
    found.length should be(1)
    found.head
  }

  private def verifyRaportinLataaminen = {
    val organisaatioOid = MockOrganisaatiot.jyväskylänNormaalikoulu
    val paiva = "2018-01-01"

    authGet(s"api/raportit/esiopetus?oppilaitosOid=${organisaatioOid}&paiva=${paiva}&password=kalasana") {
      verifyResponseStatusOk()
      response.headers("Content-Disposition").head should equal(s"""attachment; filename="esiopetus_koski_raportti_${organisaatioOid}_${paiva.toString.replaceAll("-","")}.xlsx"""")
      val ENCRYPTED_XLSX_PREFIX = Array(0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1).map(_.toByte)
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetus&oppilaitosOid=$organisaatioOid&paiva=$paiva")))
    }
  }
}
