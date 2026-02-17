package fi.oph.koski.todistus

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.MockUsers
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

  private val templateVariant = "fi"
  private val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
  private val expectedHash = laskeHenkilötiedotHash(oppija)
  private val hetu = oppija.hetu.get
  private val oppijaOid = oppija.oid

  private lazy val opiskeluoikeus: KielitutkinnonOpiskeluoikeus = {
    getVahvistettuKielitutkinnonOpiskeluoikeus(oppijaOid).get
  }

  private lazy val opiskeluoikeusOid: String = opiskeluoikeus.oid.get

  private lazy val todistusJob: TodistusJob = {
    val req = TodistusGenerateRequest(opiskeluoikeusOid, templateVariant)
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

  // Printattava todistus (fi_tulostettava_uusi) - vain pääkäyttäjille
  private val printTemplateVariant = "fi_tulostettava_uusi"

  private lazy val printTodistusJob: TodistusJob = {
    val req = TodistusGenerateRequest(opiskeluoikeusOid, printTemplateVariant)
    addGenerateJobSuccessfullyAsVirkailijaPääkäyttäjä(req) { todistusJob =>
      todistusJob.state should equal(TodistusState.QUEUED)
      todistusJob
    }
  }

  private lazy val printCompletedJob: TodistusJob = waitForCompletionAsVirkailijaPääkäyttäjä(printTodistusJob.id)

  private lazy val printPdfBytes: Array[Byte] = {
    var bytes: Array[Byte] = null
    get(s"/todistus/download/${printCompletedJob.id}", headers = authHeaders(MockUsers.paakayttaja)) {
      verifyResponseStatusOk()
      bytes = response.getContentBytes()
    }
    bytes
  }

  private var _printPdfDocument: Option[PDDocument] = None
  private lazy val printPdfDocument: PDDocument = {
    val doc = Loader.loadPDF(printPdfBytes)
    _printPdfDocument = Some(doc)
    doc
  }

  private lazy val printPdfText: String = new PDFTextStripper().getText(printPdfDocument)

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
    if (_printPdfDocument.isDefined) {
      _printPdfDocument.get.close()
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
    ("suora-lataus", () => pdfBytes, () => pdfDocument, () => pdfText, () => completedJob, false),
    ("presigned-lataus", () => presignedPdfBytes, () => presignedPdfDocument, () => presignedPdfText, () => completedJob, false)
  ).foreach { case (lataustapa, bytesGetter, documentGetter, textGetter, jobGetter, isPrintTemplate) =>
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
        verifyTodistusMetadata(documentGetter(), jobGetter(), opiskeluoikeus)
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

  // Testaa printattava todistus erikseen, koska sillä on eri rakenne
  "printattava-todistus" - {
    "Lataus onnistuu" in {
      printPdfBytes.length should be > 0
    }

    "PDF sisältää oikeat tekstit" in {
      verifyYleinenKielitutkintoTodistusSisalto(printPdfText)
    }

    "PDF on allekirjoitettu oikein" in {
      verifyTodistusSignature(printPdfDocument)
    }

    "PDF käyttää oikeita fontteja ja ne on embedattu" in {
      verifyTodistusFontit(printPdfDocument)
    }

    "PDF:ssä on oikea määrä sivuja" in {
      verifyTodistusSivumaara(printPdfDocument, 3)
    }

    "PDF metadata sisältää oikeat tiedot" in {
      verifyTodistusMetadata(printPdfDocument, printCompletedJob, opiskeluoikeus)
    }

    "PDF on PDF/UA-1 -saavutettava" in {
      verifyPdfUaAccessibility(printPdfBytes)
    }

    "PDF sivukoko on A4" in {
      verifyPageSize(printPdfDocument, 3)
    }

    "PDF tekstisisällöt ovat oikeilla paikoilla" in {
      verifyTeksitOvatGraafisestiOikeillaKohdillaPrintTemplate(printPdfDocument)
    }

    "PDF alueet ja logot vastaavat referenssikuvaa pikselitasolla" in {
      verifyAreasByPixelPrintTemplate(printPdfDocument, "printattava-todistus")
    }
  }


  def verifyTeksitOvatGraafisestiOikeillaKohdilla(document: PDDocument): Unit = {
    // PDF:n koordinaatisto: 0,0 on vasemmassa ylänurkassa, y kasvaa alaspäin
    // PDF:n koko: 595 x 842 pistettä (A4)

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

    verifyTextRegions(document, regions, "Tavallinen todistus")
  }

  // Printattavan todistuksen tekstisisältöjen tarkistus (3 sivua, eri allekirjoitusalue)
  def verifyTeksitOvatGraafisestiOikeillaKohdillaPrintTemplate(document: PDDocument): Unit = {
    val regions = Seq(
      // Sivu 1: Pääosin samat kuin tavallisessa todistuksessa, mutta allekirjoitusalue eri
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

      // Printattavan todistuksen allekirjoitusalue on erilainen - vain perustiedot, ei eIDAS-leiman tietoja
      TextRegion("allekirjoitus_print", pageIndex = 0,
        x = 49, y = 600, width = 356, height = 150,
        expectedTexts = Seq(
          "Tutkinnon järjestäjä:",
          "Varsinais-Suomen kansanopisto",
          "Helsingissä, 3.1.2011"
        )),

      // Sivu 2: Taitotasokuvaukset (samat kuin tavallisessa todistuksessa)
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

      // Sivu 3: Printattavan todistuksen leiska (uusi sivu, jota ei ole tavallisessa todistuksessa)
      TextRegion("sivu3_otsikko", pageIndex = 2,
        x = 100, y = 60, width = 396, height = 30,
        expectedTexts = Seq(
          "TIETOA TODISTUKSEN SAAJALLE"
        )),

      TextRegion("sivu3_teksti", pageIndex = 2,
        x = 100, y = 95, width = 396, height = 650,
        expectedTexts = Seq(
          "Voit tehdä oikaisuvaatimuspyynnön, jos et ole tyytyväinen YKI-testisi arviointiin.",
          "Oikaisuvaatimuspyyntö tarkoittaa, että YKI-testisi arvioidaan uudelleen, eli sille",
          "tehdään tarkistusarviointi. Tarkistusarvioinnissa testivastauksesi arvioidaan",
          "uudelleen.",
          "Jos et ole tyytyväinen YKI-testisi arviointiin, sinulla on kaksi vaihtoehtoa:",
          "1.  Voit pyytää tarkistusarviointia. Sinulla on 30 vuorokautta aikaa pyytää",
          "tarkistusarviointia. Aika alkaa siitä, kun saat tiedon valmiista todistuksesta. Voit",
          "pyytää tarkistusarviointia tällä verkkosivulla:",
          "https://yki.opintopolku.fi/yki/tarkistusarviointi.",
          "2.  Voit pyytää ensin tietoa arvosteluperusteiden soveltamisesta testiisi. Se",
          "tarkoittaa, että pyydät tietoa siitä, miten YKI-testisi on arvosteltu. Pyynnön",
          "tekeminen ei muuta testisi arviointia. Sinulla on 14 vuorokautta aikaa pyytää",
          "tietoa arvosteluperusteista. Aika alkaa siitä, kun saat tiedon valmiista",
          "todistuksesta. Voit tehdä pyynnön lähettämällä sähköpostia osoitteeseen",
          "yki-info@jyu.fi. Kun olet saanut vastauksen pyyntöösi, voit pyytää sen jälkeen",
          "myös tarkistusarviointia. Sinulla on 14 vuorokautta aikaa pyytää",
          "tarkistusarviointia. Aika alkaa siitä, kun saat tiedon siitä, miten YKI-testisi on",
          "arvosteltu.",
          "Lisätietoja löydät myös Opetushallituksen verkkosivuilta osoitteesta:",
          "https://www.oph.fi/fi/koulutus-ja-tutkinnot/yki-testin-jalkeen.",
          "Jos tarvitset muuta todistukseen tai tarkistusarviointiin liittyvää neuvontaa, voit",
          "lähettää kysymyksesi Opetushallitukselle osoitteeseen kielitutkinnot@oph.fi.",
          "Löydät Opetushallituksen yhteystiedot (Tutkintojen ja kieliosaamisen",
          "tunnustaminen-yksikkö) Opetushallituksen verkkosivuilta osoitteesta:",
          "https://www.oph.fi/fi/tietoa-meista/yhteystiedot."
        )))

    verifyTextRegions(document, regions, "Printattava todistus")
  }

  // Vertailee, että logot ja muut sivun alueet vastaavat renderöitynä grafiikkana (lähes) toisiaan.
  def verifyAreasByPixel(document: PDDocument, lataustapa: String): Unit = {
    // PDF renderöidään 72 DPI:llä, mikä vastaa PDF:n natiiveja pisteitä ja tekee A4:stä noin 595 x 842 pikseliä
    // Koordinaatit ovat pikseleinä renderöidyssä kuvassa (0,0 = vasen ylänurkka, y kasvaa alaspäin)
    // minContentRatio: Minimiosuus ei-valkoisia pikseleitä jotta alue katsotaan sisällölliseksi
    // skipPixelComparison: Jos true, vertaillaan vain sisältömääriä, ei pikselikohtaista samankaltaisuutta
    val testRegions = Seq(
      TestRegion("vasen_ylanurkka", pageIndex = 0, x = 45, y = 55, width = 200, height = 100, minContentRatio = 0.25),
      TestRegion("oikea_alanurkka", pageIndex = 0, x = 420, y = 740, width = 130, height = 50, minContentRatio = 0.15),
      TestRegion("sivu1_kokonaan", pageIndex = 0, x = 0, y = 0, width = 595, height = 842, minContentRatio = 0.05, skipPixelComparison = true),
      TestRegion("sivu2_kokonaan", pageIndex = 1, x = 0, y = 0, width = 595, height = 842, minContentRatio = 0.10, skipPixelComparison = true)
    )

    verifyPixelRegions(
      document,
      lataustapa,
      "/todistus-test-kielitutkinto-yleinenkielitutkinto-fi-expected",
      testRegions,
      pageCount = 2
    )
  }

  // Printattavan todistuksen pikselitason vertailu (3 sivua)
  def verifyAreasByPixelPrintTemplate(document: PDDocument, lataustapa: String): Unit = {
    val testRegions = Seq(
      // Sivu 1
      TestRegion("vasen_ylanurkka", pageIndex = 0, x = 45, y = 55, width = 200, height = 100, minContentRatio = 0.25),
      TestRegion("oikea_alanurkka", pageIndex = 0, x = 420, y = 740, width = 130, height = 50, minContentRatio = 0.15),
      TestRegion("sivu1_kokonaan", pageIndex = 0, x = 0, y = 0, width = 595, height = 842, minContentRatio = 0.05, skipPixelComparison = true),

      // Sivu 2
      TestRegion("sivu2_kokonaan", pageIndex = 1, x = 0, y = 0, width = 595, height = 842, minContentRatio = 0.10, skipPixelComparison = true),

      // Sivu 3 (leiska)
      TestRegion("sivu3_kokonaan", pageIndex = 2, x = 0, y = 0, width = 595, height = 842, minContentRatio = 0.05, skipPixelComparison = true)
    )

    verifyPixelRegions(
      document,
      lataustapa,
      "/todistus-test-kielitutkinto-yleinenkielitutkinto-fi_tulostettava_uusi-expected",
      testRegions,
      pageCount = 3
    )
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

  case class TextRegion(name: String, pageIndex: Int, x: Int, y: Int, width: Int, height: Int, expectedTexts: Seq[String])

  private def verifyTextRegions(document: PDDocument, regions: Seq[TextRegion], templateName: String): Unit = {
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
          withClue(s"$templateName: Sivulla ${region.pageIndex + 1}, alueella '${region.name}' (x=${region.x}, y=${region.y}, w=${region.width}, h=${region.height}) ei löytynyt odotettua tekstiä '$expectedText'. Löydetty teksti: '$extractedText'. ") {
            extractedText should include(expectedText)
          }
        }
      } else {
        fail(s"Alue '${region.name}' viittaa sivuun ${region.pageIndex + 1}, mutta PDF:ssä on vain ${pages.getCount} sivua")
      }
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

  private val dpi = 72f

  private def renderPageToImage(doc: PDDocument, pageIndex: Int): BufferedImage = {
    val renderer = new PDFRenderer(doc)
    renderer.renderImageWithDPI(pageIndex, dpi)
  }

  private def cropImage(image: BufferedImage, region: TestRegion): BufferedImage = {
    val actualX = math.max(0, math.min(region.x, image.getWidth - 1))
    val actualY = math.max(0, math.min(region.y, image.getHeight - 1))
    val actualWidth = math.min(region.width, image.getWidth - actualX)
    val actualHeight = math.min(region.height, image.getHeight - actualY)

    image.getSubimage(actualX, actualY, actualWidth, actualHeight)
  }

  private def isImageContentful(image: BufferedImage, minContentRatio: Double = 0.10): (Boolean, Double) = {
    val contentRatio = calculateContentRatio(image)
    (contentRatio >= minContentRatio, contentRatio)
  }

  private def calculateContentRatio(image: BufferedImage): Double = {
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

  private def isBackgroundColor(rgb: Int): Boolean = {
    val r = (rgb >> 16) & 0xFF
    val g = (rgb >> 8) & 0xFF
    val b = rgb & 0xFF

    // Valkoinen tai lähes valkoinen (threshold 240)
    r > 240 && g > 240 && b > 240
  }

  private def verifyPixelRegions(
    document: PDDocument,
    lataustapa: String,
    referenceResourcePath: String,
    testRegions: Seq[TestRegion],
    pageCount: Int
  ): Unit = {
    // Luo lataustapa-alihakemisto
    val lataustapaDir = baseTempDir.resolve(lataustapa)
    java.nio.file.Files.createDirectories(lataustapaDir)
    println(s"DEBUG: Lataustapa-hakemisto: ${lataustapaDir.toAbsolutePath}")

    def loadReferenceImage(filename: String): BufferedImage = {
      val resourcePath = s"$referenceResourcePath/$filename"
      val inputStream = getClass.getResourceAsStream(resourcePath)
      require(inputStream != null, s"Referenssikuvaa ei löytynyt polusta: $resourcePath")
      try {
        ImageIO.read(inputStream)
      } finally {
        inputStream.close()
      }
    }

    val testPages = (0 until pageCount).map { pageIdx =>
      pageIdx -> renderPageToImage(document, pageIdx)
    }.toMap

    val referencePages = (0 until pageCount).map { pageIdx =>
      pageIdx -> loadReferenceImage(s"todistus-full-page-${pageIdx + 1}.png")
    }.toMap

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

      // Tarkista että sisältömäärät ovat lähellä toisiaan
      val contentRatioDifference = math.abs(actualContentRatio - expectedContentRatio)
      val maxAllowedDifference = 0.005

      withClue(f"Alueen '${region.name}' sisältömäärät poikkeavat liikaa toisistaan. Actual: ${actualContentRatio * 100}%.2f%%, expected: ${expectedContentRatio * 100}%.2f%%, ero ${contentRatioDifference * 100}%.2f%% (max ${maxAllowedDifference * 100}%.2f%%). Tarkista että koordinaatit ovat oikein. ") {
        contentRatioDifference should be <= maxAllowedDifference
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
