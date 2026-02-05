package fi.oph.koski.todistus

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Opiskeluoikeus, YleisenKielitutkinnonOsakokeenSuoritus}
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.{PDFTextStripper, PDFTextStripperByArea}
import org.json4s.jackson.JsonMethods
import org.scalatest.BeforeAndAfterAll
import org.verapdf.core.VeraPDFException
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider
import org.verapdf.pdfa.flavours.PDFAFlavour
import org.verapdf.pdfa.{Foundries, PDFAParser, PDFAValidator}

import java.awt.Rectangle
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import scala.jdk.CollectionConverters._

class TodistusLatausSpec extends TodistusSpecHelpers with BeforeAndAfterAll {

  private val lang = "fi"
  private val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
  private val expectedHash = laskeHenkilötiedotHash(oppija)
  private val hetu = oppija.hetu.get
  private val oppijaOid = oppija.oid

  private lazy val opiskeluoikeus: KielitutkinnonOpiskeluoikeus = {
    getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).get
  }

  private lazy val opiskeluoikeusOid: String = opiskeluoikeus.oid.get

  private lazy val todistusJob: TodistusJob = {
    val req = TodistusGenerateRequest(opiskeluoikeusOid, lang)
    addGenerateJobSuccessfully(req, hetu) { todistusJob =>
      todistusJob.state should equal(TodistusState.QUEUED)
      todistusJob
    }
  }

  private lazy val completedJob: TodistusJob = waitForCompletion(todistusJob.id, hetu)

  private lazy val completedJobFromDb: TodistusJob = app.todistusRepository.getFromDbForUnitTests(completedJob.id).get

  private lazy val pdfBytes: Array[Byte] = {
    var bytes: Array[Byte] = null
    verifyDownloadResultAndContent(s"/todistus/download/${completedJob.id}", hetu) {
      bytes = response.getContentBytes()
    }
    bytes
  }

  private var _pdfDocument: Option[PDDocument] = None
  private lazy val pdfDocument: PDDocument = {
    val doc = Loader.loadPDF(pdfBytes)
    _pdfDocument = Some(doc)
    doc
  }

  private lazy val pdfText: String = new PDFTextStripper().getText(pdfDocument)

  private lazy val presignedPdfBytes: Array[Byte] = {
    var bytes: Array[Byte] = null
    verifyPresignedResultAndContent(s"/todistus/download/presigned/${completedJob.id}") {
      bytes = response.getContentBytes()
    }
    bytes
  }

  private var _presignedPdfDocument: Option[PDDocument] = None
  private lazy val presignedPdfDocument: PDDocument = {
    val doc = Loader.loadPDF(presignedPdfBytes)
    _presignedPdfDocument = Some(doc)
    doc
  }

  private lazy val presignedPdfText: String = new PDFTextStripper().getText(presignedPdfDocument)

  // Temp-hakemistot debuggausta varten
  private lazy val datePrefix: String = {
    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
  }

  private lazy val baseTempDir: java.nio.file.Path = {
    val dir = java.nio.file.Files.createTempDirectory(s"todistus-test-kielitutkinto-yleinenkielitutkinto-fi-${datePrefix}-")
    println(s"DEBUG: Temp-hakemisto: ${dir.toAbsolutePath}")
    dir
  }

  override protected def afterAll(): Unit = {
    if (_pdfDocument.isDefined) {
      _pdfDocument.get.close()
    }
    if (_presignedPdfDocument.isDefined) {
      _presignedPdfDocument.get.close()
    }
    cleanup()
    super.afterAll()
  }

  "Todistuksen generointi onnistuu ja todistus valmistuu" in {
    // Kirjoita PDF temp-tiedostoon debuggausta varten
    writeTempPdf(baseTempDir, "todistus-test.pdf", pdfBytes, "PDF kirjoitettu tiedostoon")

    completedJob.state should equal(TodistusState.COMPLETED)
    completedJob.error should be(None)
    completedJobFromDb.error should be(None)
  }

  "Todistusjobin tiedot tallennetaan oikein" in {
    completedJob.userOid should be(None)
    completedJobFromDb.userOid should be(Some(oppijaOid))

    completedJob.opiskeluoikeusVersionumero should be(Some(opiskeluoikeus.versionumero.get))
    completedJobFromDb.opiskeluoikeusVersionumero should be(Some(opiskeluoikeus.versionumero.get))

    completedJob.oppijaHenkilötiedotHash should be(Some(expectedHash))
    completedJobFromDb.oppijaHenkilötiedotHash should be(Some(expectedHash))

    completedJob.completedAt should not be empty
    completedJobFromDb.completedAt should not be empty

    completedJob.startedAt should not be empty
    completedJobFromDb.startedAt should not be empty

    completedJob.worker should be(None)
    completedJobFromDb.worker should be(Some("local"))
  }

  "Suora lataus palauttaa oikeat HTTP-headerit" in {
    verifyDownloadResultAndContent(s"/todistus/download/${completedJob.id}", hetu) {
      response.header("Content-Type") should include("application/pdf")
      response.header("Content-Disposition") should include("attachment")
      response.header("Content-Disposition") should include("filename")
      response.header("Content-Disposition") should include("yki-todistus-fi.pdf")
    }
  }

  // Testaa molemmat lataustiedot: suora lataus ja presigned URL
  Seq(
    ("suora-lataus", () => pdfBytes, () => pdfDocument, () => pdfText),
    ("presigned-lataus", () => presignedPdfBytes, () => presignedPdfDocument, () => presignedPdfText)
  ).foreach { case (lataustapa, bytesGetter, documentGetter, textGetter) =>
    s"$lataustapa" - {
      "Lataus onnistuu" in {
        bytesGetter().length should be > 0
      }

      "PDF sisältää oikeat tekstit" in {
        verifyYleinenKielitutkintoTodistusSisalto(textGetter())
      }

      "PDF on allekirjoitettu oikein" in {
        verifyTodistusSignature(documentGetter())
      }

      "PDF käyttää oikeita fontteja ja ne on embedattu" in {
        verifyTodistusFontit(documentGetter())
      }

      "PDF:ssä on oikea määrä sivuja" in {
        verifyTodistusSivumaara(documentGetter(), 2)
      }

      "PDF metadata sisältää oikeat tiedot" in {
        verifyTodistusMetadata(documentGetter(), completedJob, opiskeluoikeus)
      }

      "PDF on PDF/UA-1 -saavutettava" in {
        verifyPdfUaAccessibility(bytesGetter())
      }

      "PDF sivukoko on A4" in {
        verifyPageSize(documentGetter(), 2)
      }

      "PDF tekstisisällöt ovat oikeilla paikoilla" in {
        verifyTeksitOvatGraafisestiOikeillaKohdilla(documentGetter())
      }

      "PDF alueet ja logot vastaavat referenssikuvaa pikselitasolla" in {
        verifyAreasByPixel(documentGetter(), lataustapa)
      }
    }
  }

  // TODO: TOR-2400: validoi todistus jollain paikallisella validaattorilla, ja katso, että sisältää kaiken long-term -validointia tukevan



  private def verifyYleinenKielitutkintoTodistusSisalto(pdfText: String): Unit = {
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

  private def verifyTodistusSignature(document: PDDocument): Unit = {
    val signerName = document.getLastSignatureDictionary.getName()
    signerName should be("TEST Signer")
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

            println(s"DEBUG: Validating flavour $flavour - Compliant: $isCompliant, Total assertions: $totalAssertions, Failed: ${failedAssertions.size}")

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

  def verifyTeksitOvatGraafisestiOikeillaKohdilla(document: PDDocument): Unit = {
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
          "on osallistunut yleisten kielitutkintojen",
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

  // Vertailee, että logot ja muut sivun alueet vastaavat renderöitynä grafiikkana (lähes) toisiaan.
  def verifyAreasByPixel(document: PDDocument, lataustapa: String): Unit = {
    // Luo lataustapa-alihakemisto
    val lataustapaDir = baseTempDir.resolve(lataustapa)
    java.nio.file.Files.createDirectories(lataustapaDir)
    println(s"DEBUG: Lataustapa-hakemisto: ${lataustapaDir.toAbsolutePath}")

    def loadReferenceImage(filename: String): BufferedImage = {
      val resourcePath = s"/todistus-test-kielitutkinto-yleinenkielitutkinto-fi-expected/$filename"
      val inputStream = getClass.getResourceAsStream(resourcePath)
      require(inputStream != null, s"Referenssikuvaa ei löytynyt polusta: $resourcePath")
      try {
        ImageIO.read(inputStream)
      } finally {
        inputStream.close()
      }
    }

    case class TestRegion(
                           name: String,
                           pageIndex: Int,
                           x: Int,
                           y: Int,
                           width: Int,
                           height: Int,
                           minContentRatio: Double,
                           skipPixelComparison: Boolean = false
                         )

    // Määrittele testattavat alueet renderöidyssä kuvassa

    // PDF renderöidään 72 DPI:llä, mikä vastaa PDF:n natiiveja pisteitä ja tekee A4:stä noin 595 x 842 pikseliä
    // Koordinaatit ovat pikseleinä renderöidyssä kuvassa (0,0 = vasen ylänurkka, y kasvaa alaspäin)
    // minContentRatio: Minimiosuus ei-valkoisia pikseleitä jotta alue katsotaan sisällölliseksi
    // skipPixelComparison: Jos true, vertaillaan vain sisältömääriä, ei pikselikohtaista samankaltaisuutta
    val dpi = 72f
    val testRegions = Seq(
      TestRegion("vasen_ylanurkka", pageIndex = 0, x = 45, y = 55, width = 200, height = 100, minContentRatio = 0.25),
      TestRegion("oikea_alanurkka", pageIndex = 0, x = 420, y = 740, width = 130, height = 50, minContentRatio = 0.15),
      TestRegion("sivu1_kokonaan", pageIndex = 0, x = 0, y = 0, width = 595, height = 842, minContentRatio = 0.05, skipPixelComparison = true),
      TestRegion("sivu2_kokonaan", pageIndex = 1, x = 0, y = 0, width = 595, height = 842, minContentRatio = 0.10, skipPixelComparison = true)
    )

    def renderPageToImage(doc: PDDocument, pageIndex: Int): BufferedImage = {
      val renderer = new PDFRenderer(doc)
      renderer.renderImageWithDPI(pageIndex, dpi)
    }

    def cropImage(image: BufferedImage, region: TestRegion): BufferedImage = {
      val actualX = math.max(0, math.min(region.x, image.getWidth - 1))
      val actualY = math.max(0, math.min(region.y, image.getHeight - 1))
      val actualWidth = math.min(region.width, image.getWidth - actualX)
      val actualHeight = math.min(region.height, image.getHeight - actualY)

      image.getSubimage(actualX, actualY, actualWidth, actualHeight)
    }

    // Tarkistaa että kuvassa on riittävästi ei-valkoista/ei-taustaväristä sisältöä
    def isImageContentful(image: BufferedImage, minContentRatio: Double = 0.10): (Boolean, Double) = {
      val contentRatio = calculateContentRatio(image)
      (contentRatio >= minContentRatio, contentRatio)
    }

    // Tarkistaa että alue on tyhjä (lähes täysin valkoinen)
    def isAreaEmpty(image: BufferedImage, maxContentRatio: Double = 0.05): (Boolean, Double) = {
      val contentRatio = calculateContentRatio(image)
      (contentRatio <= maxContentRatio, contentRatio)
    }

    // Laskee kuinka suuri osuus pikseleistä sisältää ei-valkoista sisältöä
    def calculateContentRatio(image: BufferedImage): Double = {
      val width = image.getWidth
      val height = image.getHeight
      val totalPixels = width * height

      var contentPixels = 0

      for {
        x <- 0 until width
        y <- 0 until height
      } {
        val rgb = image.getRGB(x, y)
        if (!isBackgroundColor(rgb)) {
          contentPixels += 1
        }
      }

      contentPixels.toDouble / totalPixels
    }

    def isBackgroundColor(rgb: Int): Boolean = {
      val r = (rgb >> 16) & 0xFF
      val g = (rgb >> 8) & 0xFF
      val b = rgb & 0xFF

      // Valkoinen tai lähes valkoinen (threshold 240)
      r > 240 && g > 240 && b > 240
    }

    // Tarkistaa että sivun marginaalit ovat tyhjät
    def verifyMarginsAreEmpty(pageImage: BufferedImage, pageIndex: Int, outputDir: java.nio.file.Path): Unit = {
      val pageWidth = pageImage.getWidth
      val pageHeight = pageImage.getHeight

      // Marginaalit 72 DPI:llä (1 piste = 1 pikseli)
      val sideMargin = 49  // Vasen ja oikea
      val verticalMargin = 59  // Ylä ja ala

      case class MarginRegion(name: String, x: Int, y: Int, width: Int, height: Int, maxContentRatio: Double)

      val margins = if (pageIndex == 0) {
        // Sivu 1: Tiukat marginaalit
        Seq(
          MarginRegion("vasen", x = 0, y = 0, width = sideMargin, height = pageHeight, maxContentRatio = 0.00001),
          MarginRegion("oikea", x = pageWidth - sideMargin, y = 0, width = sideMargin, height = pageHeight, maxContentRatio = 0.00001),
          MarginRegion("ylä", x = 0, y = 0, width = pageWidth, height = verticalMargin, maxContentRatio = 0.00001),
          MarginRegion("ala", x = 0, y = pageHeight - verticalMargin, width = pageWidth, height = verticalMargin, maxContentRatio = 0.00001)
        )
      } else {
        // Sivu 2: Oikea marginaali sallii enemmän sisältöä
        Seq(
          MarginRegion("vasen", x = 0, y = 0, width = sideMargin, height = pageHeight, maxContentRatio = 0.00001),
          MarginRegion("oikea", x = pageWidth - sideMargin, y = 0, width = sideMargin, height = pageHeight, maxContentRatio = 0.0010),
          MarginRegion("ylä", x = 0, y = 0, width = pageWidth, height = verticalMargin, maxContentRatio = 0.00001),
          MarginRegion("ala", x = 0, y = pageHeight - verticalMargin, width = pageWidth, height = verticalMargin, maxContentRatio = 0.00001)
        )
      }

      margins.foreach { margin =>
        val marginImage = pageImage.getSubimage(margin.x, margin.y, margin.width, margin.height)
        val (isEmpty, contentRatio) = isAreaEmpty(marginImage, margin.maxContentRatio)

        // Tallenna marginaali debuggausta varten aina
        writeTempImage(
          outputDir,
          s"todistus-margin-sivu${pageIndex + 1}-${margin.name}.png",
          marginImage,
          f"DEBUG: Sivu ${pageIndex + 1} ${margin.name}marginaali: ${contentRatio * 100}%.2f%% pikseleistä ei-valkoisia (max sallittu: ${margin.maxContentRatio * 100}%.8f%%), tallennettu"
        )

        withClue(f"Sivu ${pageIndex + 1} ${margin.name}marginaali ei ole tyhjä (${contentRatio * 100}%.2f%% pikseleistä sisältää grafiikkaa, max sallittu ${margin.maxContentRatio * 100}%.8f%%). ") {
          isEmpty should be(true)
        }
      }
    }

    def compareImages(img1: BufferedImage, img2: BufferedImage, tolerance: Double = 0.05): (Boolean, Double, Double) = {
      // Vertaa kuvien kokoa
      if (img1.getWidth != img2.getWidth || img1.getHeight != img2.getHeight) {
        return (false, 1.0, 1.0)
      }

      val width = img1.getWidth
      val height = img1.getHeight
      val totalPixels = width * height

      // Laske erilaiset pikselit
      var differentPixels = 0
      var totalDifference = 0.0

      for {
        x <- 0 until width
        y <- 0 until height
      } {
        val rgb1 = img1.getRGB(x, y)
        val rgb2 = img2.getRGB(x, y)

        if (rgb1 != rgb2) {
          differentPixels += 1

          // Laske värikomponenttien erot
          val r1 = (rgb1 >> 16) & 0xFF
          val g1 = (rgb1 >> 8) & 0xFF
          val b1 = rgb1 & 0xFF

          val r2 = (rgb2 >> 16) & 0xFF
          val g2 = (rgb2 >> 8) & 0xFF
          val b2 = rgb2 & 0xFF

          val diff = math.sqrt(
            math.pow(r1 - r2, 2) +
              math.pow(g1 - g2, 2) +
              math.pow(b1 - b2, 2)
          ) / 441.67 // Normalisoi välille 0-1 (max diff = sqrt(3*255^2))

          totalDifference += diff
        }
      }

      val pixelDifferenceRatio = differentPixels.toDouble / totalPixels
      val averageDifference = if (differentPixels > 0) totalDifference / differentPixels else 0.0

      // Hyväksy jos alle toleranssin verran pikseleitä eroaa tai keskimääräinen ero on pieni
      val areSimilar = pixelDifferenceRatio < tolerance || averageDifference < tolerance
      (areSimilar, pixelDifferenceRatio, averageDifference)
    }

    val testPages = Map(
      0 -> renderPageToImage(document, 0),
      1 -> renderPageToImage(document, 1)
    )
    val referencePages = Map(
      0 -> loadReferenceImage("todistus-full-page-1.png"),
      1 -> loadReferenceImage("todistus-full-page-2.png")
    )

    // Tallenna koko renderöidyt sivut debuggausta varten
    testPages.foreach { case (pageIdx, pageImage) =>
      println(s"DEBUG: Renderöity actual-sivu ${pageIdx + 1}: ${pageImage.getWidth}x${pageImage.getHeight} px")
      writeTempImage(
        lataustapaDir,
        s"todistus-full-page-${pageIdx + 1}.png",
        pageImage,
        s"DEBUG: Actual-sivun ${pageIdx + 1} kuva kirjoitettu"
      )
    }

    referencePages.foreach { case (pageIdx, pageImage) =>
      println(s"DEBUG: Ladattu expected-sivu ${pageIdx + 1}: ${pageImage.getWidth}x${pageImage.getHeight} px")
    }

    // Tarkista että marginaalit ovat tyhjät molemmilla sivuilla
    testPages.foreach { case (pageIdx, pageImage) =>
      verifyMarginsAreEmpty(pageImage, pageIdx, lataustapaDir)
    }

    // Käy läpi jokainen alue
    testRegions.foreach { region =>
      println(s"DEBUG: Tarkistetaan alue: ${region.name} (sivu ${region.pageIndex + 1}, x=${region.x}, y=${region.y}, w=${region.width}, h=${region.height})")

      // Leikkaa testattavat alueet oikealta sivulta
      val testArea = cropImage(testPages(region.pageIndex), region)
      val referenceArea = cropImage(referencePages(region.pageIndex), region)

      // Tallenna leikatut alueet debuggausta varten
      writeTempImage(
        lataustapaDir,
        s"todistus-area-${region.name}.png",
        testArea,
        s"DEBUG: Actual-alue '${region.name}' kirjoitettu"
      )

      // Tarkista että alueet eivät ole tyhjiä
      val (actualHasContent, actualContentRatio) = isImageContentful(testArea, minContentRatio = region.minContentRatio)
      val (expectedHasContent, expectedContentRatio) = isImageContentful(referenceArea, minContentRatio = region.minContentRatio)

      withClue(f"Actual-alue '${region.name}' on tyhjä tai lähes tyhjä (vain ${actualContentRatio * 100}%.2f%% sisältöä, vaadittu vähintään ${region.minContentRatio * 100}%.2f%%). Tarkista että koordinaatit ovat oikein. ") {
        actualHasContent should be(true)
      }

      withClue(f"Expected-alue '${region.name}' on tyhjä tai lähes tyhjä (vain ${expectedContentRatio * 100}%.2f%% sisältöä, vaadittu vähintään ${region.minContentRatio * 100}%.2f%%). Tarkista että koordinaatit ovat oikein. ") {
        expectedHasContent should be(true)
      }

      // Tarkista että sisältömäärät ovat lähellä toisiaan (merkki että alueet ovat samankaltaisia)
      val contentRatioDifference = math.abs(actualContentRatio - expectedContentRatio)
      val maxAllowedDifference = 0.005 // Sallitaan max 0.5% absoluuttinen ero sisältömäärissä

      withClue(f"Alueen '${region.name}' sisältömäärät poikkeavat liikaa toisistaan. Actual: ${actualContentRatio * 100}%.2f%%, expected: ${expectedContentRatio * 100}%.2f%%, ero ${contentRatioDifference * 100}%.2f%% (max ${maxAllowedDifference * 100}%.2f%%). Tarkista että koordinaatit ovat oikein. ") {
        contentRatioDifference should be <= maxAllowedDifference
      }

      // Vertaa alueiden samankaltaisuutta pikselitasolla
      if (!region.skipPixelComparison) {
        val (areSimilar, pixelDiff, avgDiff) = compareImages(testArea, referenceArea, tolerance = 0.05)

        withClue(f"Alue '${region.name}' ei vastaa expected-kuvaa. Pikselierojen osuus: ${pixelDiff * 100}%.2f%%, keskimääräinen ero: ${avgDiff * 100}%.2f%%. ") {
          areSimilar should be(true)
        }
      } else {
        println(s"DEBUG: Alue '${region.name}' pikselivertailu ohitettu (skipPixelComparison=true)")
      }
    }
  }

  private def writeTempPdf(dir: java.nio.file.Path, filename: String, bytes: Array[Byte], debugMessage: String): java.nio.file.Path = {
    val file = dir.resolve(filename)
    java.nio.file.Files.write(file, bytes)
    println(s"$debugMessage: ${file.toAbsolutePath}")
    file
  }

  private def writeTempImage(dir: java.nio.file.Path, filename: String, image: BufferedImage, debugMessage: String): java.nio.file.Path = {
    val file = dir.resolve(filename)
    ImageIO.write(image, "png", file.toFile)
    println(s"$debugMessage: ${file.toAbsolutePath}")
    file
  }
}
