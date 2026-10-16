package fi.oph.koski.raportit

import fi.oph.koski.api.misc.{OpiskeluoikeudenMitätöintiJaPoistoTestMethods, OpiskeluoikeusTestMethodsPerusopetus}
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, suomenKieli}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.oppilaitos
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.Kunta
import fi.oph.koski.organisaatio.MockOrganisaatiot.{aapajoenKoulu, helsinginKaupunki, tornionKaupunki}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

class KunnanOppijatSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsPerusopetus with OpiskeluoikeudenMitätöintiJaPoistoTestMethods with BeforeAndAfterAll with DirtiesFixtures {
  private val raportointipäivä = date(2026, 9, 1)

  override protected def alterFixture(): Unit = {
    reloadRaportointikanta()
  }

  private val application = KoskiApplicationForTests

  private val kunnanOppijatBuilder = KunnanOppijat(application.raportointiDatabase.db)

  "Raportti voidaan ladata kunnalle ja lataaminen tuottaa auditlogin" in {
    authGet(s"api/raportit/kunnanoppijat?oppilaitosOid=$helsinginKaupunki&paiva=$raportointipäivä&lang=fi&password=salasana", user = MockUsers.paakayttaja) {
      verifyResponseStatusOk()
      response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
      AuditLogTester.verifyLastAuditLogMessageForOperation(
        Map(
          "operation" -> "OPISKELUOIKEUS_RAPORTTI",
          "target" -> Map(
            "hakuEhto" -> s"raportti=kunnanoppijat&oppilaitosOid=$helsinginKaupunki&paiva=$raportointipäivä&lang=fi"
          )
        )
      )
    }
  }

