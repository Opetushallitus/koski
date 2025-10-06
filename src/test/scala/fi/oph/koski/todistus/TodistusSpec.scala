package fi.oph.koski.todistus

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExamplesKielitutkinto
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Opiskeluoikeus, Suoritus, YleisenKielitutkinnonSuoritus}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.io.ByteArrayInputStream
import java.net.URL
import java.time.{Duration, LocalDate}

class TodistusSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with PutOpiskeluoikeusTestMethods[KielitutkinnonOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[KielitutkinnonOpiskeluoikeus]]

  val app = KoskiApplicationForTests

  val vahvistettuKielitutkinnonOpiskeluoikeus = ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2011, 1, 3), "FI", "kt")
  val defaultOpiskeluoikeus = vahvistettuKielitutkinnonOpiskeluoikeus

  override protected def beforeAll(): Unit = {
    resetFixtures()
  }

  override protected def afterEach(): Unit = {
    Wait.until { !app.todistusService.hasWork }
    app.todistusService.truncate()
  }

  "Generointipyyntö ja statuspyyntö" - {
    "onnistuu kansalaiselta omasta kielitutkinnon opiskeluoikeudesta" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(oppijaOid, opiskeluoikeusOid, lang)

      withoutRunningTodistusScheduler {
        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          getStatusSuccessfully(todistusJob.id, hetu) { status =>
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }

    "onnistuu kansalaiselta omasta kielitutkinnon opiskeluoikeudesta, joka on tallennettu kansalaisen toisella oppija-oidilla" in {
      // TODO: TOR-2400
      false shouldBe(true)
    }

    "onnistuu huoltajalta huollettavan kielitutkinnon opiskeluoikeudesta" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val kirjautujanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOppijaOid = KoskiSpecificMockOppijat.eskari.oid
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

      val req = TodistusGenerateRequest(huollettavanOppijaOid, huollettavanOpiskeluoikeusOid, lang)

      withoutRunningTodistusScheduler {
        addGenerateJobSuccessfully(req, kirjautujanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          getStatusSuccessfully(todistusJob.id, kirjautujanHetu) { status =>
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }

    "Onnistuu virkailijapääkäyttäjältä kielitutkinnon opiskeluoikeuteen" in {
      // TODO: TOR-2400
    }
  }

  "Generointipyyntö ei onnistu" - {
    "kansalaiselta muuntyyppisten opintojen opiskeluoikeuteen" in {
      val lang = "fi"
      val kirjautujanHetu = KoskiSpecificMockOppijat.lukiolainen.hetu.get
      val kirjautujanOppijaOid = KoskiSpecificMockOppijat.lukiolainen.oid
      val muidenOpintojenOpiskeluoikeus = getVahvistettuOpiskeluoikeus(KoskiSpecificMockOppijat.lukiolainen.oid)

      muidenOpintojenOpiskeluoikeus.get.isInstanceOf[KielitutkinnonOpiskeluoikeus] should be(false)

      val muidenOpintojenOpiskeluoikeusOid = muidenOpintojenOpiskeluoikeus.flatMap(_.oid).get

      val req = TodistusGenerateRequest(kirjautujanOppijaOid, muidenOpintojenOpiskeluoikeusOid, lang)

      withoutRunningTodistusScheduler {
        addGenerateJob(req, kirjautujanHetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "kansalaiselta mitätöidystä opiskeluoikeudesta" in {
      val lang = "fi"
      val oo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.eskari)
      val hetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.eskari.oid
      val opiskeluoikeusOid = oo.oid.get

      val req = TodistusGenerateRequest(oppijaOid, opiskeluoikeusOid, lang)

      withoutRunningTodistusScheduler {
        addGenerateJob(req, hetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "kansalaiselta toisen oppijan kielitutkinnon opiskeluoikeuteen" in {
      val lang = "fi"
      val kirjautujanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val kirjautujanOppijaOid = KoskiSpecificMockOppijat.eskari.oid
      val toisenOpiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(kirjautujanOppijaOid, toisenOpiskeluoikeusOid, lang)

      withoutRunningTodistusScheduler {
        addGenerateJob(req, kirjautujanHetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "huoltajalta toisen kansalaisen kielitutkinnon opiskeluoikeuteen" in {
      val lang = "fi"
      val huoltajaKirjautujanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOppijaOid = KoskiSpecificMockOppijat.eskari.oid
      val toisenOpiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(huollettavanOppijaOid, toisenOpiskeluoikeusOid, lang)

      withoutRunningTodistusScheduler {
        addGenerateJob(req, huoltajaKirjautujanHetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "Virkailijakäyttäjältä" in {
      // TODO: TOR-2400
      false shouldBe(true)
    }
  }

  "Statuspyyntö ei onnistu" - {
    "kansalaiselta toisen oppijan luomaan generointi-jobiin" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val toisenKansalaisenHetu = KoskiSpecificMockOppijat.eskari.hetu.get

      val req = TodistusGenerateRequest(oppijaOid, opiskeluoikeusOid, lang)

      withoutRunningTodistusScheduler {
        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          getStatus(todistusJob.id, toisenKansalaisenHetu) {
            verifyResponseStatus(404)
          }
        }
      }
    }

    "Huoltajalta huollettavan luomaan generointi-jobiin" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOppijaOid = KoskiSpecificMockOppijat.eskari.oid
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

      val req = TodistusGenerateRequest(huollettavanOppijaOid, huollettavanOpiskeluoikeusOid, lang)

      withoutRunningTodistusScheduler {
        addGenerateJobSuccessfully(req, huollettavanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          getStatus(todistusJob.id, huoltajanHetu) {
            verifyResponseStatus(404)
          }
        }
      }
    }

    "Virkailijapääkäyttäjältä kansalaisen itsensä luomaan generointi-jobiin" in {
      // TODO: TOR-2400
      false shouldBe(true)
    }

    "Virkailijakäyttäjältä" in {
      // TODO: TOR-2400
      false shouldBe(true)
    }
  }

  "Allekirjoitetun todistuksen saa ladattua sen valmistuttua" in {
    val lang = "fi"
    val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
    val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
    val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

    val req = TodistusGenerateRequest(oppijaOid, opiskeluoikeusOid, lang)

    val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
      todistusJob.state should equal(TodistusState.QUEUED)
      todistusJob
    }

    val completedJob = waitForCompletion(todistusJob.id, hetu)

    completedJob.error should be(None)
    // completedJob.opiskeluoikeusVersionumero should be(1) // TODO: TOR-2400
    // completedJob.oppijaHenkilötiedotHash should be Some("foo")  // TODO: TOR-2400
    // completedJob.completedAt should not be empty // TODO: TOR-2400
    // completedJob.startedAt should not be empty // TODO: TOR-2400
    // completedJob.worker should not be empty // TODO: TOR-2400

    verifyResultAndContent(s"/api/todistus/download/${todistusJob.id}", hetu) {
      val pdfBytes = response.getContentBytes()

      // Files.write(Paths.get(s"todistus-${todistusJob.id}.pdf"), pdfBytes)

      val pdfStream = new ByteArrayInputStream(pdfBytes)

      val document = PDDocument.load(pdfStream)
      val pdfText = new PDFTextStripper().getText(document)
      assert(pdfText.contains("Läpi meni!"))

      val signerName = document.getLastSignatureDictionary.getName()
      signerName should be("TEST Signer")
      // TODO: TOR-2400: onko hyötyä tarkistaa jotain muuta? Ainakin esim. että revocation listit on tallennettu? Käytännössä validia signaturea mock-allekirjoituksella ei synny, mutta muut
      // algoritmin osuudet voinee tarkistaa?

      // TODO: TOR-2400
      // - tarkista, että pdf:n sisältö vastaa myös graafisesti renderöitynä odotettua (marginaalit, sivumäärä jne.)
      document.close()
    }
  }

  // TODO: TOR-2400 testejä:
  //  - huollettavan todistukset
  //  - käyttöoikeudet: ei pääse toisen oppijan todistuksiin tai niiden tilaan
  //  - käyttöoikeudet: OPH pääkäyttäjä pääsee kaikkien oppijoiden todistuksiin
  //  - uuden skedulerin herääminen, jos vanha kontti kuollut
  //  - yritys tehdä todistusta muusta kuin sallitun tyyppisestä valmistuneesta opiskeluoikeudesta epäonnistuu
  //  - keskenjääneiden töiden automaattinen peruutus ja uudelleenyritykset (3x) toimivat
  //  - töiden cancelointi toimii
  //  - debug-rajapinta vain pääkäyttäjille: voi hakea myös allekirjoittamattoman todistuksen
  //  - Todistuksen ulkonäkö säilyy samana, eli marginaalit, fontit jne. eivät muutu (joku kuvavertailu tarvitaan?)
  //  - oppijalle ei palauta teknisiä tietoja (S3 osoitteet, virheilmoitukset jne.)
  //  - pääkäyttäjälle palautetaan tekniset tiedot (S3-osoitteet, virheilmoitukset jne.)
  //  - uusi pyyntö samalla sisällöllä ei käynnistä uutta generointia vaan palauttaa jo luodun tiedot
  //  - tallenna kantaan oo-versio ja henkilötiedot hash niistä tiedoista, joilla todistus luotiin: ei niistä, mitkä oli pyyntöhetkellä (voivat muuttua välissä)
  //  - todistusgeneroinnin voi pyytää uudestaan, vain jos
  //      - vanha on epäonnistunut/peruttu
  //      - mennyt kauan aikaa edellisestä
  //      - opiskeluoikeus on muuttunut
  //      - henkilötiedoissa on tapahtunut muutos

  private def getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid: String): Option[KielitutkinnonOpiskeluoikeus] = {
    getOpiskeluoikeudet(oppijaOid).find(_.suoritukset.exists {
      case s: YleisenKielitutkinnonSuoritus if s.vahvistus.isDefined => true
      case _ => false
    }).map(_.asInstanceOf[KielitutkinnonOpiskeluoikeus])
  }

  private def getVahvistettuOpiskeluoikeus(oppijaOid: String): Option[Opiskeluoikeus] = {
    getOpiskeluoikeudet(oppijaOid).find(_.suoritukset.exists {
      case s: Suoritus if s.vahvistus.isDefined => true
      case _ => false
    })
  }

  def addGenerateJob[T](req: TodistusGenerateRequest, hetu: String)(f: => T): T =
    get(s"api/todistus/generate/${req.toPathParams}", headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)

  def addGenerateJobSuccessfully[T](req: TodistusGenerateRequest, hetu: String)(f: TodistusJob => T): T = {
    addGenerateJob(req, hetu) {
      f(parsedResponse)
    }
  }

  def getStatus[T](id: String, hetu: String)(f: => T): T =
    get(s"api/todistus/status/$id", headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)

  def getStatusSuccessfully[T](id: String, hetu: String)(f: TodistusJob => T): T = {
    getStatus(id, hetu) {
      f(parsedResponse)
    }
  }

  def getResult[T](url: String, hetu: String)(f: => T): T = {
    val rootUrl = KoskiApplicationForTests.config.getString("koski.root.url")
    get(url.replace(rootUrl, ""), headers = kansalainenLoginHeaders(hetu))(f)
  }

  def verifyResult(url: String, hetu: String): Unit =
    getResult(url, hetu) {
      verifyResponseStatus(302) // 302: Found (redirect)
    }

  def verifyResultAndContent[T](url: String, hetu: String)(f: => T): T = {
    val location = new URL(getResult(url, hetu) {
      verifyResponseStatus(302) // 302: Found (redirect)
      response.header("Location")
    })
    withBaseUrl(location) {
      get(s"${location.getPath}?${location.getQuery}") {
        verifyResponseStatusOk()
        f
      }
    }
  }

  def waitForStateTransition(id: String, hetu: String)(states: String*): TodistusJob = {
    var lastResponse: Option[TodistusJob] = None
    Wait.until {
      getStatusSuccessfully(id, hetu) { response =>
        states should contain(response.state)
        lastResponse = Some(response)
        response.state == states.last
      }
    }
    lastResponse.get
  }

  def waitForCompletion(id: String, hetu: String): TodistusJob =
    waitForStateTransition(id, hetu)(
      TodistusState.QUEUED,
      TodistusState.GENERATING_RAW_PDF,
      TodistusState.SAVING_RAW_PDF,
      TodistusState.STAMPING_PDF,
      TodistusState.SAVING_STAMPED_PDF,
      TodistusState.COMPLETED
    )

  def parsedResponse: TodistusJob = {
    verifyResponseStatusOk()
    val json = JsonMethods.parse(response.body)
    val result = KoskiApplicationForTests.validatingAndResolvingExtractor.extract[TodistusJob](json, strictDeserialization)
    result should not be Left
    result.right.get
  }

  def withoutRunningTodistusScheduler[T](f: => T): T =
    try {
      app.todistusScheduler.pause(Duration.ofDays(1))
      f
    } finally {
      app.todistusService.truncate()
      app.todistusScheduler.resume()
    }
}
