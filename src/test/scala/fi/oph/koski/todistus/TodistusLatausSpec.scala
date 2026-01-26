package fi.oph.koski.todistus

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Opiskeluoikeus, YleisenKielitutkinnonOsakokeenSuoritus}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.{PDFTextStripper, PDFTextStripperByArea}
import org.json4s.jackson.JsonMethods
import org.verapdf.core.VeraPDFException
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider
import org.verapdf.pdfa.flavours.PDFAFlavour
import org.verapdf.pdfa.{Foundries, PDFAParser, PDFAValidator}

import java.awt.Rectangle
import scala.jdk.CollectionConverters._

class TodistusLatausSpec extends TodistusSpecHelpers {
  "Allekirjoitetun todistuksen saa ladattua sen valmistuttua ja se sisältää oikeat tiedot" in {
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

      info.getTitle should equal("Todistus")
      info.getSubject should equal("Todistus yleisen kielitutkinnon suorittamisesta")
      info.getAuthor should equal("Opetushallitus")
      // TODO: TOR-2400: Keywords ei toimi Acrobat:ssa, vaikka tässä toimii, ja samoin esim. Chromen PDF viewerissä.
      // Jotenkin littyy siihen, miten/mihin keywordsit PDF:ssä on tallennettu.
      info.getKeywords should equal("todistus, yleinen kielitutkinto")

      val generointiStartedAt = info.getCustomMetadataValue("GenerointiStartedAt")
      withClue(s"GenerointiStartedAt-metadatassa virhe. Saatu: $generointiStartedAt ") {
        generointiStartedAt should not be null
        generointiStartedAt should not be empty
      }

      val expectedCommitHash = "local"
      val commitHash = info.getCustomMetadataValue("CommitHash")
      withClue(s"CommitHash-metadatassa virhe. Saatu: $commitHash ") {
        commitHash should (equal(expectedCommitHash) or fullyMatch regex "[0-9a-f]{7,40}")
      }

      val producer = info.getProducer
      withClue(s"Producer-kentässä virhe. Saatu: $producer ") {
        producer should include("Koski")
        producer should include("commit:")
        producer should include(commitHash)
      }

      val opiskeluoikeusJson = info.getCustomMetadataValue("OpiskeluoikeusJson")
      withClue(s"OpiskeluoikeusJson-metadatassa virhe. Saatu: $opiskeluoikeusJson ") {
        opiskeluoikeusJson should not be null
        opiskeluoikeusJson should not be empty

        // Varmista että JSON on validi ja deserialisoituu takaisin Opiskeluoikeus-objektiksi
        val parsedJson = JsonMethods.parse(opiskeluoikeusJson)
        val deserializedOpiskeluoikeus = KoskiApplicationForTests.validatingAndResolvingExtractor.extract[Opiskeluoikeus](parsedJson, strictDeserialization)

        deserializedOpiskeluoikeus match {
          case Right(extractedOpiskeluoikeus) =>
            extractedOpiskeluoikeus.oid should equal(opiskeluoikeus.oid)
            extractedOpiskeluoikeus.versionumero should equal(opiskeluoikeus.versionumero)
            extractedOpiskeluoikeus.suoritukset.flatMap(_.osasuoritusLista).foreach {
              case os: YleisenKielitutkinnonOsakokeenSuoritus =>
                os.arviointi.toList.flatten.size should be(1)
              case _ =>
                fail()
            }
          case Left(error) =>
            fail(s"Opiskeluoikeuden deserialisointi epäonnistui: ${error.toString}")
        }
      }
    }

    def verifyPageSize(document: PDDocument, expectedPages: Int): Unit = {
      def eqWithTolerance(a: Double, b: Double, tolerance: Double = 0.05): Boolean = math.abs(a - b) <= tolerance

      val expectedWidth = 595.27
      val expectedHeight = 841.89

      val pages = document.getDocumentCatalog.getPages

      (0 until expectedPages).foreach { i =>

        val mediaBox = pages.get(i).getMediaBox

        eqWithTolerance(mediaBox.getWidth, expectedWidth) should be(true)
        eqWithTolerance(mediaBox.getHeight, expectedHeight) should be(true)
      }
    }