  "Raporttia ei voi ladata organisaatiolle, joka ei ole kunta" in {
    authGet(s"api/raportit/kunnanoppijat?oppilaitosOid=$aapajoenKoulu&paiva=$raportointipäivä&lang=fi&password=salasana") {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam(s"Organisaatio $aapajoenKoulu ei ole kunta"))
    }
  }

  "Kunnan oppilaitoksen käyttöoikeuksilla ei voi ladata kunnan raporttia" in {
    authGet(s"api/raportit/kunnanoppijat?oppilaitosOid=$helsinginKaupunki&paiva=$raportointipäivä&lang=fi&password=salasana", user = MockUsers.stadinAmmattiopistoKatselija) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio())
    }
  }

  "Kunnan esiopetuksen käyttöoikeuksilla ei voi ladata raporttia" in {
    authGet(s"api/raportit/kunnanoppijat?oppilaitosOid=$helsinginKaupunki&paiva=$raportointipäivä&lang=fi&password=salasana", user = MockUsers.esiopetusTallentaja) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeudenTyyppi())
    }
  }

  "Koulutusmuotoihin rajatuilla OPH:n oikeuksilla ei voi ladata raporttia" in {
    authGet(s"api/raportit/kunnanoppijat?oppilaitosOid=$helsinginKaupunki&paiva=$raportointipäivä&lang=fi&password=salasana", user = MockUsers.ophLukioKatselija) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeudenTyyppi())
    }
  }

  "Luottamuksellisten tietojen oikeus muussa organisaatiossa ei riitä" in {
    authGet(s"api/raportit/kunnanoppijat?oppilaitosOid=$helsinginKaupunki&paiva=$raportointipäivä&lang=fi&password=salasana", user = MockUsers.helsinkiIlmanLuottamuksellisiaTietoja) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
    }
  }

  "Perus- ja esiopetuksen oikeuksilla voi ladata raportin" in {
    authGet(s"api/raportit/kunnanoppijat?oppilaitosOid=$helsinginKaupunki&paiva=$raportointipäivä&lang=fi&password=salasana", user = MockUsers.helsinkiPerusopetuksenKatselija) {
      verifyResponseStatusOk()
    }
  }

  lazy val helsinkiläiset = kunnanOppijatBuilder.oppijat(helsinginKaupunki, Kunta.helsinki, raportointipäivä, "Esiopetus")

  "Toisen kunnan koulussa opiskelevat kuntalaiset näkyvät opiskelupaikkoineen" in {
    val oidit = Seq(
      KoskiSpecificMockOppijat.kotikuntalaskelmaSeitsemanKaksitoista,
      KoskiSpecificMockOppijat.kotikuntalaskelmaKolmetoistaViisitoista,
      KoskiSpecificMockOppijat.kotikuntalaskelmaKuusitoistaEiErityista,
      KoskiSpecificMockOppijat.kotikuntalaskelmaEsiopetus
    ).map(_.oid)

    oidit.foreach { oid =>
      val rivi = helsinkiläiset.find(_.oppijaNumero.contains(oid))
      rivi.map(_.oppilaitos) shouldBe Some(Some("Aapajoen koulu"))
      rivi.get.kotikunta shouldBe Some("Helsinki")
      rivi.get.opetuksenJärjestäjä shouldBe Some("Tornion kaupunki")
    }
  }

  "Muiden kuntien asukkaat eivät näy" in {
    helsinkiläiset.find(_.oppijaNumero.contains(KoskiSpecificMockOppijat.kotikuntalaskelmaKuusivuotias.oid)) shouldBe None
    kunnanOppijatBuilder.oppijat(tornionKaupunki, "851", raportointipäivä, "Esiopetus")
      .find(_.oppijaNumero.contains(KoskiSpecificMockOppijat.kotikuntalaskelmaSeitsemanKaksitoista.oid)) shouldBe None
  }

  "Turvakiellon alainen kuntalainen näkyy ilman tunnistetietoja listan lopussa" in {
    helsinkiläiset.find(_.oppijaNumero.contains(KoskiSpecificMockOppijat.kotikuntalaskelmaTurvakielto.oid)) shouldBe None

    val turvakieltoRivit = helsinkiläiset.filter(_.oppijaNumero.contains("Turvakielto"))
    turvakieltoRivit.length shouldBe 1
    val rivi = turvakieltoRivit.head
    rivi.hetu shouldBe None
    rivi.yksiloity shouldBe None
    rivi.etunimet shouldBe None
    rivi.sukunimi shouldBe None
    rivi.kotikunta shouldBe None
    rivi.opetuksenJärjestäjä shouldBe None
    rivi.oppilaitos shouldBe None
    rivi.luokkaAste shouldBe None
    rivi.seitsemänKaksitoista shouldBe true
    helsinkiläiset.last shouldBe rivi
  }

  "Kunnan itse järjestämä opetus" - {
    val ilmari = KoskiSpecificMockOppijat.kotikuntalaskelmaKuusitoistaEiErityista
    val haaganPeruskoulu = "1.2.246.562.10.83119092639"
    lazy val aapajoenOpiskeluoikeus = getOpiskeluoikeudet(ilmari.oid).collect { case oo: PerusopetuksenOpiskeluoikeus => oo }.head

    def ilmarinRivi = kunnanOppijatBuilder.oppijat(helsinginKaupunki, Kunta.helsinki, raportointipäivä, "Esiopetus")
      .find(_.oppijaNumero.contains(ilmari.oid))

    "ei näy kuntalaisella, joka opiskelee myös muun järjestäjän koulussa" in {
      aapajoenOpiskeluoikeus
      val helsingin = PerusopetuksenOpiskeluoikeus(
        oppilaitos = Some(oppilaitos(haaganPeruskoulu)),
        suoritukset = List(PerusopetuksenVuosiluokanSuoritus(
          koulutusmoduuli = PerusopetuksenLuokkaAste(9, PerusopetusExampleData.perusopetuksenDiaarinumero),
          luokka = "9H",
          toimipiste = oppilaitos(haaganPeruskoulu),
          suorituskieli = suomenKieli,
          alkamispäivä = Some(date(2026, 8, 15))
        )),
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(NuortenPerusopetuksenOpiskeluoikeusjakso(date(2026, 8, 15), opiskeluoikeusLäsnä)))
      )
      putOppija(Oppija(ilmari, List(aapajoenOpiskeluoikeus, helsingin)), headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }
      reloadRaportointikanta()

      ilmarinRivi.flatMap(_.oppilaitos) shouldBe Some("Aapajoen koulu")
    }

    "rajaa pois kuntalaisen, joka opiskelee vain kunnan omassa koulussa" in {
      mitätöiOpiskeluoikeus(aapajoenOpiskeluoikeus.oid.get, MockUsers.paakayttaja)
      reloadRaportointikanta()

      ilmarinRivi shouldBe None
    }
  }
}
