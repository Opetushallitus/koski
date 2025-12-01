package fi.oph.koski.todistus

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExamplesKielitutkinto
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{AuditLogTester, KoskiAuditLogMessageField, KoskiOperation}
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Opiskeluoikeus, Suoritus, YleisenKielitutkinnonSuoritus}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.net.URL
import java.security.MessageDigest
import java.time.{Duration, LocalDate, LocalDateTime}

class TodistusSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with PutOpiskeluoikeusTestMethods[KielitutkinnonOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[KielitutkinnonOpiskeluoikeus]]

  val app = KoskiApplicationForTests

  val vahvistettuKielitutkinnonOpiskeluoikeus = ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2011, 1, 3), "FI", "kt")
  val defaultOpiskeluoikeus = vahvistettuKielitutkinnonOpiskeluoikeus

  override protected def beforeAll(): Unit = {
    resetFixtures()
  }

  override protected def afterEach(): Unit = {
    Wait.until { !hasWork && !KoskiApplicationForTests.todistusScheduler.isRunning && !KoskiApplicationForTests.todistusCleanupScheduler.isRunning}
    app.todistusRepository.truncateForLocal()
    AuditLogTester.clearMessages
  }

  def hasWork: Boolean = {
    KoskiApplicationForTests.todistusRepository.numberOfMyRunningJobs > 0 || KoskiApplicationForTests.todistusRepository.numberOfQueuedJobs > 0
  }

  "Generointipyyntö ja statuspyyntö" - {
    "onnistuu kansalaiselta omasta kielitutkinnon opiskeluoikeudesta" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          getStatusSuccessfully(todistusJob.id, hetu) { status =>
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }

    "onnistuu kansalaiselta omasta kielitutkinnon opiskeluoikeudesta, joka on tallennettu kansalaisen toisella oppija-oidilla" in {
      val lang = "fi"
      // Master ja slave jakavat saman hetun, slave on linkitetty masteriin
      val masterHetu = KoskiSpecificMockOppijat.master.hetu.get
      val slaveOid = KoskiSpecificMockOppijat.slave.henkilö.oid

      // Luo opiskeluoikeus slave-oppijalle (linkitetty OID)
      val slaveOpiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.slave.henkilö, MockUsers.paakayttaja)
      val opiskeluoikeusOid = slaveOpiskeluoikeus.oid.get

      withoutRunningSchedulers {
        // Kirjaudu master-oppijana
        // Pyydä todistusta slave-oppijan opiskeluoikeudella
        val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

        addGenerateJobSuccessfully(req, masterHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob.oppijaOid should equal(slaveOid)
          todistusJob.opiskeluoikeusOid should equal(opiskeluoikeusOid)
        }
      }
    }

    "onnistuu huoltajalta huollettavan kielitutkinnon opiskeluoikeudesta" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val kirjautujanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        addGenerateJobSuccessfully(req, kirjautujanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          getStatusSuccessfully(todistusJob.id, kirjautujanHetu) { status =>
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }

    "Onnistuu virkailijapääkäyttäjältä kielitutkinnon opiskeluoikeuteen" in {
      val lang = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        addGenerateJobSuccessfullyAsVirkailijaPääkäyttäjä(req) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob.oppijaOid should equal(oppijaOid)
          todistusJob.opiskeluoikeusOid should equal(opiskeluoikeusOid)
        }
      }
    }
  }

  "Generointipyyntö ei onnistu" - {
    "kansalaiselta muuntyyppisten opintojen opiskeluoikeuteen" in {
      val lang = "fi"
      val kirjautujanHetu = KoskiSpecificMockOppijat.lukiolainen.hetu.get
      val muidenOpintojenOpiskeluoikeus = getVahvistettuOpiskeluoikeus(KoskiSpecificMockOppijat.lukiolainen.oid)

      muidenOpintojenOpiskeluoikeus.get.isInstanceOf[KielitutkinnonOpiskeluoikeus] should be(false)

      val muidenOpintojenOpiskeluoikeusOid = muidenOpintojenOpiskeluoikeus.flatMap(_.oid).get

      val req = TodistusGenerateRequest(muidenOpintojenOpiskeluoikeusOid, lang)

      withoutRunningSchedulers {
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
      val opiskeluoikeusOid = oo.oid.get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        addGenerateJob(req, hetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "kansalaiselta toisen oppijan kielitutkinnon opiskeluoikeuteen" in {
      val lang = "fi"
      val kirjautujanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val toisenOpiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(toisenOpiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        addGenerateJob(req, kirjautujanHetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "huoltajalta toisen kansalaisen kielitutkinnon opiskeluoikeuteen" in {
      val lang = "fi"
      val huoltajaKirjautujanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val toisenOpiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(toisenOpiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        addGenerateJob(req, huoltajaKirjautujanHetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "Virkailijakäyttäjältä (ei pääkäyttäjä)" in {
      val lang = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Tavallinen virkailijä (ei pääkäyttäjä) ei pysty luomaan todistusta
        get(s"api/todistus/generate/${req.toPathParams}", headers = authHeaders(MockUsers.kalle) ++ jsonContent) {
          verifyResponseStatus(403)
        }
      }
    }
  }

  "Statuspyyntö onnistuu" - {

    "Huoltajalta huollettavan luomaan generointi-jobiin" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        addGenerateJobSuccessfully(req, huollettavanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          getStatus(todistusJob.id, huoltajanHetu) {
            verifyResponseStatus(200)
          }
        }
      }
    }

    "Virkailijapääkäyttäjältä kansalaisen itsensä luomaan generointi-jobiin" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Kansalainen luo todistuspyynnön
        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          // Virkailijapääkäyttäjä hakee statuksen
          getStatusSuccessfullyAsVirkailijaPääkäyttäjä(todistusJob.id) { status =>
            status.id should equal(todistusJob.id)
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }

    "Virkailijapääkäyttäjältä virkailijapääkäyttäjän luomaan generointi-jobiin" in {
      val lang = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Virkailijapääkäyttäjä luo todistuspyynnön
        addGenerateJobSuccessfullyAsVirkailijaPääkäyttäjä(req) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          // Virkailijapääkäyttäjä hakee statuksen
          getStatusSuccessfullyAsVirkailijaPääkäyttäjä(todistusJob.id) { status =>
            status.id should equal(todistusJob.id)
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }
    }
  }

  "Statuspyyntö ei onnistu" - {
    "oppijalta toisen oppijan luomaan generointi-jobiin" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val toisenKansalaisenHetu = KoskiSpecificMockOppijat.eskari.hetu.get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          getStatus(todistusJob.id, toisenKansalaisenHetu) {
            verifyResponseStatus(404)
          }
        }
      }
    }
  }

  "Huoltajan luoma todistus ja oppijan omat oikeudet" - {
    "Oppija pääsee omiin todistuksiin, vaikka huoltaja olisi luonut pyynnön" - {
      "Status by id onnistuu" in {
        val lang = "fi"
        val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
        val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
        val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

        val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

        withoutRunningSchedulers {
          // Huoltaja luo todistuspyynnön
          val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
            todistusJob.state should equal(TodistusState.QUEUED)
            todistusJob
          }

          // Huollettava itse yrittää hakea statuksen ID:llä (ERILLINEN HTTP-kutsu)
          getStatusSuccessfully(todistusJob.id, huollettavanHetu) { status =>
            status.id should equal(todistusJob.id)
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }

      "Status parametreilla onnistuu" in {
        val lang = "fi"
        val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
        val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
        val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

        val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

        withoutRunningSchedulers {
          // Huoltaja luo todistuspyynnön
          val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
            todistusJob.state should equal(TodistusState.QUEUED)
            todistusJob
          }

          // Huollettava itse yrittää hakea statuksen parametreilla (ERILLINEN HTTP-kutsu)
          checkStatusByParametersSuccessfully(req, huollettavanHetu) { status =>
            status.id should equal(todistusJob.id)
            status.state should equal(TodistusState.QUEUED)
          }
        }
      }

      "Download onnistuu kun todistus on valmis" in {
        val lang = "fi"
        val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
        val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
        val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
        val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

        val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

        // Huoltaja luo todistuspyynnön
        val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob
        }

        // Odotetaan todistuksen valmistumista (käyttäen huoltajan hetua pollaamaan)
        val completedJob = waitForCompletion(todistusJob.id, huoltajanHetu)
        completedJob.state should equal(TodistusState.COMPLETED)

        // Huollettava itse yrittää ladata todistuksen
        verifyDownloadResult(s"/todistus/download/${todistusJob.id}", huollettavanHetu)
      }
    }
  }

  "Allekirjoitetun todistuksen saa ladattua sen valmistuttua" in {
    def verifyYleinenKielitutkintoTodistusSisalto(pdfText: String): Unit = {
      // Tarkista, että todistuksen kaikki templatoidut merkkijonot löytyvät PDF:stä
      assert(pdfText.contains("Kielitutkinto Suorittaja"), "Oppijan nimi puuttuu")
      assert(pdfText.contains("1.1.2007"), "Oppijan syntymäaika puuttuu")
      assert(pdfText.contains("suomen kielen keskitason"), "Tutkinnon nimi puuttuu")
      assert(pdfText.contains("3-4"), "Tason arvosanarajat puuttuvat")
      assert(pdfText.contains("Varsinais-Suomen kansanopisto"), "Järjestäjän nimi puuttuu")
      assert(pdfText.contains("3.1.2011"), "Allekirjoituspäivämäärä puuttuu")
      assert(pdfText.contains("TEKSTIN YMMÄRTÄMINEN"), "Osasuoritus 'TEKSTIN YMMÄRTÄMINEN' puuttuu")
      assert(pdfText.contains("KIRJOITTAMINEN"), "Osasuoritus 'KIRJOITTAMINEN' puuttuu")
      assert(pdfText.contains("PUHEEN YMMÄRTÄMINEN"), "Osasuoritus 'PUHEEN YMMÄRTÄMINEN' puuttuu")
      assert(pdfText.contains("PUHUMINEN"), "Osasuoritus 'PUHUMINEN' puuttuu")
    }

    def verifyTodistusFontit(document: PDDocument): Unit = {
      import scala.jdk.CollectionConverters._

      val expectedFonts = Set("OpenSans-SemiBold", "OpenSans-Bold", "OpenSans-Regular")

      case class FontInfo(name: String, embedded: Boolean)

      val allFonts = document.getDocumentCatalog.getPages.asScala.flatMap { page =>
        Option(page.getResources).toSeq.flatMap { resources =>
          resources.getFontNames.asScala.map { fontName =>
            val font = resources.getFont(fontName)
            FontInfo(font.getName, font.isEmbedded)
          }
        }
      }.toList

      val uniqueFontNames = allFonts.map(_.name).toSet
      val nonEmbeddedFonts = allFonts.filterNot(_.embedded).map(_.name)

      uniqueFontNames.size should be > 0

      withClue(s"Löydettiin ${nonEmbeddedFonts.size} embeddaamatonta fonttia: ${nonEmbeddedFonts.mkString(", ")}. ") {
        nonEmbeddedFonts shouldBe empty
      }

      withClue(s"Odotettu ${expectedFonts.size} eri fonttia, löydettiin ${uniqueFontNames.size}. Löydetyt fontit: ${uniqueFontNames.mkString(", ")}. ") {
        uniqueFontNames.size should be(expectedFonts.size)
      }

      expectedFonts.foreach { expectedFont =>
        val found = uniqueFontNames.exists(_.contains(expectedFont))
        withClue(s"Odotettua fonttia '$expectedFont' ei löytynyt. Löydetyt fontit: ${uniqueFontNames.mkString(", ")}. ") {
          found should be(true)
        }
      }
    }

    def verifyTodistusSivumaara(document: PDDocument, expectedPages: Int): Unit = {
      val actualPages = document.getNumberOfPages
      withClue(s"PDF:n sivumäärä oli $actualPages, odotettu $expectedPages. ") {
        actualPages should be(expectedPages)
      }
    }

    def verifyTodistusMetadata(document: PDDocument, todistusJob: TodistusJob, opiskeluoikeus: KielitutkinnonOpiskeluoikeus): Unit = {
      val info = document.getDocumentInformation

      info.getCustomMetadataValue("OppijaOid") should equal(todistusJob.oppijaOid)
      info.getCustomMetadataValue("OpiskeluoikeusOid") should equal(todistusJob.opiskeluoikeusOid)
      info.getCustomMetadataValue("TodistusJobId") should equal(todistusJob.id)
      info.getCustomMetadataValue("OpiskeluoikeusVersionumero") should equal(opiskeluoikeus.versionumero.get.toString)

      val generointiStartedAt = info.getCustomMetadataValue("GenerointiStartedAt")
      withClue(s"GenerointiStartedAt-metadatassa virhe. Saatu: $generointiStartedAt ") {
        generointiStartedAt should not be null
        generointiStartedAt should not be empty
      }

      val expectedCommitHash = "unknown"
      info.getCustomMetadataValue("CommitHash") should equal(expectedCommitHash)

      val producer = info.getProducer
      withClue(s"Producer-kentässä virhe. Saatu: $producer ") {
        producer should include("Koski")
        producer should include("commit:")
        producer should include(expectedCommitHash)
      }
    }

    val lang = "fi"
    val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
    val expectedHash = laskeHenkilötiedotHash(oppija)
    val hetu = oppija.hetu.get
    val oppijaOid = oppija.oid
    val opiskeluoikeus = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid)
    val opiskeluoikeusOid = opiskeluoikeus.flatMap(_.oid).get

    val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

    val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
      todistusJob.state should equal(TodistusState.QUEUED)
      todistusJob
    }

    val completedJob = waitForCompletion(todistusJob.id, hetu)
    val completedJobFromDb = app.todistusRepository.getFromDbForUnitTests(completedJob.id).get

    completedJob.userOid should be(None)
    completedJobFromDb.userOid should be(Some(oppijaOid))

    completedJob.error should be(None)
    completedJobFromDb.error should be(None)

    completedJob.opiskeluoikeusVersionumero should be(opiskeluoikeus.flatMap(_.versionumero))
    completedJobFromDb.opiskeluoikeusVersionumero should be(opiskeluoikeus.flatMap(_.versionumero))

    completedJob.oppijaHenkilötiedotHash should be(Some(expectedHash))
    completedJobFromDb.oppijaHenkilötiedotHash should be(Some(expectedHash))

    completedJob.completedAt should not be empty
    completedJobFromDb.completedAt should not be empty

    completedJob.startedAt should not be empty
    completedJobFromDb.startedAt should not be empty

    completedJob.worker should be(None)
    completedJobFromDb.worker should be(Some("local"))

    // Testaa suora lataus (kansalainen)
    verifyDownloadResultAndContent(s"/todistus/download/${todistusJob.id}", hetu) {
      val pdfBytes = response.getContentBytes()

      val document = Loader.loadPDF(pdfBytes)

      // Kirjoita PDF temp-tiedostoon debuggausta varten
      val tempFile = java.nio.file.Files.createTempFile("todistus-test-", ".pdf")
      java.nio.file.Files.write(tempFile, pdfBytes)
      println(s"PDF kirjoitettu tiedostoon: ${tempFile.toAbsolutePath}")

      val pdfText = new PDFTextStripper().getText(document)
      verifyYleinenKielitutkintoTodistusSisalto(pdfText)

      val signerName = document.getLastSignatureDictionary.getName()
      signerName should be("TEST Signer")

      verifyTodistusFontit(document)
      verifyTodistusSivumaara(document, 2)
      verifyTodistusMetadata(document, completedJob, opiskeluoikeus.get)

      // Varmista että Content-Type on oikea
      response.header("Content-Type") should include("application/pdf")
      // Varmista että Content-Disposition on asetettu
      response.header("Content-Disposition") should include("attachment")
      response.header("Content-Disposition") should include("filename")
      // Varmista että tiedostonimi on lokalisoitu oikein (yki-todistus-fi.pdf)
      response.header("Content-Disposition") should include("yki-todistus-fi.pdf")

      document.close()
    }

    // Testaa presigned URL lataus (OPH-pääkäyttäjä)
    verifyPresignedResultAndContent(s"/todistus/download/presigned/${todistusJob.id}") {
      val pdfBytes = response.getContentBytes()

      val document = Loader.loadPDF(pdfBytes)
      val pdfText = new PDFTextStripper().getText(document)
      verifyYleinenKielitutkintoTodistusSisalto(pdfText)

      val signerName = document.getLastSignatureDictionary.getName()
      signerName should be("TEST Signer")

      verifyTodistusFontit(document)
      verifyTodistusSivumaara(document, 2)
      verifyTodistusMetadata(document, completedJob, opiskeluoikeus.get)

      // TODO: TOR-2400: validoi todistus jollain paikallisella validaattorilla, ja katso, että sisältää kaiken long-term -validointia tukevan

      // TODO: TOR-2400: tarkista, että pdf:n sisältö vastaa myös graafisesti renderöitynä odotettua (marginaalit jne.)
      document.close()
    }
  }

  "Mitätöidyn opiskeluoikeuden todistuksen lataus" - {
    "estyy jos opiskeluoikeus on mitätöity todistuksen luomisen jälkeen" in {
      val lang = "fi"
      val oppija = KoskiSpecificMockOppijat.eskari
      val hetu = oppija.hetu.get

      // Luo opiskeluoikeus ja todistus
      val oo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, oppija, MockUsers.paakayttaja)
      val opiskeluoikeusOid = oo.oid.get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Mitätöi opiskeluoikeus todistuksen luomisen jälkeen
      mitätöiOppijanKaikkiOpiskeluoikeudet(oppija)

      // Yritä ladata todistus - pitäisi epäonnistua
      getResult(s"/todistus/download/${todistusJob.id}", hetu) {
        verifyResponseStatus(503)
      }
    }
  }

  "Todistuksen latauksen käyttöoikeudet" - {
    "Kansalainen ei voi käyttää presigned URL endpointtiä" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Kansalainen yrittää käyttää presigned URL endpointtiä - pitäisi estää
      getResult(s"/todistus/download/presigned/${todistusJob.id}", hetu) {
        verifyResponseStatus(403) // Forbidden
      }

      // Varmista että normaali download toimii
      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", hetu)
    }

    "Kansalainen ei pääse lataamaan toisen oppijan todistusta" in {
      val lang = "fi"
      val todistuksenOmistajaHetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val todistuksenOmistajaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val toinenKansalainenHetu = KoskiSpecificMockOppijat.eskari.hetu.get

      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(todistuksenOmistajaOid).flatMap(_.oid).get
      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Todistuksen omistaja luo todistuksen
      val todistusJob = addGenerateJobSuccessfully(req, todistuksenOmistajaHetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, todistuksenOmistajaHetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", todistuksenOmistajaHetu)

      // Toinen kansalainen yrittää ladata todistuksen
      getResult(s"/todistus/download/${todistusJob.id}", toinenKansalainenHetu) {
        verifyResponseStatus(404)
      }
    }

    "Huoltaja ei pääse lataamaan muiden kuin huollettaviensa todistuksia" in {
      val lang = "fi"
      val todistuksenOmistajaHetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val todistuksenOmistajaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get

      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(todistuksenOmistajaOid).flatMap(_.oid).get
      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Todistuksen omistaja luo todistuksen
      val todistusJob = addGenerateJobSuccessfully(req, todistuksenOmistajaHetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, todistuksenOmistajaHetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Huoltaja (joka ei ole tämän oppijan huoltaja) yrittää ladata todistuksen
      getResult(s"/todistus/download/${todistusJob.id}", huoltajanHetu) {
        verifyResponseStatus(404)
      }
    }
  }

  "Todistuksen uudelleenkäyttö" - {
    "Palauttaa olemassaolevan COMPLETED-todistuksen, jos sama sisältö" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo ensimmäinen todistus ja odota sen valmistumista
      val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedFirstJob = waitForCompletion(firstJob.id, hetu)
      completedFirstJob.state should equal(TodistusState.COMPLETED)

      // Pyydä samaa todistusta uudestaan
      val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob
      }

      // Toisen pyynnön pitäisi palauttaa sama job
      secondJob.id should equal(firstJob.id)
      secondJob.state should equal(TodistusState.COMPLETED)
    }

    "Palauttaa olemassaolevan QUEUED-todistuksen, jos sama sisältö" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Luo ensimmäinen todistus (jää QUEUED-tilaan)
        val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob
        }

        // Pyydä samaa todistusta uudestaan
        val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob
        }

        // Toisen pyynnön pitäisi palauttaa sama job
        secondJob.id should equal(firstJob.id)
        secondJob.state should equal(TodistusState.QUEUED)
      }
    }

    "Luo uuden todistuksen, jos aiempi on ERROR-tilassa" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Luo ensimmäinen todistus
        val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob
        }

        // Merkitse se epäonnistuneeksi
        app.todistusRepository.setJobFailed(firstJob.id, "Testissä luotu virhe")
        val errorJob = app.todistusRepository.getFromDbForUnitTests(firstJob.id).get
        errorJob.state should equal(TodistusState.ERROR)

        // Pyydä samaa todistusta uudestaan
        val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob
        }

        // Toisen pyynnön pitäisi luoda uusi job
        secondJob.id should not equal firstJob.id
        secondJob.state should equal(TodistusState.QUEUED)
      }
    }

    "Luo uuden todistuksen, jos opiskeluoikeus-versionumero on muuttunut" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo ensimmäinen todistus ja odota sen valmistumista
      val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedFirstJob = waitForCompletion(firstJob.id, hetu)
      completedFirstJob.state should equal(TodistusState.COMPLETED)

      // Päivitä opiskeluoikeutta, jolloin versionumero kasvaa
      val oo = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).get
      val paivitettyOo = oo.copy(
        tila = oo.tila.copy(opiskeluoikeusjaksot = oo.tila.opiskeluoikeusjaksot.map(
          j => j.copy(alku = j.alku.plusDays(1))
        )),
        suoritukset = oo.suoritukset.map(s => {
          val ks = s.asInstanceOf[YleisenKielitutkinnonSuoritus]
          ks.copy(vahvistus = ks.vahvistus.map(_.copy(päivä = ks.vahvistus.map(_.päivä).map(_.plusDays(1)).get)))
        })
      )
      putOpiskeluoikeus(paivitettyOo, henkilö = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      // Pyydä todistusta uudestaan
      val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob
      }

      // Toisen pyynnön pitäisi luoda uusi job eri versionumerolla
      secondJob.id should not equal firstJob.id
      val completedSecondJob = waitForCompletion(secondJob.id, hetu)
      completedSecondJob.state should equal(TodistusState.COMPLETED)
    }

    "Luo uuden todistuksen, jos aiempi on EXPIRED-tilassa" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo ensimmäinen todistus ja odota sen valmistumista
      val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedFirstJob = waitForCompletion(firstJob.id, hetu)
      completedFirstJob.state should equal(TodistusState.COMPLETED)

      // Merkitse todistus EXPIRED-tilaan (simuloi vanhenemisprosessia)
      app.todistusRepository.setJobExpiredForUnitTests(firstJob.id)
      val expiredJobFromDb = app.todistusRepository.getFromDbForUnitTests(firstJob.id).get
      expiredJobFromDb.state should equal(TodistusState.EXPIRED)

      // Pyydä samaa todistusta uudestaan
      val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob
      }

      // Toisen pyynnön pitäisi luoda uusi job, koska EXPIRED-tilassa olevaa ei uudelleenkäytetä
      secondJob.id should not equal firstJob.id
      secondJob.state should equal(TodistusState.QUEUED)
    }

    "Luo uuden todistuksen, jos aiempi on QUEUED_FOR_EXPIRE-tilassa" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo ensimmäinen todistus ja odota sen valmistumista
      val firstJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedFirstJob = waitForCompletion(firstJob.id, hetu)
      completedFirstJob.state should equal(TodistusState.COMPLETED)

      // Merkitse todistus QUEUED_FOR_EXPIRE-tilaan (simuloi vanhenemisjonoon merkitsemistä)
      app.todistusRepository.setJobQueuedForExpireForUnitTests(firstJob.id)
      val queuedForExpireJobFromDb = app.todistusRepository.getFromDbForUnitTests(firstJob.id).get
      queuedForExpireJobFromDb.state should equal(TodistusState.QUEUED_FOR_EXPIRE)

      // Pyydä samaa todistusta uudestaan
      val secondJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob
      }

      // Toisen pyynnön pitäisi luoda uusi job, koska QUEUED_FOR_EXPIRE-tilassa olevaa ei uudelleenkäytetä
      secondJob.id should not equal firstJob.id
      secondJob.state should equal(TodistusState.QUEUED)
    }
  }

  "Status-endpoint" - {
    "Palauttaa ajantasaisen COMPLETED-todistuksen jos saatavilla" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo todistus ja odota sen valmistumista
      val job = addGenerateJobSuccessfully(req, hetu) { todistusJob => todistusJob }
      val completedJob = waitForCompletion(job.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Kutsu status-endpointia parametreilla
      checkStatusByParametersSuccessfully(req, hetu) { statusJob =>
        statusJob.id should equal(completedJob.id)
        statusJob.state should equal(TodistusState.COMPLETED)
      }
    }

    "Palauttaa ajantasaisen QUEUED-todistuksen jos saatavilla" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Luo todistus joka jää QUEUED-tilaan
        val job = addGenerateJobSuccessfully(req, hetu) { todistusJob => todistusJob }
        job.state should equal(TodistusState.QUEUED)

        // Kutsu status-endpointia parametreilla
        checkStatusByParametersSuccessfully(req, hetu) { statusJob =>
          statusJob.id should equal(job.id)
          statusJob.state should equal(TodistusState.QUEUED)
        }
      }
    }

    "Palauttaa 404 jos ei ajantasaista todistusta" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Kutsu status-endpointia kun ei todistusta olemassa
      checkStatusByParameters(req, hetu) {
        verifyResponseStatus(404)
      }
    }

    "Palauttaa 404 jos vain ERROR-tilassa olevia todistuksia" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        // Luo todistus ja merkitse se epäonnistuneeksi
        val job = addGenerateJobSuccessfully(req, hetu) { todistusJob => todistusJob }
        app.todistusRepository.setJobFailed(job.id, "Testi-virhe")

        // Kutsu status-endpointia
        checkStatusByParameters(req, hetu) {
          verifyResponseStatus(404)
        }
      }
    }

    "Palauttaa 404 jos versionumero on muuttunut" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo todistus ja odota sen valmistumista
      val job = addGenerateJobSuccessfully(req, hetu) { todistusJob => todistusJob }
      val completedJob = waitForCompletion(job.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Päivitä opiskeluoikeutta, jolloin versionumero kasvaa
      val oo = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).get
      val paivitettyOo = oo.copy(
        tila = oo.tila.copy(opiskeluoikeusjaksot = oo.tila.opiskeluoikeusjaksot.map(
          j => j.copy(alku = j.alku.plusDays(1))
        )),
        suoritukset = oo.suoritukset.map(s => {
          val ks = s.asInstanceOf[YleisenKielitutkinnonSuoritus]
          ks.copy(vahvistus = ks.vahvistus.map(_.copy(päivä = ks.vahvistus.map(_.päivä).map(_.plusDays(1)).get)))
        })
      )
      putOpiskeluoikeus(paivitettyOo, henkilö = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja, headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }

      // Kutsu status-endpointia - pitäisi palauttaa 404 koska versionumero muuttunut
      checkStatusByParameters(req, hetu) {
        verifyResponseStatus(404)
      }
    }
  }


  "markAllMyJobsInterrupted merkitsee jobin keskeytetyksi" - {
    val lang = "fi"
    val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
    val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

    withoutRunningSchedulers {
      // Luo job joka on käynnissä tällä workerilla
      val activeWorkerId = app.todistusRepository.workerId
      val job = TodistusJob(
        id = java.util.UUID.randomUUID().toString,
        userOid = Some(oppijaOid),
        oppijaOid = oppijaOid,
        opiskeluoikeusOid = opiskeluoikeusOid,
        language = lang,
        opiskeluoikeusVersionumero = Some(1),
        oppijaHenkilötiedotHash = Some("test-hash"),
        state = TodistusState.STAMPING_PDF,
        createdAt = LocalDateTime.now(),
        startedAt = Some(LocalDateTime.now()),
        completedAt = None,
        worker = Some(activeWorkerId),
        attempts = Some(1),
        error = None
      )
      app.todistusRepository.addRawForUnitTests(job)

      // Merkitse kaikki tämän workerin jobit keskeytetyiksi
      app.todistusService.markAllMyJobsInterrupted()

      // Varmista että job on nyt INTERRUPTED tilassa
      val interruptedJob = app.todistusRepository.getFromDbForUnitTests(job.id).get
      interruptedJob.state should equal(TodistusState.INTERRUPTED)
      interruptedJob.worker should equal(Some(activeWorkerId))

      job // Palauta job jotta voidaan käyttää lohkon ulkopuolella
    }
  }

  "Todistusten vanheneminen" - {
    "Merkitsee vanhentuneet todistukset QUEUED_FOR_EXPIRE-tilaan" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo todistus ja odota sen valmistumista
      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Merkitse todistus vanhaksi muuttamalla completed_at-aikaleimaa tietokannassa
      val expirationDuration = app.config.getDuration("todistus.expirationDuration")
      val oldCompletedAt = LocalDateTime.now().minusSeconds(expirationDuration.getSeconds).minusSeconds(1)
      app.todistusRepository.setCompletedAtForUnitTests(todistusJob.id, oldCompletedAt)

      // Odota että cleanup-scheduler käsittelee vanhentuneen todistuksen
      Thread.sleep(3000) // cleanupInterval on 2s

      // Varmista että todistus on nyt QUEUED_FOR_EXPIRE-tilassa
      val expiredJob = app.todistusRepository.getFromDbForUnitTests(todistusJob.id).get
      expiredJob.state should equal(TodistusState.QUEUED_FOR_EXPIRE)
    }

    "Ei merkitse QUEUED_FOR_EXPIRE-tilaan, jos todistus ei ole vielä vanhentunut" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      // Luo todistus ja odota sen valmistumista
      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }
      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      // Odota cleanup-schedulerin käynnistymistä
      Thread.sleep(3000) // cleanupInterval on 2s

      // Varmista että todistus on edelleen COMPLETED-tilassa (ei vanhentunut)
      val stillValidJob = app.todistusRepository.getFromDbForUnitTests(todistusJob.id).get
      stillValidJob.state should equal(TodistusState.COMPLETED)
    }

    "Merkitsee useita vanhentuneita todistuksia kerralla" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      // Luo useita todistuksia eri kielillä
      val reqFi = TodistusGenerateRequest(opiskeluoikeusOid, "fi")
      val reqSv = TodistusGenerateRequest(opiskeluoikeusOid, "sv")
      val reqEn = TodistusGenerateRequest(opiskeluoikeusOid, "en")

      val jobFi = addGenerateJobSuccessfully(reqFi, hetu) { todistusJob => todistusJob }
      val completedFi = waitForCompletion(jobFi.id, hetu)

      val jobSv = addGenerateJobSuccessfully(reqSv, hetu) { todistusJob => todistusJob }
      val completedSv = waitForCompletion(jobSv.id, hetu)

      val jobEn = addGenerateJobSuccessfully(reqEn, hetu) { todistusJob => todistusJob }
      val completedEn = waitForCompletion(jobEn.id, hetu)

      // Merkitse kaikki todistukset vanhoiksi
      val expirationDuration = app.config.getDuration("todistus.expirationDuration")
      val oldCompletedAt = LocalDateTime.now().minusSeconds(expirationDuration.getSeconds).minusSeconds(1)

      Seq(jobFi.id, jobSv.id, jobEn.id).foreach { id =>
        app.todistusRepository.setCompletedAtForUnitTests(id, oldCompletedAt)
      }

      // Odota cleanup-schedulerin käynnistymistä
      Thread.sleep(3000)

      // Varmista että kaikki todistukset ovat QUEUED_FOR_EXPIRE-tilassa
      val expiredFi = app.todistusRepository.getFromDbForUnitTests(jobFi.id).get
      val expiredSv = app.todistusRepository.getFromDbForUnitTests(jobSv.id).get
      val expiredEn = app.todistusRepository.getFromDbForUnitTests(jobEn.id).get

      expiredFi.state should equal(TodistusState.QUEUED_FOR_EXPIRE)
      expiredSv.state should equal(TodistusState.QUEUED_FOR_EXPIRE)
      expiredEn.state should equal(TodistusState.QUEUED_FOR_EXPIRE)
    }
  }

  "Orpojen todistusjobien uudelleenkäynnistys" - {

    "Ei koske aktiivisesti ajossa olevaan jobiin" in {
      val lang = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      // Luo job oikealla workerIdllä (tämän instanssin workerId)
      val activeWorkerId = app.todistusRepository.workerId
      val activeJob = TodistusJob(
        id = java.util.UUID.randomUUID().toString,
        userOid = Some(oppijaOid),
        oppijaOid = oppijaOid,
        opiskeluoikeusOid = opiskeluoikeusOid,
        language = lang,
        opiskeluoikeusVersionumero = Some(1),
        oppijaHenkilötiedotHash = Some("test-hash"),
        state = TodistusState.GENERATING_RAW_PDF,
        createdAt = LocalDateTime.now(),
        startedAt = Some(LocalDateTime.now()),
        completedAt = None,
        worker = Some(activeWorkerId),
        attempts = Some(1),
        error = None
      )
      app.todistusRepository.addRawForUnitTests(activeJob)

      try {
        // Odota cleanup-schedulerin käynnistymistä
        Thread.sleep(3000) // cleanupInterval on 2s

        // Varmista että jobin tila ei ole muuttunut
        val jobAfterCleanup = app.todistusRepository.getFromDbForUnitTests(activeJob.id).get
        jobAfterCleanup.state should equal(TodistusState.GENERATING_RAW_PDF)
        jobAfterCleanup.worker should equal(Some(activeWorkerId))
        jobAfterCleanup.attempts should equal(Some(1)) // Ei ole kasvanut
      } finally {
        // Siivoa: merkitse job valmiiksi jotta afterEach ei jää odottamaan
        app.todistusRepository.updateState(activeJob.id, TodistusState.GENERATING_RAW_PDF, TodistusState.COMPLETED)
      }
    }

    "Uudelleenkäynnistää orpo-jobin (attempts < 3) ja ajaa sen loppuun" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val orphanJob = createOrphanJob(oppijaOid, opiskeluoikeusOid, lang, attempts = 1)

      val completedJob = waitForCompletion(orphanJob.id, hetu)
      val completedJobFromDb = app.todistusRepository.getFromDbForUnitTests(orphanJob.id).get

      completedJob.state should equal(TodistusState.COMPLETED)
      completedJobFromDb.attempts.get should be > orphanJob.attempts.get
      completedJob.error should be(None)
    }


    "Uudelleenkäynnistää INTERRUPTED-tilaan merkityn jobin" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val runningJob = withoutRunningSchedulers(truncate = false) {
        // Luo job joka on käynnissä toisella workerilla
        val activeWorkerId = app.todistusRepository.workerId
        val job = TodistusJob(
          id = java.util.UUID.randomUUID().toString,
          userOid = Some(oppijaOid),
          oppijaOid = oppijaOid,
          opiskeluoikeusOid = opiskeluoikeusOid,
          language = lang,
          opiskeluoikeusVersionumero = Some(1),
          oppijaHenkilötiedotHash = Some("test-hash"),
          state = TodistusState.INTERRUPTED,
          createdAt = LocalDateTime.now(),
          startedAt = Some(LocalDateTime.now()),
          completedAt = None,
          worker = Some("dummy-other-worker"),
          attempts = Some(1),
          error = None
        )
        app.todistusRepository.addRawForUnitTests(job)

        // Varmista että job on nyt INTERRUPTED tilassa
        val interruptedJob = app.todistusRepository.getFromDbForUnitTests(job.id).get
        interruptedJob.state should equal(TodistusState.INTERRUPTED)

        job // Palauta job jotta voidaan käyttää lohkon ulkopuolella
      }

      // Cleanup-scheduler käsittelee INTERRUPTED-jobin ja uudelleenkäynnistää sen
      val completedJob = waitForCompletionSkipStateChecks(runningJob.id, hetu)
      val completedJobFromDb = app.todistusRepository.getFromDbForUnitTests(runningJob.id).get

      // Varmista että job valmistui onnistuneesti
      completedJob.state should equal(TodistusState.COMPLETED)
      completedJobFromDb.attempts.get should be > runningJob.attempts.get
      completedJob.error should be(None)
    }

    "Siirtää orpo-jobin (attempts >= 3) ERROR-tilaan" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val orphanJob = createOrphanJob(oppijaOid, opiskeluoikeusOid, lang, attempts = 3)

      val errorJob = waitForError(orphanJob.id, hetu)
      val errorJobFromDb = app.todistusRepository.getFromDbForUnitTests(orphanJob.id).get

      errorJob.state should equal(TodistusState.ERROR)
      errorJobFromDb.error should not be empty
      errorJobFromDb.error.get should include("epäonnistui 3 yrityksen jälkeen")
    }

    "Käsittelee sekä uudelleenkäynnistettävät että ERROR-tilaan siirrettävät orpo-jobit oikein" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val restartableOrphan = createOrphanJob(oppijaOid, opiskeluoikeusOid, lang, attempts = 1)
      val failableOrphan = createOrphanJob(oppijaOid, opiskeluoikeusOid, lang, attempts = 3)

      val completedJob = waitForCompletion(restartableOrphan.id, hetu)
      val errorJob = waitForError(failableOrphan.id, hetu)

      val completedJobFromDb = app.todistusRepository.getFromDbForUnitTests(restartableOrphan.id).get
      val errorJobFromDb = app.todistusRepository.getFromDbForUnitTests(failableOrphan.id).get

      completedJob.state should equal(TodistusState.COMPLETED)
      completedJobFromDb.attempts.get should be > restartableOrphan.attempts.get
      errorJob.state should equal(TodistusState.ERROR)
      errorJobFromDb.error should not be empty
    }
  }

  "HTML preview endpoint" - {
    "onnistuu OPH-pääkäyttäjältä" in {
      val lang = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      get(s"todistus/preview/$lang/$opiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatusOk()
        response.header("Content-Type") should include("text/html")

        val html = response.body

        // Tarkista että HTML sisältää oikeat tiedot
        html should include("Kielitutkinto Suorittaja")
        html should include("1.1.2007") // syntymäaika
        html should include("suomen kielen keskitason")
        html should include("Varsinais-Suomen kansanopisto")
        html should include("Tekstin ymmärtäminen")
        html should include("Kirjoittaminen")
        html should include("Puheen ymmärtäminen")
        html should include("Puhuminen")
      }
    }

    "ei onnistu kansalaiselta" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      get(s"todistus/preview/$lang/$opiskeluoikeusOid", headers = kansalainenLoginHeaders(hetu)) {
        verifyResponseStatus(403) // Forbidden
      }
    }

    "ei onnistu tavalliselta virkailijalta (ei pääkäyttäjä)" in {
      val lang = "fi"
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      get(s"todistus/preview/$lang/$opiskeluoikeusOid", headers = authHeaders(MockUsers.kalle)) {
        verifyResponseStatus(403) // Forbidden
      }
    }

    "palauttaa 404 jos opiskeluoikeus ei ole kielitutkinto" in {
      val lang = "fi"
      val muidenOpintojenOpiskeluoikeus = getVahvistettuOpiskeluoikeus(KoskiSpecificMockOppijat.lukiolainen.oid)
      val muidenOpintojenOpiskeluoikeusOid = muidenOpintojenOpiskeluoikeus.flatMap(_.oid).get

      get(s"todistus/preview/$lang/$muidenOpintojenOpiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatus(404)
      }
    }

    "palauttaa 404 jos opiskeluoikeus on mitätöity" in {
      val lang = "fi"
      val oo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.eskari)
      val opiskeluoikeusOid = oo.oid.get

      get(s"todistus/preview/$lang/$opiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatus(404)
      }
    }
  }

  "Audit-lokitukset" - {
    "TODISTUKSEN_LUONTI lokitetaan kun kansalainen luo todistuksen" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        AuditLogTester.clearMessages

        addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          AuditLogTester.verifyLastAuditLogMessage(Map(
            "operation" -> KoskiOperation.TODISTUKSEN_LUONTI.toString,
            "target" -> Map(
              KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid,
              KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> opiskeluoikeusOid,
              KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
            )
          ))
        }
      }
    }

    "TODISTUKSEN_LUONTI lokitetaan kun huoltaja luo todistuksen huollettavalle" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOid = KoskiSpecificMockOppijat.eskari.oid
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        AuditLogTester.clearMessages

        addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)

          AuditLogTester.verifyLastAuditLogMessage(Map(
            "operation" -> KoskiOperation.TODISTUKSEN_LUONTI.toString,
            "target" -> Map(
              KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> huollettavanOid,
              KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> huollettavanOpiskeluoikeusOid,
              KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
            )
          ))
        }
      }
    }

    "TODISTUKSEN_LATAAMINEN lokitetaan kun kansalainen lataa valmiin todistuksen" in {
      val lang = "fi"
      val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
      val hetu = oppija.hetu.get
      val oppijaOid = oppija.oid
      val opiskeluoikeus = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid)
      val opiskeluoikeusOid = opiskeluoikeus.flatMap(_.oid).get
      val opiskeluoikeusVersionumero = opiskeluoikeus.flatMap(_.versionumero).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, hetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      AuditLogTester.clearMessages

      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", hetu)

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> KoskiOperation.TODISTUKSEN_LATAAMINEN.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> opiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> opiskeluoikeusVersionumero.toString,
          KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
        )
      ))
    }

    "TODISTUKSEN_LATAAMINEN lokitetaan kun huoltaja lataa huollettavan todistuksen" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanOid = KoskiSpecificMockOppijat.eskari.oid
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get
      val huollettavanOpiskeluoikeusVersionumero = huollettavanOo.versionumero.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

      val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, huoltajanHetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      AuditLogTester.clearMessages

      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", huoltajanHetu)

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> KoskiOperation.TODISTUKSEN_LATAAMINEN.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> huollettavanOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> huollettavanOpiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> huollettavanOpiskeluoikeusVersionumero.toString,
          KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
        )
      ))
    }

    "TODISTUKSEN_LATAAMINEN lokitetaan kun oppija lataa huoltajan luoman todistuksen" in {
      val lang = "fi"
      val huollettavanOo = setupOppijaWithAndGetOpiskeluoikeus(vahvistettuKielitutkinnonOpiskeluoikeus, KoskiSpecificMockOppijat.eskari, MockUsers.paakayttaja)
      val huoltajanHetu = KoskiSpecificMockOppijat.faija.hetu.get
      val huollettavanHetu = KoskiSpecificMockOppijat.eskari.hetu.get
      val huollettavanOid = KoskiSpecificMockOppijat.eskari.oid
      val huollettavanOpiskeluoikeusOid = huollettavanOo.oid.get
      val huollettavanOpiskeluoikeusVersionumero = huollettavanOo.versionumero.get

      val req = TodistusGenerateRequest(huollettavanOpiskeluoikeusOid, lang)

      // Huoltaja luo todistuspyynnön
      val todistusJob = addGenerateJobSuccessfully(req, huoltajanHetu) { todistusJob =>
        todistusJob.state should equal(TodistusState.QUEUED)
        todistusJob
      }

      val completedJob = waitForCompletion(todistusJob.id, huoltajanHetu)
      completedJob.state should equal(TodistusState.COMPLETED)

      AuditLogTester.clearMessages

      // Huollettava itse lataa todistuksen
      verifyDownloadResult(s"/todistus/download/${todistusJob.id}", huollettavanHetu)

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> KoskiOperation.TODISTUKSEN_LATAAMINEN.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> huollettavanOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> huollettavanOpiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> huollettavanOpiskeluoikeusVersionumero.toString,
          KoskiAuditLogMessageField.todistusId.toString -> todistusJob.id
        )
      ))
    }

    "TODISTUKSEN_LATAAMINEN ei lokiteta kun todistus ei ole valmis" in {
      val lang = "fi"
      val hetu = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.hetu.get
      val oppijaOid = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja.oid
      val opiskeluoikeusOid = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).flatMap(_.oid).get

      val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)

      withoutRunningSchedulers {
        val todistusJob = addGenerateJobSuccessfully(req, hetu) { todistusJob =>
          todistusJob.state should equal(TodistusState.QUEUED)
          todistusJob
        }

        AuditLogTester.clearMessages

        getResult(s"/todistus/download/${todistusJob.id}", hetu) {
          verifyResponseStatus(503)
        }

        // Vain KANSALAINEN_LOGIN-loki pitäisi löytyä, ei TODISTUKSEN_LATAAMINEN-lokia
        val logMessages = AuditLogTester.getLogMessages
        logMessages.length should equal(1)
        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> KoskiOperation.KANSALAINEN_LOGIN.toString
        ))
      }
    }

    "TODISTUKSEN_ESIKATSELU lokitetaan kun OPH-pääkäyttäjä katsoo todistuksen esikatselua" in {
      val lang = "fi"
      val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
      val oppijaOid = oppija.oid
      val opiskeluoikeus = getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid)
      val opiskeluoikeusOid = opiskeluoikeus.flatMap(_.oid).get
      val opiskeluoikeusVersionumero = opiskeluoikeus.flatMap(_.versionumero).get

      AuditLogTester.clearMessages

      get(s"todistus/preview/$lang/$opiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
        verifyResponseStatusOk()
      }

      AuditLogTester.verifyLastAuditLogMessage(Map(
        "operation" -> KoskiOperation.TODISTUKSEN_ESIKATSELU.toString,
        "target" -> Map(
          KoskiAuditLogMessageField.oppijaHenkiloOid.toString -> oppijaOid,
          KoskiAuditLogMessageField.opiskeluoikeusOid.toString -> opiskeluoikeusOid,
          KoskiAuditLogMessageField.opiskeluoikeusVersio.toString -> opiskeluoikeusVersionumero.toString
        )
      ))
    }
  }

  // TODO: TOR-2400 puuttuvia toteutuksia ja testejä:
  //  - töiden cancelointi toimii (tarvitaanko tätä edes?)
  //  - debug-rajapinta vain pääkäyttäjille: voi hakea myös allekirjoittamattoman todistuksen. Tarvitaanko tätä?
  //  - Todistuksen ulkonäkö säilyy samana, eli marginaalit, fontit jne. eivät muutu (joku kuvavertailu tarvitaan?)
  //  - pääkäyttäjälle palautetaan tekniset tiedot (S3-osoitteet, virheilmoitukset jne.)

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

  def addGenerateJobAsVirkailijaPääkäyttäjä[T](req: TodistusGenerateRequest)(f: => T): T =
    get(s"api/todistus/generate/${req.toPathParams}", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent)(f)

  def addGenerateJobSuccessfullyAsVirkailijaPääkäyttäjä[T](req: TodistusGenerateRequest)(f: TodistusJob => T): T = {
    addGenerateJobAsVirkailijaPääkäyttäjä(req) {
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

  def getStatusAsVirkailijaPääkäyttäjä[T](id: String)(f: => T): T =
    get(s"api/todistus/status/$id", headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent)(f)

  def getStatusSuccessfullyAsVirkailijaPääkäyttäjä[T](id: String)(f: TodistusJob => T): T = {
    getStatusAsVirkailijaPääkäyttäjä(id) {
      f(parsedResponse)
    }
  }

  def checkStatusByParameters[T](req: TodistusGenerateRequest, hetu: String)(f: => T): T =
    get(s"api/todistus/status/${req.toPathParams}", headers = kansalainenLoginHeaders(hetu) ++ jsonContent)(f)

  def checkStatusByParametersSuccessfully[T](req: TodistusGenerateRequest, hetu: String)(f: TodistusJob => T): T = {
    checkStatusByParameters(req, hetu) {
      f(parsedResponse)
    }
  }

  def getResult[T](url: String, hetu: String)(f: => T): T = {
    val rootUrl = KoskiApplicationForTests.config.getString("koski.root.url")
    get(url.replace(rootUrl, ""), headers = kansalainenLoginHeaders(hetu))(f)
  }

  def verifyPresignedResult(url: String): Unit =
    verifyPresignedResultAndContent(url) {}

  def verifyPresignedResultAndContent[T](url: String)(f: => T): T = {
    val location = new URL(get(url, headers = authHeaders(MockUsers.paakayttaja)) {
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

  def verifyDownloadResult(url: String, hetu: String): Unit =
    verifyDownloadResultAndContent(url, hetu) {}

  def verifyDownloadResultAndContent[T](url: String, hetu: String)(f: => T): T = {
    getResult(url, hetu) {
      verifyResponseStatusOk()
      f
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
      TodistusState.GATHERING_INPUT,
      TodistusState.GENERATING_RAW_PDF,
      TodistusState.SAVING_RAW_PDF,
      TodistusState.STAMPING_PDF,
      TodistusState.SAVING_STAMPED_PDF,
      TodistusState.COMPLETED
    )

  def waitForCompletionSkipStateChecks(id:String, hetu: String): TodistusJob = {
    var lastResponse: Option[TodistusJob] = None

    Wait.until {
      getStatusSuccessfully(id, hetu) { response =>
        lastResponse = Some(response)
        response.state == TodistusState.COMPLETED
      }
    }
    lastResponse.get
  }

  def waitForError(id: String, hetu: String): TodistusJob = {
    var lastResponse: Option[TodistusJob] = None
    Wait.until {
      getStatusSuccessfully(id, hetu) { response =>
        lastResponse = Some(response)
        response.state == TodistusState.ERROR
      }
    }
    lastResponse.get
  }

  def createOrphanJob(oppijaOid: String, opiskeluoikeusOid: String, language: String, attempts: Int): TodistusJob = {
    val orphanJob = TodistusJob(
      id = java.util.UUID.randomUUID().toString,
      userOid = Some(oppijaOid),
      oppijaOid = oppijaOid,
      opiskeluoikeusOid = opiskeluoikeusOid,
      language = language,
      opiskeluoikeusVersionumero = Some(1),
      oppijaHenkilötiedotHash = Some("test-hash"),
      state = TodistusState.GENERATING_RAW_PDF,
      createdAt = LocalDateTime.now(),
      startedAt = Some(LocalDateTime.now()),
      completedAt = None,
      worker = Some("dead-worker-id"),
      attempts = Some(attempts),
      error = None
    )
    app.todistusRepository.addRawForUnitTests(orphanJob)
  }

  def parsedResponse: TodistusJob = {
    verifyResponseStatusOk()
    val json = JsonMethods.parse(response.body)
    val result = KoskiApplicationForTests.validatingAndResolvingExtractor.extract[TodistusJob](json, strictDeserialization)
    result should not be Left
    result.right.get
  }

  def withoutRunningSchedulers[T](f: => T): T = withoutRunningSchedulers(true)(f)

  def withoutRunningSchedulers[T](truncate: Boolean)(f: => T): T =
    try {
      Wait.until { !hasWork }

      app.todistusScheduler.pause(Duration.ofDays(1))
      app.todistusCleanupScheduler.pause(Duration.ofDays(1))

      Wait.until { !KoskiApplicationForTests.todistusScheduler.isRunning && !KoskiApplicationForTests.todistusCleanupScheduler.isRunning}
      f
    } finally {
      if (truncate) {
        app.todistusRepository.truncateForLocal()
      }
      app.todistusScheduler.resume()
      app.todistusCleanupScheduler.resume()
    }

  private def laskeHenkilötiedotHash(henkilö: OppijaHenkilö): String = {
    val data = s"${henkilö.etunimet}|${henkilö.sukunimi}|${henkilö.syntymäaika.getOrElse("")}"
    val digest = MessageDigest.getInstance("SHA-256")
    digest.digest(data.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
