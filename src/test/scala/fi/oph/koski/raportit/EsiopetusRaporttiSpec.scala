package fi.oph.koski.raportit

import java.time.LocalDate.{of => localDate}
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession, MockUser, MockUsers}
import fi.oph.koski.koskiuser.MockUsers.{helsinkiTallentaja, tornioTallentaja}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.{helsinginKaupunki, jyväskylänNormaalikoulu, päiväkotiMajakka, päiväkotiTouhula}
import fi.oph.koski.raportit.esiopetus.{EsiopetusRaportti, EsiopetusRaporttiRow, EsiopetusRaporttiService}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.Organisaatio.Oid
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class EsiopetusRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {
  private val application = KoskiApplicationForTests
  private val raporttiService = new EsiopetusRaporttiService(application)
  private val raporttiBuilder = EsiopetusRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val raportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), localDate(2007, 1, 1), t)(session(defaultUser)).rows.map(_.asInstanceOf[EsiopetusRaporttiRow])

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  "Esiopetuksen raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/esiopetus?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2018-01-01&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="esiopetus_koski_raportti_${jyväskylänNormaalikoulu}_20180101.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetus&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2018-01-01&lang=fi")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/esiopetus?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2018-01-01&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="esiopetus_koski_raportti_${jyväskylänNormaalikoulu}_20180101.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetus&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2018-01-01&lang=sv")))
      }
    }

    "Raportin kolumnit" - {
      lazy val r = findSingle(raportti, KoskiSpecificMockOppijat.eskari)

      "Perustiedot" in {
        r.koulutustoimijaNimi should equal(Some("Jyväskylän yliopisto"))
        r.oppilaitosNimi should equal(Some("Jyväskylän normaalikoulu"))
        r.toimipisteNimi should equal(Some("Jyväskylän normaalikoulu"))

        r.opiskeluoikeudenAlkamispäivä should equal(localDate(2006, 8, 13))
        r.opiskeluoikeudenPäättymispäivä should equal(Some(localDate(2007, 6, 3)))
        r.opiskeluoikeudenViimeisinTila should equal("valmistunut")
        r.opiskeluoikeudenTilaRaportinTarkasteluajankohtana should equal("lasna")
        r.koulutuskoodi should equal("001101")
        r.koulutus should equal("Peruskoulun esiopetus")
        r.suorituksenVahvistuspäivä should equal(Some(localDate(2007, 6, 3)))
        r.perusteenDiaarinumero should equal (Some("102/011/2014"))
        r.suorituskieli should equal (Some("suomi"))

        r.yksilöity should equal(true)
        r.oppijaOid should equal(KoskiSpecificMockOppijat.eskari.oid)
        r.etunimet should equal(KoskiSpecificMockOppijat.eskari.etunimet)
        r.sukunimi should equal(KoskiSpecificMockOppijat.eskari.sukunimi)
        r.kotikunta should equal(Some("Jyväskylä"))
      }

      "Opiskeluoikeuden lisätiedot" in {
        r.pidennettyOppivelvollisuus should equal(false)
        r.erityisenTuenPäätös should equal(false)
        r.vammainen should equal(false)
        r.vaikeastiVammainen should equal(false)
        r.majoitusetu should equal(false)
        r.kuljetusetu should equal(false)
        r.sisäoppilaitosmainenMajoitus should equal(false)
        r.koulukoti should equal(false)
        r.ostopalveluTaiPalveluseteli should equal(None)
      }
    }

    "Varhaiskasvatuksen järjestäjä" - {
      "näkee vain omat opiskeluoikeutensa" in {
        val tornionTekemäRaportti = buildOrganisaatioRaportti(tornioTallentaja, päiväkotiTouhula)
        getOppilaitokset(tornionTekemäRaportti) should be(empty)

        val helsinginTekemäRaportti = buildOrganisaatioRaportti(helsinkiTallentaja, päiväkotiTouhula)
        getOppilaitokset(helsinginTekemäRaportti) should equal(List("Päiväkoti Touhula"))
      }

      "voi hakea kaikki koulutustoimijan alla olevat tiedot" in {
        val raportti = buildOrganisaatioRaportti(helsinkiTallentaja, helsinginKaupunki)
        getOppilaitokset(raportti) should equal(List("Kulosaaren ala-aste", "Päiväkoti Majakka", "Päiväkoti Touhula"))
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetus&oppilaitosOid=$helsinginKaupunki&paiva=2006-08-13&lang=fi")))
      }

      "voi hakea kaikki ostopalvelu/palveluseteli-tiedot" in {
        val raportti = buildOstopalveluRaportti(helsinkiTallentaja)
        getOppilaitokset(raportti) should equal(List("Päiväkoti Majakka", "Päiväkoti Touhula"))
        getRows(raportti).flatMap(_.ostopalveluTaiPalveluseteli) should equal(List("JM02", "JM02"))

        val ostopalveluOrganisaatiot = s"$jyväskylänNormaalikoulu,$päiväkotiMajakka,$päiväkotiTouhula"
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetus&oppilaitosOid=$ostopalveluOrganisaatiot&paiva=2006-08-13&lang=fi")))
      }

      "ei näe muiden ostopalvelu/palveluseteli-tietoja" in {
        val raportti = buildOstopalveluRaportti(tornioTallentaja)
        getOppilaitokset(raportti) should be(empty)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetus&oppilaitosOid=&paiva=2006-08-13&lang=fi")))
      }

      "globaaleilla käyttöoikeuksilla voi tehdä raportin" in {
        val raportti = buildOrganisaatioRaportti(MockUsers.paakayttaja, helsinginKaupunki)
        getOppilaitokset(raportti) should equal(List("Kulosaaren ala-aste", "Päiväkoti Majakka", "Päiväkoti Touhula"))
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetus&oppilaitosOid=$helsinginKaupunki&paiva=2006-08-13&lang=fi")))
      }
    }
  }

  private def buildOstopalveluRaportti(user: KoskiMockUser) =
    raporttiService.buildOstopalveluRaportti(localDate(2006, 8, 13), "", None, t)(session(user))

  private def buildOrganisaatioRaportti(user: KoskiMockUser, organisaatio: Oid) =
    raporttiService.buildOrganisaatioRaportti(organisaatio, localDate(2006, 8, 13), "", None, t)(session(user))

  private def getOppilaitokset(raportti: OppilaitosRaporttiResponse) = {
    getRows(raportti).flatMap(_.oppilaitosNimi).sorted
  }

  private def getRows(raportti: OppilaitosRaporttiResponse): List[EsiopetusRaporttiRow] = {
    raportti.sheets.collect { case d: DataSheet =>
      d.rows.collect {
        case r: EsiopetusRaporttiRow => r
      }
    }.flatten.toList
  }

  private def findSingle(rows: Seq[EsiopetusRaporttiRow], oppija: LaajatOppijaHenkilöTiedot) = {
    val found = rows.filter(_.hetu.exists(oppija.hetu.contains))
    found.length should be(1)
    found.head
  }

  private def session(user: KoskiMockUser): KoskiSpecificSession= user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
