package fi.oph.koski.massaluovutus

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.massaluovutus.raportit._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.util.Wait
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.nio.charset.StandardCharsets
import java.time.LocalDate

class MassaluovutusRaportitSpec extends AnyFreeSpec with MassaluovutusTestMethods with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  override protected def beforeEach(): Unit = {
    resetFixturesSkipInvalidOpiskeluoikeudet()
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Wait.until { !app.massaluovutusService.hasWork }
    app.massaluovutusService.truncate()
  }

  private def raporttiKyselyOnnistuu(query: MassaluovutusQueryParameters, user: UserWithPassword): String = {
    val queryId = addQuerySuccessfully(query, user) { response =>
      response.status should equal(QueryState.pending)
      response.queryId
    }
    val complete = waitForCompletion(queryId, user)

    complete.files should have length 1
    complete.files.foreach(verifyResult(_, user))

    val raportitService = new RaportitService(app)
    complete.sourceDataUpdatedAt.map(_.toLocalDateTime) should equal(Some(raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika))
    queryId
  }

  private def raporttiKyselyEiOnnistu(query: MassaluovutusQueryParameters, user: UserWithPassword): Unit = {
    addQuery(query, user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden())
    }
  }

  private def verifyAinoaAuditlokimerkintä(raportti: String, paiva: String, oppilaitokset: List[String], queryId: String): Unit =
    verifyAinoaAuditlokimerkintä(queryId, "lang=fi&" + oppilaitokset.map(oid => s"oppilaitosOid=$oid").mkString("&") + s"&paiva=$paiva&raportti=$raportti")

  private def verifyAinoaAuditlokimerkintä(queryId: String, hakuEhto: String): Unit =
    AuditLogTester.verifyOnlyAuditLogMessageForOperation(Map(
      "operation" -> "OPISKELUOIKEUS_RAPORTTI",
      "target" -> Map(
        "hakuEhto" -> hakuEhto,
        "massaluovutusId" -> queryId,
      ),
    ))

  private val helsinginOstopalveluyksiköt = List(
    MockOrganisaatiot.jyväskylänNormaalikoulu,
    MockOrganisaatiot.päiväkotiMajakka,
    MockOrganisaatiot.päiväkotiTouhula,
  )

  // Lukio

  "Lukion suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryLukionSuoritustiedot(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2012-01-01&lang=fi&loppu=2016-01-01&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&raportti=lukionsuoritustietojentarkistus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }

    "Ei onnistu ilman oikeuksia lukiokoulutuksen opiskeluoikeuksiin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.perusopetusTallentaja)
      raporttiKyselyEiOnnistu(query, MockUsers.esiopetusTallentaja)
    }
  }

  "Lukio 2019 suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryLukio2019Suoritustiedot(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2012-01-01&lang=fi&loppu=2016-01-01&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&raportti=lukio2019suoritustietojentarkistus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Lukion kurssikertymät" - {
    val query = MassaluovutusQueryLukioKurssikertymat(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2012-01-01&lang=fi&loppu=2016-01-01&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&raportti=lukiokurssikertymat")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }

    "Tiedoston nimi on kyselyn kielellä" in {
      val queryId = addQuerySuccessfully(query.copy(language = Some("sv")), MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)(_.queryId)
      val tiedostot = waitForCompletion(queryId, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä).files
      tiedostot should have length 1
      tiedostot.head should endWith("/gymnasiets_antal_kurser_20120101-20160101.xlsx")
    }
  }

  "Lukio 2019 opintopistekertymät" - {
    val query = MassaluovutusQueryLukio2019Opintopistekertymat(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2012-01-01&lang=fi&loppu=2016-01-01&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&raportti=lukio2019opintopistekertymat")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Lukio/DIA/IB/International/ESH opiskelijamäärät" - {
    val query = MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat(
      paiva = LocalDate.of(2016, 1, 15),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä("lukiodiaibinternationaleshopiskelijamaarat", "2016-01-15", List(MockOrganisaatiot.jyväskylänNormaalikoulu), queryId)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  // Ammatillinen

  "Ammatillinen opiskelijavuositiedot" - {
    val query = MassaluovutusQueryAmmatillinenOpiskelijavuositiedot(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2016-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&raportti=ammatillinenopiskelijavuositiedot")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "Ammatillinen tutkinto suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2016-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&raportti=ammatillinentutkintosuoritustietojentarkistus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "Ammatillinen osittainen tutkinto suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2016-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&raportti=ammatillinenosittainensuoritustietojentarkistus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "Muu ammatillinen koulutus" - {
    val query = MassaluovutusQueryMuuAmmatillinen(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2016-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&raportti=muuammatillinen")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "TOPKS ammatillinen koulutus" - {
    val query = MassaluovutusQueryTOPKSAmmatillinen(
      alku = LocalDate.of(2016, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2016-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&raportti=topksammatillinen")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  // Perusopetus

  "Perusopetuksen vuosiluokka" - {
    val query = MassaluovutusQueryPerusopetuksenVuosiluokka(
      paiva = LocalDate.of(2012, 1, 1),
      vuosiluokka = "9",
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"lang=fi&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&paiva=2012-01-01&raportti=perusopetuksenvuosiluokka&vuosiluokka=9")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }

    "Ei onnistu koulutustoimijalle esiopetuksen ostopalvelun oppilaitoksen tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiTallentaja)
    }
  }

  "Perusopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryPerusopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2012, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä("perusopetuksenoppijamaaratraportti", "2012-01-01", List(MockOrganisaatiot.jyväskylänNormaalikoulu), queryId)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Aikuisten perusopetuksen suoritustietojen tarkistus" - {
    val query = MassaluovutusQueryAikuistenPerusopetusSuoritustiedot(
      alku = LocalDate.of(2008, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      raportinTyyppi = "päättövaihe",
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2008-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&raportinTyyppi=päättövaihe&raportti=aikuistenperusopetuksensuoritustietojentarkistus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Aikuisten perusopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2010, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä("aikuistenperusopetuksenoppijamaaratraportti", "2010-01-01", List(MockOrganisaatiot.jyväskylänNormaalikoulu), queryId)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Aikuisten perusopetuksen kurssikertymät" - {
    val query = MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma(
      alku = LocalDate.of(2008, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2008-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&raportti=aikuistenperusopetuksenkurssikertymaraportti")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  // Esiopetus

  "Esiopetus" - {
    val query = MassaluovutusQueryEsiopetus(
      paiva = LocalDate.of(2012, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }

    "Kysely kirjaa auditlokiin yhden merkinnän" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä("esiopetus", "2012-01-01", List(MockOrganisaatiot.jyväskylänNormaalikoulu), queryId)
    }

    "Toisen käyttäjän tulostiedostoa ei voi ladata OPH:n käyttäjä, joka ei saisi tehdä samaa kyselyä" in {
      val queryId = addQuerySuccessfully(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)(_.queryId)
      val tiedosto = waitForCompletion(queryId, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä).files.head
      getResult(tiedosto, MockUsers.ophLukioKatselija) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
      verifyResult(tiedosto, MockUsers.ophkatselija)
    }

    "Ostopalvelu/palveluseteli-kysely onnistuu ja kirjaa auditlokiin ostopalveluyksiköt" in {
      AuditLogTester.clearMessages()
      val ostopalvelukysely = query.copy(
        paiva = LocalDate.of(2015, 1, 1),
        organisaatioOid = Some(app.organisaatioService.ostopalveluRootOid),
      )
      val queryId = raporttiKyselyOnnistuu(ostopalvelukysely, MockUsers.helsinkiTallentaja)
      verifyAinoaAuditlokimerkintä("esiopetus", "2015-01-01", helsinginOstopalveluyksiköt, queryId)
    }
  }

  "Esiopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryEsiopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2012, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä("esiopetuksenoppijamaaratraportti", "2012-01-01", List(MockOrganisaatiot.jyväskylänNormaalikoulu), queryId)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }

    "Ostopalvelu/palveluseteli-kysely onnistuu ja kirjaa auditlokiin ostopalveluyksiköt" in {
      AuditLogTester.clearMessages()
      val ostopalvelukysely = query.copy(
        paiva = LocalDate.of(2015, 1, 1),
        organisaatioOid = Some(app.organisaatioService.ostopalveluRootOid),
      )
      val queryId = raporttiKyselyOnnistuu(ostopalvelukysely, MockUsers.helsinkiTallentaja)
      verifyAinoaAuditlokimerkintä("esiopetuksenoppijamaaratraportti", "2015-01-01", helsinginOstopalveluyksiköt, queryId)
    }
  }

  // Muut

  "Perusopetukseen valmistava opetus" - {
    val query = MassaluovutusQueryPerusopetukseenValmistava(
      alku = LocalDate.of(2008, 1, 1),
      loppu = LocalDate.of(2020, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2008-01-01&lang=fi&loppu=2020-01-01&oppilaitosOid=${MockOrganisaatiot.jyväskylänNormaalikoulu}&raportti=perusopetukseenvalmistavansuoritustietojentarkistus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "TUVA perusopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2022, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä("tuvaperusopetuksenoppijamaaratraportti", "2022-01-01", List(MockOrganisaatiot.stadinAmmattiopisto), queryId)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "TUVA suoritustiedot" - {
    val query = MassaluovutusQueryTuvaSuoritustiedot(
      alku = LocalDate.of(2021, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.stadinAmmattiopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2021-01-01&lang=fi&loppu=2025-01-01&oppilaitosOid=${MockOrganisaatiot.stadinAmmattiopisto}&raportti=tuvasuoritustietojentarkistus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
    }
  }

  "IB-tutkinnon suoritustiedot" - {
    val query = MassaluovutusQueryIBSuoritustiedot(
      alku = LocalDate.of(2012, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      raportinTyyppi = "ibtutkinto",
      organisaatioOid = Some(MockOrganisaatiot.ressunLukio),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.paakayttaja)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2012-01-01&lang=fi&loppu=2025-01-01&oppilaitosOid=${MockOrganisaatiot.ressunLukio}&raportinTyyppi=ibtutkinto&raportti=ibsuoritustietojentarkistus")
    }

    "Kysely kotikuntapäivällä onnistuu ja kirjaa päivän auditlokiin" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query.copy(kotikuntaPvm = Some(LocalDate.of(2015, 1, 1))), MockUsers.paakayttaja)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2012-01-01&kotikuntaPvm=2015-01-01&lang=fi&loppu=2025-01-01&oppilaitosOid=${MockOrganisaatiot.ressunLukio}&raportinTyyppi=ibtutkinto&raportti=ibsuoritustietojentarkistus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Vapaan sivistystyön JOTPA" - {
    val query = MassaluovutusQueryVSTJOTPA(
      alku = LocalDate.of(2022, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.varsinaisSuomenKansanopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.varsinaisSuomiPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2022-01-01&lang=fi&loppu=2025-01-01&oppilaitosOid=${MockOrganisaatiot.varsinaisSuomenKansanopisto}&raportti=vstjotpa")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Vapaan sivistystyön vapaatavoitteinen koulutus" - {
    val query = MassaluovutusQueryVSTVapaatavoitteinen(
      alku = LocalDate.of(2022, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.varsinaisSuomenKansanopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.varsinaisSuomiPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2022-01-01&lang=fi&loppu=2025-01-01&oppilaitosOid=${MockOrganisaatiot.varsinaisSuomenKansanopisto}&raportti=vstvapaatavoitteinen")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Vapaan sivistystyön oppivelvollisille suunnattu koulutus" - {
    val query = MassaluovutusQueryVSTKOPS(
      alku = LocalDate.of(2022, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.varsinaisSuomenKansanopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.varsinaisSuomiPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2022-01-01&lang=fi&loppu=2025-01-01&oppilaitosOid=${MockOrganisaatiot.varsinaisSuomenKansanopisto}&raportti=vstkops")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Vapaan sivistystyön osaamismerkit" - {
    val query = MassaluovutusQueryVSTOsaamismerkit(
      alku = LocalDate.of(2024, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.varsinaisSuomenKansanopisto),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.varsinaisSuomiPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2024-01-01&lang=fi&loppu=2025-01-01&oppilaitosOid=${MockOrganisaatiot.varsinaisSuomenKansanopisto}&raportti=vstosaamismerkit")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Muu kuin säännelty koulutus" - {
    val query = MassaluovutusQueryMuuKuinSaanneltyKoulutus(
      alku = LocalDate.of(2022, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.MuuKuinSäänneltyKoulutusToimija.oppilaitos),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.muuKuinSäänneltyKoulutusYritys)
      verifyAinoaAuditlokimerkintä(queryId, s"alku=2022-01-01&lang=fi&loppu=2025-01-01&oppilaitosOid=${MockOrganisaatiot.MuuKuinSäänneltyKoulutusToimija.oppilaitos}&raportti=muukuinsaanneltykoulutus")
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Lukioon valmistavan koulutuksen opiskelijamäärät" - {
    val query = MassaluovutusQueryLuvaOpiskelijamaarat(
      paiva = LocalDate.of(2016, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.ressunLukio),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.paakayttaja)
      verifyAinoaAuditlokimerkintä("luvaopiskelijamaarat", "2016-01-01", List(MockOrganisaatiot.ressunLukio), queryId)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoPalvelukäyttäjä)
    }
  }

  "Perusopetuksen lisäopetuksen oppijamäärät" - {
    val query = MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti(
      paiva = LocalDate.of(2012, 1, 1),
      organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu),
    )

    "Kysely onnistuu, palauttaa oikeat tiedostot ja kirjaa auditlokiin hakuehdot" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)
      verifyAinoaAuditlokimerkintä("perusopetuksenlisaopetuksenoppijamaaratraportti", "2012-01-01", List(MockOrganisaatiot.jyväskylänNormaalikoulu), queryId)
    }

    "Ei onnistu väärän organisaation tietoihin" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiKatselija)
    }
  }

  "Kotikuntalaskelma" - {
    val query = MassaluovutusQueryKotikuntalaskelma(
      paiva = LocalDate.of(2026, 9, 1),
      organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query.copy(organisaatioOid = Some(MockOrganisaatiot.aapajoenKoulu)), MockUsers.kalle)
    }

    "Kysely kirjaa auditlokiin yhden merkinnän" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.helsinkiPerusopetuksenKatselija)
      verifyAinoaAuditlokimerkintä("kotikuntalaskelma", "2026-09-01", List(MockOrganisaatiot.helsinginKaupunki), queryId)
    }

    "Perus- ja esiopetuksen oikeuksilla kysely onnistuu" in {
      raporttiKyselyOnnistuu(query, MockUsers.helsinkiPerusopetuksenKatselija)
    }

    "Pelkillä esiopetuksen oikeuksilla ei onnistu" in {
      raporttiKyselyEiOnnistu(query, MockUsers.esiopetusTallentaja)
    }

    "Pelkillä perusopetuksen oikeuksilla ei onnistu" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiPelkänPerusopetuksenKatselija)
    }

    "Koulutusmuotoihin rajatuilla OPH:n oikeuksilla ei onnistu" in {
      raporttiKyselyEiOnnistu(query, MockUsers.ophLukioKatselija)
    }

    "Ei onnistu toisen koulutustoimijan oppilaitokselle" in {
      raporttiKyselyEiOnnistu(query.copy(organisaatioOid = Some(MockOrganisaatiot.aapajoenKoulu)), MockUsers.helsinkiTallentaja)
    }
  }

  "Kunnan oppijat" - {
    val query = MassaluovutusQueryKunnanOppijat(
      paiva = LocalDate.of(2026, 9, 1),
      organisaatioOid = Some(MockOrganisaatiot.helsinginKaupunki),
    )

    "Kysely onnistuu ja palauttaa oikeat tiedostot" in {
      raporttiKyselyOnnistuu(query, MockUsers.paakayttaja)
    }

    "Kysely kirjaa auditlokiin yhden merkinnän" in {
      AuditLogTester.clearMessages()
      val queryId = raporttiKyselyOnnistuu(query, MockUsers.helsinkiPerusopetuksenKatselija)
      verifyAinoaAuditlokimerkintä("kunnanoppijat", "2026-09-01", List(MockOrganisaatiot.helsinginKaupunki), queryId)
    }

    "Perus- ja esiopetuksen oikeuksilla kysely onnistuu" in {
      raporttiKyselyOnnistuu(query, MockUsers.helsinkiPerusopetuksenKatselija)
    }

    "Kysely onnistuu kunnalle, jolla ei ole omia oppilaitoksia" in {
      raporttiKyselyOnnistuu(query.copy(organisaatioOid = Some(MockOrganisaatiot.pyhtäänKunta)), MockUsers.pyhtäänTallentaja)
    }

    "Ei onnistu organisaatiolle, joka ei ole kunta" in {
      raporttiKyselyEiOnnistu(query.copy(organisaatioOid = Some(MockOrganisaatiot.aapajoenKoulu)), MockUsers.kalle)
    }

    "Kunnan oppilaitoksen käyttöoikeuksilla ei onnistu" in {
      raporttiKyselyEiOnnistu(query, MockUsers.stadinAmmattiopistoKatselija)
    }

    "Kunnan esiopetuksen käyttöoikeuksilla ei onnistu" in {
      raporttiKyselyEiOnnistu(query, MockUsers.esiopetusTallentaja)
    }

    "Kunnan pelkillä perusopetuksen käyttöoikeuksilla ei onnistu" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiPelkänPerusopetuksenKatselija)
    }

    "Koulutusmuotoihin rajatuilla OPH:n oikeuksilla ei onnistu" in {
      raporttiKyselyEiOnnistu(query, MockUsers.ophLukioKatselija)
    }

    "Luottamuksellisten tietojen oikeus muussa organisaatiossa ei riitä" in {
      raporttiKyselyEiOnnistu(query, MockUsers.helsinkiIlmanLuottamuksellisiaTietoja)
    }
  }
}