    def verifyTodistusPositionalContent(document: PDDocument): Unit = {
      // PDF:n koordinaatisto: 0,0 on vasemmassa ylänurkassa, y kasvaa alaspäin
      // PDF:n koko: 595 x 842 pistettä (A4)

      case class TextRegion(name: String, pageIndex: Int, x: Int, y: Int, width: Int, height: Int, expectedTexts: Seq[String])

      val regions = Seq(
        TextRegion("sivu1_otsikot", pageIndex = 0,
          x = 150, y = 150, width = 300, height = 200,
          expectedTexts = Seq(
            "TODISTUS",
            "Kielitutkinto Suorittaja",
            "(1.1.2007)"
          )),

        TextRegion("sivu1_kokeeninimi", pageIndex = 0,
          x = 150, y = 250, width = 300, height = 70,
          expectedTexts = Seq(
            "on osallistunut Yleisten kielitutkintojen",
            "suomen kielen keskitason tutkintoon"
          )),

        TextRegion("sivu1_johdanto_arvosanoihin", pageIndex = 0,
          x = 100, y = 325, width = 400, height = 50,
          expectedTexts = Seq(
            "Suorituksen perusteella hänen kielitaitonsa on arvioitu osataidoittain",
            "seuraavasti:"
          )),

        TextRegion("alueet", pageIndex = 0,
          x = 100, y = 375, width = 150, height = 90,
          expectedTexts = Seq(
            "PUHEEN YMMÄRTÄMINEN",
            "PUHUMINEN",
            "TEKSTIN YMMÄRTÄMINEN",
            "KIRJOITTAMINEN"
          )),

        TextRegion("arvosanat", pageIndex = 0,
          x = 250, y = 375, width = 150, height = 90,
          expectedTexts = Seq(
            "4, keskitaso",
            "3, keskitaso",
            "3, keskitaso",
            "Alle 3 (hylätty)"
          )),

        TextRegion("arvosanakuvaus", pageIndex = 0,
          x = 100, y = 470, width = 400, height = 100,
          expectedTexts = Seq(
            "Tutkinnon kunkin osataidon hyväksytystä suorituksesta voi saada tasoarvion 3-4.",
            "Yleisissä kielitutkinnoissa kielitaidon arviointi perustuu kuuteen taitotasoon, jotka",
            "on kuvattu todistuksen kääntöpuolella. Yleisten kielitutkintojen taitotasoasteikko",
            "vastaa Eurooppalaisen viitekehyksen (EVK) taitotasoasteikkoa."
          )),

        TextRegion("allekirjoitus", pageIndex = 0,
          x = 49, y = 600, width = 356, height = 200,
          expectedTexts = Seq("Tutkinnon järjestäjä:",
            "Varsinais-Suomen kansanopisto",
            "Helsingissä, 3.1.2011",
            "Tämän todistuksen on myöntänyt Opetushallitus.",
            "Tämä tutkinto perustuu yleisistä kielitutkinnoista annettuun lakiin 964/2004 sekä valtioneuvoston asetuksiin",
            "1163/2004 ja 1109/2011.",
            "Alkuperän ja eheyden varmistamiseksi todistukseen on liitetty eIDAS-asetuksen mukainen kehittynyt",
            "sähköinen leima. Tämän todistuksen voimassaolo voidaan vahvistaa",
            " asti.")),

        TextRegion("Sivun 2 otsikkorivi", pageIndex = 1,
          x = 50, y = 60, width = 500, height = 40,
          expectedTexts = Seq("TAITOTASOKUVAUKSET",
            "EVK:N",
            "ASTEIKKO")),

        TextRegion("Sivun 2 vasen palsta", pageIndex = 1,
          x = 50, y = 90, width = 40, height = 650,
          expectedTexts = Seq("YLIN TASO",
            "6",
            "5",
            "KESKITASO",
            "4",
            "3",
            "PERUSTASO",
            "2",
            "1")),

        TextRegion("Sivun 2 taso 6", pageIndex = 1,
          x = 85, y = 105, width = 410, height = 75,
          expectedTexts = Seq(
            "Ymmärtää vaikeuksitta kaikenlaista"
          )),

        TextRegion("Sivun 2 taso 5", pageIndex = 1,
          x = 85, y = 190, width = 410, height = 75,
          expectedTexts = Seq(
            "Ymmärtää kaikenlaista normaalitempoista"
          )),

        TextRegion("Sivun 2 taso 4", pageIndex = 1,
          x = 85, y = 305, width = 410, height = 75,
          expectedTexts = Seq(
            "Ymmärtää normaalitempoista puhetta"
          )),

        TextRegion("Sivun 2 taso 3", pageIndex = 1,
          x = 85, y = 420, width = 410, height = 75,
          expectedTexts = Seq(
            "Ymmärtää pidempää yhtäjaksoista"
          )),

        TextRegion("Sivun 2 taso 2", pageIndex = 1,
          x = 85, y = 550, width = 410, height = 75,
          expectedTexts = Seq(
            "Ymmärtää selkeää ja yksinkertaistettua"
          )),

        TextRegion("Sivun 2 taso 1", pageIndex = 1,
          x = 85, y = 650, width = 410, height = 75,
          expectedTexts = Seq(
            "Ymmärtää hitaasta ja selkeästä"
          )),

        TextRegion("Sivun 2 oikea palsta", pageIndex = 1,
          x = 500, y = 90, width = 40, height = 650,
          expectedTexts = Seq(
            "C2",
            "C1",
            "B2",
            "B1",
            "A2",
            "A1")),

        TextRegion("Sivun 2 alaviite", pageIndex = 1,
          x = 45, y = 770, width = 500, height = 50,
          expectedTexts = Seq(
            "Yleisten kielitutkintojen taitotasoasteikko on linkitetty empiirisesti Eurooppalaisen viitekehyksen asteikkoon.")),

      )

      // Käy läpi määritellyt alueet
      val pages = document.getDocumentCatalog.getPages

      regions.foreach { region =>
        if (region.pageIndex < pages.getCount) {
          val page = pages.get(region.pageIndex)
          val stripper = new PDFTextStripperByArea()
          stripper.setSortByPosition(true)

          val rect = new Rectangle(region.x, region.y, region.width, region.height)
          stripper.addRegion(region.name, rect)
          stripper.extractRegions(page)

          val extractedText = stripper.getTextForRegion(region.name).trim()

          region.expectedTexts.foreach { expectedText =>
            withClue(s"Sivulla ${region.pageIndex + 1}, alueella '${region.name}' (x=${region.x}, y=${region.y}, w=${region.width}, h=${region.height}) ei löytynyt odotettua tekstiä '$expectedText'. Löydetty teksti: '$extractedText'. ") {
              extractedText should include(expectedText)
            }
          }
        } else {
          fail(s"Alue '${region.name}' viittaa sivuun ${region.pageIndex + 1}, mutta PDF:ssä on vain ${pages.getCount} sivua")
        }
      }
    }

