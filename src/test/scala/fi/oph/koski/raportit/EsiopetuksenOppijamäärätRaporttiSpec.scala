package fi.oph.koski.raportit

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.MockUsers.{helsinkiTallentaja, tornioTallentaja}
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.{helsinginKaupunki, jyväskylänNormaalikoulu, päiväkotiTouhula, tornionKaupunki}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.Organisaatio.Oid
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => localDate}

class EsiopetuksenOppijamäärätRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with BeforeAndAfterAll {

  private val application = KoskiApplicationForTests
  private val raporttiBuilder = EsiopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val raportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), localDate(2007, 1, 1), t)(session(defaultUser)).rows.map(_.asInstanceOf[EsiopetuksenOppijamäärätRaporttiRow])
  private lazy val ilmanOikeuksiaRaportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), localDate(2007, 1, 1), t)(session(tornioTallentaja)).rows.map(_.asInstanceOf[EsiopetuksenOppijamäärätRaporttiRow])
  private lazy val tyhjäVuosiRaportti =
    raporttiBuilder.build(List(jyväskylänNormaalikoulu), localDate(2012, 1, 1), t)(session(defaultUser)).rows.map(_.asInstanceOf[EsiopetuksenOppijamäärätRaporttiRow])
  private val raporttiService = EsiopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

  "Esiopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/esiopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&password=salasana&lang=fi") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="esiopetuksen_oppijamäärät_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=fi")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/esiopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&password=salasana&lang=sv") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="esiopetuksen_oppijamäärät_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=sv")))
      }
    }

    "Raportin kolumnit" in {
      lazy val r = findSingle(raportti)

      r.oppilaitosNimi should equal("Jyväskylän normaalikoulu")
      r.opetuskieli should equal("suomi")
      r.esiopetusoppilaidenMäärä should equal(3)
      r.vieraskielisiä should equal(0)
      r.koulunesiopetuksessa should equal(3)
      r.päiväkodinesiopetuksessa should equal(0)
      r.viisivuotiaita should equal(0)
      r.viisivuotiaitaEiPidennettyäOppivelvollisuutta should equal(0)
      r.pidennettyOppivelvollisuusJaVaikeastiVammainen should equal(0)
      r.pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen should equal(1)
      r.virheellisestiSiirretytVaikeastiVammaiset should equal(0)
      r.virheellisestiSiirretytMuutKuinVaikeimminVammaiset should equal(0)
      r.erityiselläTuella should equal(1)
      r.majoitusetu should equal(1)
      r.kuljetusetu should equal(1)
      r.sisäoppilaitosmainenMajoitus should equal(1)
    }

    "Haettu vuodelle, jona ei oppilaita" in {
      tyhjäVuosiRaportti.length should be(0)
    }

    "Ei näe muiden organisaatioiden raporttia" in {
      ilmanOikeuksiaRaportti.length should be(0)
    }

    "Varhaiskasvatuksen järjestäjä" - {
      "näkee vain omat opiskeluoikeutensa" in {
        val tornionTekemäRaportti = buildRaportti(tornioTallentaja, päiväkotiTouhula)
        getOppilaitokset(tornionTekemäRaportti) should be(empty)

        val helsinginTekemäRaportti = buildRaportti(helsinkiTallentaja, päiväkotiTouhula)
        getOppilaitokset(helsinginTekemäRaportti) should equal(List("Päiväkoti Touhula"))
      }

      "voi hakea kaikki koulutustoimijan alla olevat tiedot" in {
        val raportti = buildRaportti(helsinkiTallentaja, helsinginKaupunki)
        getOppilaitokset(raportti) should equal(List("Kulosaaren ala-aste", "Päiväkoti Majakka", "Päiväkoti Touhula"))
      }

      "ei näe muiden ostopalvelu/palveluseteli-tietoja" in {
        val raportti = buildRaportti(tornioTallentaja, tornionKaupunki)
        getOppilaitokset(raportti) should be(empty)
      }

      "globaaleilla käyttöoikeuksilla voi tehdä raportin" in {
        val raportti = buildRaportti(MockUsers.paakayttaja, helsinginKaupunki)
        getOppilaitokset(raportti) should equal(List("Kulosaaren ala-aste", "Päiväkoti Majakka", "Päiväkoti Touhula"))
      }
    }
  }

  private def findSingle(rows: Seq[EsiopetuksenOppijamäärätRaporttiRow]) = {
    val found = rows.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
    found.length should be(1)
    found.head
  }

  private def buildRaportti(user: KoskiMockUser, organisaatio: Oid) =
    raporttiService.build(List(organisaatio), localDate(2007, 1, 1), t)(session(user))

  private def getOppilaitokset(raportti: DataSheet) = {
    getRows(raportti).map(_.oppilaitosNimi).sorted
  }

  private def getRows(raportti: DataSheet): List[EsiopetuksenOppijamäärätRaporttiRow] = {
    raportti.rows.collect {
      case r: EsiopetuksenOppijamäärätRaporttiRow => r
    }.toList
  }

  private def session(user: KoskiMockUser): KoskiSpecificSession = user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
