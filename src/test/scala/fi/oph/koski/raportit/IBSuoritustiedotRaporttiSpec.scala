package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.ibPredicted
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.ressunLukio
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => localDate}

class IBSuoritustiedotRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private val application = KoskiApplicationForTests
  private val raporttiRepository = IBSuoritustiedotRaporttiRepository(application.raportointiDatabase.db)
  private val raporttiBuilder = IBSuoritustiedotRaportti(raporttiRepository, t)

  private lazy val raporttiRivitIB = raporttiBuilder.build(
      ressunLukio,
      localDate(2012, 1, 1),
      localDate(2022, 1, 1),
      false,
      IBTutkinnonSuoritusRaportti
    )(session(defaultUser)).rows.map(_.asInstanceOf[IBRaporttiRow])

  private lazy val raporttiRivitPreIB = raporttiBuilder.build(
      ressunLukio,
      localDate(2012, 1, 1),
      localDate(2022, 1, 1),
      false,
      PreIBSuoritusRaportti
    )(session(defaultUser)).rows.map(_.asInstanceOf[IBRaporttiRow])

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  "IB suoritustiedot raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/ibsuoritustietojentarkistus?oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="ib_suoritustiedot_${ressunLukio}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=ibsuoritustietojentarkistus&oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=fi")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/ibsuoritustietojentarkistus?oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="ib_suoritustiedot_${ressunLukio}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=ibsuoritustietojentarkistus&oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=sv")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/ibsuoritustietojentarkistus?oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=&osasuoritustenAikarajaus=false&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="ib_suoritustiedot_${ressunLukio}_2018-01-01_2022-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=ibsuoritustietojentarkistus&oppilaitosOid=$ressunLukio&alku=2018-01-01&loppu=2022-01-01&raportinTyyppi=ibtutkinto&osasuoritustenAikarajaus=false&lang=sv")))
      }
    }

    "Raportilla olevat rivit näyttävät tiedot oikein" - {

      "IB-tutkinnon suorituksille" in {
        raporttiRivitIB.size shouldBe 2

        val r = findSingle(raporttiRivitIB, ibPredicted)

        r.koulutustoimijaNimi shouldBe "Helsingin kaupunki"
        r.oppilaitoksenNimi shouldBe "Ressun lukio"
        r.toimipisteNimi shouldBe "Ressun lukio"

        r.yksiloity shouldBe true
        r.oppijaOid shouldBe ibPredicted.oid
        r.hetu shouldBe ibPredicted.hetu
        r.sukunimi shouldBe ibPredicted.sukunimi
        r.etunimet shouldBe ibPredicted.etunimet

        r.opiskeluoikeudenAlkamispäivä shouldBe Some(localDate(2012, 9, 1))
        r.opiskeluoikeudenViimeisinTila shouldBe Some("valmistunut")
        r.opiskeluoikeudenTilatAikajaksonAikana shouldBe "lasna, valmistunut"
        r.päätasonSuoritukset shouldBe Some("IB-tutkinto (International Baccalaureate)")
        r.opiskeluoikeudenPäättymispäivä shouldBe Some(localDate(2016, 6, 4))

        r.rahoitukset shouldBe "1, 1"
        r. maksuttomuus shouldBe None
        r.oikeuttaMaksuttomuuteenPidennetty shouldBe None
        r.pidennettyPäättymispäivä shouldBe false
        r.ulkomainenVaihtoOpiskelija shouldBe false
        r.erityinenKoulutustehtäväJaksot shouldBe None
        r.ulkomaanjaksot shouldBe None
        r.sisäoppilaitosmainenMajoitus shouldBe None
      }

      "Pre-IB suorituksille" in {
        raporttiRivitPreIB.size shouldBe 4

        val r = findSingle(raporttiRivitPreIB, ibPredicted)

        r.koulutustoimijaNimi shouldBe "Helsingin kaupunki"
        r.oppilaitoksenNimi shouldBe "Ressun lukio"
        r.toimipisteNimi shouldBe "Ressun lukio"

        r.yksiloity shouldBe true
        r.oppijaOid shouldBe ibPredicted.oid
        r.hetu shouldBe ibPredicted.hetu
        r.sukunimi shouldBe ibPredicted.sukunimi
        r.etunimet shouldBe ibPredicted.etunimet

        r.opiskeluoikeudenAlkamispäivä shouldBe Some(localDate(2012, 9, 1))
        r.opiskeluoikeudenViimeisinTila shouldBe Some("valmistunut")
        r.opiskeluoikeudenTilatAikajaksonAikana shouldBe "lasna, valmistunut"
        r.päätasonSuoritukset shouldBe Some("Pre-IB")
        r.opiskeluoikeudenPäättymispäivä shouldBe Some(localDate(2016, 6, 4))

        r.rahoitukset shouldBe "1, 1"
        r. maksuttomuus shouldBe None
        r.oikeuttaMaksuttomuuteenPidennetty shouldBe None
        r.pidennettyPäättymispäivä shouldBe false
        r.ulkomainenVaihtoOpiskelija shouldBe false
        r.erityinenKoulutustehtäväJaksot shouldBe None
        r.ulkomaanjaksot shouldBe None
        r.sisäoppilaitosmainenMajoitus shouldBe None
      }
    }
  }

  private def findSingle(rows: Seq[IBRaporttiRow], oppija: LaajatOppijaHenkilöTiedot) = {
    val found = rows.filter(_.hetu.exists(oppija.hetu.contains))
    found.length should be(1)
    found.head
  }

  private def session(user: KoskiMockUser): KoskiSpecificSession= user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