    def verifyPdfUaAccessibility(pdfBytes: Array[Byte]): Unit = {
      VeraGreenfieldFoundryProvider.initialise()

      val flavoursToValidate = Seq(
        PDFAFlavour.PDFUA_1,    // PDF/UA-1, minkä täyttävän PDF:n openhtmltopdf luo. PDF/UA-2 ei mene läpi.
        // PDFAFlavour.WCAG_2_1    // WCAG 2.1, tämä on EU-direktiiviein vaatima. VeraPDF:n tuki on kuitenkin ilmeisesti tälle vielä jotain experimental-koodia
      )

      flavoursToValidate.foreach { flavour =>
        println(s"DEBUG: Validating flavour $flavour")

        val inputStream = new java.io.ByteArrayInputStream(pdfBytes)

        try {
          val parser: PDFAParser = Foundries.defaultInstance().createParser(inputStream, flavour)

          try {
            val validator: PDFAValidator = Foundries.defaultInstance().createValidator(flavour, false)
            val results = validator.validateAll(parser).asScala

            results.foreach { result =>
              val isCompliant = result.isCompliant
              val totalAssertions = result.getTotalAssertions
              val failedAssertions = result.getTestAssertions.asScala.filter(_.getStatus.toString != "PASSED")

              totalAssertions should be > 3000

              println(s"DEBUG: Flavour $flavour - Compliant: $isCompliant, Total assertions: $totalAssertions, Failed: ${failedAssertions.size}")

              if (!isCompliant && failedAssertions.nonEmpty) {
                println(s"DEBUG: Ensimmäiset 5 virhettä flavourille $flavour:")
                failedAssertions.take(5).foreach { assertion =>
                  println(s"  - ${assertion.getRuleId}: ${assertion.getMessage}")
                }
              }

              withClue(s"PDF ei ole ${flavour}-standardin mukainen. Epäonnistuneita tarkistuksia: ${failedAssertions.size}/${totalAssertions}. Ensimmäiset virheet: ${failedAssertions.take(3).map(a => s"${a.getRuleId.getClause}: ${a.getMessage}").mkString("; ")}. ") {
                isCompliant should be(true)
              }
            }
          } finally {
            parser.close()
          }
        } catch {
          case e: VeraPDFException =>
            fail(s"PDF-validointi epäonnistui flavourille $flavour: ${e.getMessage}", e)
        } finally {
          inputStream.close()
        }
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
      verifyPageSize(document, 2)
      verifyTodistusPositionalContent(document)
      verifyPdfUaAccessibility(pdfBytes)

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
      verifyPageSize(document, 2)
      verifyTodistusPositionalContent(document)
      verifyPdfUaAccessibility(pdfBytes)

      document.close()
    }

    // TODO: TOR-2400: validoi todistus jollain paikallisella validaattorilla, ja katso, että sisältää kaiken long-term -validointia tukevan

    // TODO: TOR-2400: tarkista, että pdf:n sisältö vastaa myös graafisesti renderöitynä odotettua (marginaalit jne.)
  }

}
