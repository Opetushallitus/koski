package fi.oph.koski.massaluovutus

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.{ExamplesTutkintokoulutukseenValmentavaKoulutus, PerusopetusExampleData}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.util.Wait
import fi.oph.scalaschema.Serializer.format
import org.json4s.{JNothing, JValue}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.nio.charset.StandardCharsets
import java.time.LocalDate

class MassaluovutusAiemmatOpiskeluoikeudetSpec extends AnyFreeSpec with MassaluovutusTestMethods with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with OpiskeluoikeusTestMethodsAmmatillinen {
  import MassaluovutusAiemmatOpiskeluoikeudetSpec._
  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  private val lauri = KoskiSpecificMockOppijat.aiemmatLukio
  private val demoOppijat = List(
    KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus,
    lauri,
    KoskiSpecificMockOppijat.aiemmatAmmatillinen,
    KoskiSpecificMockOppijat.aiemmatOmaOppilaitos,
    KoskiSpecificMockOppijat.aiemmatMyöhemminAlkanut,
    KoskiSpecificMockOppijat.aiemmatEiAiempia,
  )
  private val query = MassaluovutusQueryAiemmatOpiskeluoikeudet(organisaatioOid = MockOrganisaatiot.stadinAmmattiopisto, oppijaOidit = Some(demoOppijat.map(_.oid)))
  private def oidilla(oppijat: OppijaHenkilö*) = query.copy(oppijaOidit = Some(oppijat.map(_.oid).toList))

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    resetFixtures()
    val päättynyt = KoskiSpecificMockOppijat.aiemmatTestiPäättynyt
    createOrUpdate(päättynyt, PerusopetusExampleData.päättötodistusOpiskeluoikeus())
    createOrUpdate(päättynyt, päättymispäivällä(makeOpiskeluoikeus(alkamispäivä = LocalDate.of(2015, 1, 1)), LocalDate.of(2018, 1, 1)))
    val tulevaisuudessaAlkava = KoskiSpecificMockOppijat.aiemmatTestiTulevaisuudessaAlkava
    createOrUpdate(tulevaisuudessaAlkava, PerusopetusExampleData.päättötodistusOpiskeluoikeus())
    createOrUpdate(tulevaisuudessaAlkava, makeOpiskeluoikeus(alkamispäivä = LocalDate.now().plusMonths(1)))
    val tuva = KoskiSpecificMockOppijat.aiemmatTestiTuva
    createOrUpdate(tuva, PerusopetusExampleData.päättötodistusOpiskeluoikeus())
    createOrUpdate(tuva, ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusEiValmistunut.copy(lisätiedot = None))
    val slave = KoskiSpecificMockOppijat.aiemmatTestiSlave.henkilö
    createOrUpdate(slave, PerusopetusExampleData.päättötodistusOpiskeluoikeus())
    createOrUpdate(slave, makeOpiskeluoikeus(alkamispäivä = LocalDate.of(2020, 1, 1)))
  }

  // Siivotaan beforeAll:ssa luodut opiskeluoikeudet, jotta samassa JVM:ssä seuraavien suitejen oppijamäärät eivät muutu
  override protected def afterAll(): Unit = {
    resetFixtures()
    super.afterAll()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Wait.until { !app.massaluovutusService.hasWork }
    app.massaluovutusService.truncate()
  }

  private def oppijanTiedosto(files: List[String], oid: String) = files.find(_.contains(oid))

  private def koulutusmuodot(json: JValue): List[String] =
    (json \ "opiskeluoikeudet").children.map(oo => (oo \ "tyyppi" \ "koodiarvo").extract[String])

  private def oppilaitoksetJaKoulutusmuodot(json: JValue): Set[(String, String)] =
    (json \ "opiskeluoikeudet").children.map(oo => ((oo \ "oppilaitos" \ "oid").extract[String], (oo \ "tyyppi" \ "koodiarvo").extract[String])).toSet

  private def lisätieto(json: JValue, koulutusmuoto: String, kenttä: String): JValue =
    (json \ "opiskeluoikeudet").children
      .find(oo => (oo \ "tyyppi" \ "koodiarvo").extract[String] == koulutusmuoto)
      .map(oo => oo \ "lisätiedot" \ kenttä)
      .getOrElse(fail(s"$koulutusmuoto-opiskeluoikeutta ei löytynyt"))

  private def haeTulostiedostot(q: MassaluovutusQueryAiemmatOpiskeluoikeudet = query, user: UserWithPassword = oletuskäyttäjä): List[String] = {
    val queryId = addQuerySuccessfully(q, user)(_.queryId)
    waitForCompletion(queryId, user).files
  }

  private def oppijanTulos[T](files: List[String], oppija: OppijaHenkilö, user: UserWithPassword = oletuskäyttäjä)(f: JValue => T): T =
    verifyResultAndContent(oppijanTiedosto(files, oppija.oid).getOrElse(fail(s"Tiedostoa ei löytynyt oppijalle ${oppija.sukunimi}")), user) {
      f(JsonMethods.parse(body))
    }

  "Aiemmat opiskeluoikeudet" - {
    "Käyttöoikeudet ja parametrit" - {
      "Ei onnistu ilman AIEMMAT_OPISKELUOIKEUDET-roolia" in {
        addQuery(query, MockUsers.helsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu, jos rooli on vain toisessa organisaatiossa" in {
        addQuery(query, MockUsers.tornioAiemmatOpiskeluoikeudetHelsinkiKatselija) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu koulutustoimijan oidilla" in {
        addQuery(query.copy(organisaatioOid = MockOrganisaatiot.helsinginKaupunki), oletuskäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiOppilaitos())
        }
      }

      "Ei onnistu koulutustoimijan oidilla globaaleilla lukuoikeuksilla" in {
        addQuery(query.copy(organisaatioOid = MockOrganisaatiot.helsinginKaupunki), MockUsers.paakayttaja) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Ei onnistu tuntemattomalla koulutusmuodolla" in {
        addQuery(query.copy(koulutusmuoto = Some(List("perusopetus", "korkeakoulutus"))), oletuskäyttäjä) {
          verifyResponseStatus(400)
          body should include("Tuntematon koulutusmuoto")
        }
      }

      "Ei onnistu ilman oppijoita" in {
        addQuery(query.copy(hetut = None, oppijaOidit = Some(Nil)), oletuskäyttäjä) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiOppijoita())
        }
      }

      "Ei onnistu virheellisellä hetulla" in {
        addQuery(query.copy(hetut = Some(List("123456-789Z"))), oletuskäyttäjä) {
          verifyResponseStatus(400)
          body should include("badRequest.validation.henkilötiedot.hetu")
        }
      }

      "Hylätään, jos annettu oppija on tuntematon" in {
        val tuntematon = "1.2.246.562.24.98765432100"
        addQuery(oidilla(lauri).copy(oppijaOidit = Some(List(lauri.oid, tuntematon))), oletuskäyttäjä) {
          verifyResponseStatus(400)
          body should include("eiNykyinenOpiskelija")
          body should include(tuntematon)
          body should not include lauri.oid
        }
      }

      "Hylätään, jos annetun oppijan opiskeluoikeus omassa oppilaitoksessa on päättynyt" in {
        val oppija = KoskiSpecificMockOppijat.aiemmatTestiPäättynyt
        addQuery(oidilla(oppija), oletuskäyttäjä) {
          verifyResponseStatus(400)
          body should include("eiNykyinenOpiskelija")
          body should include(oppija.oid)
        }
      }

      "Hylätään, jos annettu oppija on täyttänyt 18 vuotta ennen kuluvaa vuotta" in {
        val oppija = KoskiSpecificMockOppijat.eero
        addQuery(oidilla(oppija), oletuskäyttäjä) {
          verifyResponseStatus(400)
          body should include("täyttänyt18EnnenKyselyvuotta")
          body should include(oppija.oid)
          body should not include "eiNykyinenOpiskelija"
        }
      }
    }

    "Palauttaa hetulla annetun nykyisen opiskelijan aiemmat opiskeluoikeudet ja kirjaa ne audit-lokiin" in {
      AuditLogTester.clearMessages()
      val files = haeTulostiedostot(query.copy(hetut = lauri.hetu.map(List(_)), oppijaOidit = None))
      files should have length 1
      oppijanTulos(files, lauri) { json =>
        (json \ "henkilö" \ "oid").extract[String] shouldBe lauri.oid
        oppilaitoksetJaKoulutusmuodot(json) shouldBe Set(perusopetusRaumonKoulussa, (MockOrganisaatiot.tornionYhteislyseonLukio, "lukiokoulutus"))
      }
      val auditViesti = AuditLogTester.getLogMessages.map(JsonMethods.parse(_)).find(m =>
        (m \ "operation").extract[String] == "AIEMMAT_OPISKELUOIKEUDET_KATSOMINEN" &&
          (m \ "target" \ "oppijaHenkiloOid").extractOpt[String].contains(lauri.oid)
      )
      auditViesti shouldBe defined
      (auditViesti.get \ "target" \ "hakuEhto").extract[String] shouldBe
        s"koulutusmuoto=esiopetus&koulutusmuoto=perusopetus&koulutusmuoto=ammatillinenkoulutus&koulutusmuoto=lukiokoulutus&organisaatio=${MockOrganisaatiot.stadinAmmattiopisto}"
    }

    "Rajaa koulutusmuodon mukaan" in {
      oppijanTulos(haeTulostiedostot(oidilla(lauri).copy(koulutusmuoto = Some(List("perusopetus")))), lauri) { json =>
        koulutusmuodot(json) shouldBe List("perusopetus")
      }
    }

    "Suodattaa sensitiiviset kentät ilman LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-roolia" in {
      oppijanTulos(haeTulostiedostot(oidilla(lauri)), lauri) { json =>
        lisätieto(json, "lukiokoulutus", "sisäoppilaitosmainenMajoitus") should not be JNothing
      }
      val eiLuottamuksellinen = MockUsers.helsinkiAiemmatOpiskeluoikeudetEiLuottamuksellinen
      oppijanTulos(haeTulostiedostot(oidilla(lauri), eiLuottamuksellinen), lauri, eiLuottamuksellinen) { json =>
        koulutusmuodot(json).sorted shouldBe List("lukiokoulutus", "perusopetus")
        lisätieto(json, "lukiokoulutus", "sisäoppilaitosmainenMajoitus") shouldBe JNothing
      }
    }

    "Master/slave -yhdistys: slave-oidilla annetun oppijan aiemmat opiskeluoikeudet ryhmitellään masterin alle" in {
      val slaveOid = KoskiSpecificMockOppijat.aiemmatTestiSlave.henkilö.oid
      val master = KoskiSpecificMockOppijat.aiemmatTestiMaster
      val files = haeTulostiedostot(query.copy(oppijaOidit = Some(List(slaveOid))))
      oppijanTiedosto(files, slaveOid) shouldBe None
      oppijanTulos(files, master) { json =>
        (json \ "henkilö" \ "oid").extract[String] shouldBe master.oid
        (json \ "linkitetytOidit").extract[List[String]] should contain(slaveOid)
        koulutusmuodot(json) shouldBe List("perusopetus")
      }
    }

    "Tulevaisuudessa alkava opiskeluoikeus omassa oppilaitoksessa tekee oppijasta nykyisen opiskelijan" in {
      val oppija = KoskiSpecificMockOppijat.aiemmatTestiTulevaisuudessaAlkava
      oppijanTulos(haeTulostiedostot(oidilla(oppija)), oppija) { json =>
        oppilaitoksetJaKoulutusmuodot(json) shouldBe Set((MockOrganisaatiot.jyväskylänNormaalikoulu, "perusopetus"))
      }
    }

    "Alle 18-vuotias TUVA-opiskelija on nykyinen opiskelija, vaikka TUVA-opiskeluoikeuksia ei palauteta" in {
      val oppija = KoskiSpecificMockOppijat.aiemmatTestiTuva
      oppijanTulos(haeTulostiedostot(oidilla(oppija)), oppija) { json =>
        oppilaitoksetJaKoulutusmuodot(json) shouldBe Set((MockOrganisaatiot.jyväskylänNormaalikoulu, "perusopetus"))
      }
    }

    "HiddenFromAiemmatOpiskeluoikeudet-kentät jätetään pois myös luottamuksellisten tietojen katselijalta" in {
      oppijanTulos(haeTulostiedostot(oidilla(KoskiSpecificMockOppijat.aiemmatOmaOppilaitos)), KoskiSpecificMockOppijat.aiemmatOmaOppilaitos) { json =>
        lisätieto(json, "ammatillinenkoulutus", "vaativanErityisenTuenErityinenTehtävä") should not be JNothing
        lisätieto(json, "ammatillinenkoulutus", "vankilaopetuksessa") shouldBe JNothing
      }
      // Oppijan perusopetus on PerusopetusExampleData.päättötodistusOpiskeluoikeus, jonka 8. luokan suorituksella on käyttäytymisenArvio
      val oppija = KoskiSpecificMockOppijat.aiemmatTestiTulevaisuudessaAlkava
      oppijanTulos(haeTulostiedostot(oidilla(oppija)), oppija) { json =>
        val vuosiluokat = (json \ "opiskeluoikeudet").children.flatMap(oo => (oo \ "suoritukset").children).filter(s => (s \ "tyyppi" \ "koodiarvo").extract[String] == "perusopetuksenvuosiluokka")
        vuosiluokat should not be empty
        vuosiluokat.map(_ \ "käyttäytymisenArvio") should contain only JNothing
      }
    }

    "Toisen käyttäjän kyselyä ei näe" in {
      val queryId = addQuerySuccessfully(query, oletuskäyttäjä)(_.queryId)
      getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }

    "Demo-oppijat (AiemmatOpiskeluoikeudetDemoFixtures)" - {
      "Palauttaa kullekin oppijalle ennen Stadin opintoja alkaneet opiskeluoikeudet, myös Stadin omat päättyneet" in {
        val files = haeTulostiedostot()

        oppijanTulos(files, KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus) { json =>
          oppilaitoksetJaKoulutusmuodot(json) shouldBe Set((MockOrganisaatiot.arpelanPäiväkoti, "esiopetus"), perusopetusRaumonKoulussa)
        }
        oppijanTulos(files, lauri) { json =>
          oppilaitoksetJaKoulutusmuodot(json) shouldBe Set(perusopetusRaumonKoulussa, (MockOrganisaatiot.tornionYhteislyseonLukio, "lukiokoulutus"))
        }
        oppijanTulos(files, KoskiSpecificMockOppijat.aiemmatAmmatillinen) { json =>
          oppilaitoksetJaKoulutusmuodot(json) shouldBe Set(perusopetusRaumonKoulussa, (MockOrganisaatiot.omnia, "ammatillinenkoulutus"))
        }
        oppijanTulos(files, KoskiSpecificMockOppijat.aiemmatOmaOppilaitos) { json =>
          oppilaitoksetJaKoulutusmuodot(json) shouldBe Set(perusopetusRaumonKoulussa, (MockOrganisaatiot.stadinAmmattiopisto, "ammatillinenkoulutus"))
        }
        oppijanTulos(files, KoskiSpecificMockOppijat.aiemmatMyöhemminAlkanut) { json =>
          oppilaitoksetJaKoulutusmuodot(json) shouldBe Set(perusopetusRaumonKoulussa)
        }
        oppijanTiedosto(files, KoskiSpecificMockOppijat.aiemmatEiAiempia.oid) shouldBe None
      }

      "Esi- ja perusopetuksen arkaluontoiset lisätiedot palautetaan vain luottamuksellisten tietojen katselijalle" in {
        val oppija = KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus

        oppijanTulos(haeTulostiedostot(oidilla(oppija)), oppija) { json =>
          lisätieto(json, "esiopetus", "pidennettyOppivelvollisuus") should not be JNothing
          lisätieto(json, "esiopetus", "erityisenTuenPäätökset") should not be JNothing
          lisätieto(json, "perusopetus", "kuljetusetu") should not be JNothing
          lisätieto(json, "perusopetus", "sisäoppilaitosmainenMajoitus") should not be JNothing
        }

        val eiLuottamuksellinen = MockUsers.helsinkiAiemmatOpiskeluoikeudetEiLuottamuksellinen
        oppijanTulos(haeTulostiedostot(oidilla(oppija), eiLuottamuksellinen), oppija, eiLuottamuksellinen) { json =>
          oppilaitoksetJaKoulutusmuodot(json) shouldBe Set((MockOrganisaatiot.arpelanPäiväkoti, "esiopetus"), perusopetusRaumonKoulussa)
          lisätieto(json, "esiopetus", "pidennettyOppivelvollisuus") shouldBe JNothing
          lisätieto(json, "esiopetus", "erityisenTuenPäätökset") shouldBe JNothing
          lisätieto(json, "perusopetus", "kuljetusetu") shouldBe JNothing
          lisätieto(json, "perusopetus", "sisäoppilaitosmainenMajoitus") shouldBe JNothing
        }
      }
    }
  }
}

object MassaluovutusAiemmatOpiskeluoikeudetSpec {
  val oletuskäyttäjä: UserWithPassword = MockUsers.helsinkiAiemmatOpiskeluoikeudet
  val perusopetusRaumonKoulussa: (String, String) = (MockOrganisaatiot.raumonKoulu, "perusopetus")
}
