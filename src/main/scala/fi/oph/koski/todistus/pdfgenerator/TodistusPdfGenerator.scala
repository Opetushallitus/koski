package fi.oph.koski.todistus.pdfgenerator

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle
import com.openhtmltopdf.pdfboxout.{PDFCreationListener, PdfBoxRenderer, PdfRendererBuilder}
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import fi.oph.koski.log.Logging
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

import java.io.{ByteArrayOutputStream, OutputStream}
import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters._

trait TodistusData {
  def toTemplateVariables: Map[String, Object]
  def templateName: String
}

case class TodistusMetadata(
  oppijaOid: String,
  opiskeluoikeusOid: String,
  opiskeluoikeusVersionumero: Int,
  todistusJobId: String,
  generointiStartedAt: String,
  commitHash: String
) {
  def toMap: Map[String, String] = Map(
    "OppijaOid" -> oppijaOid,
    "OpiskeluoikeusOid" -> opiskeluoikeusOid,
    "OpiskeluoikeusVersionumero" -> opiskeluoikeusVersionumero.toString,
    "TodistusJobId" -> todistusJobId,
    "GenerointiStartedAt" -> generointiStartedAt,
    "CommitHash" -> commitHash
  )
}

class TodistusPdfGenerator extends Logging {

  private val templateEngine: TemplateEngine = {
    val templateResolver = new ClassLoaderTemplateResolver()
    templateResolver.setPrefix("/todistus-templates/")
    templateResolver.setSuffix(".html")
    templateResolver.setTemplateMode(TemplateMode.HTML)
    templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name())
    templateResolver.setCacheable(true)

    // TODO: TOR-2400: Varmista, että templatet cachetetaan? Vaatii ehkä jonkun option?
    val engine = new TemplateEngine()
    engine.setTemplateResolver(templateResolver)
    engine
  }

  def generatePdf(data: TodistusData, metadata: TodistusMetadata): Array[Byte] = {
    val html = generateHtml(data.templateName, data)
    convertHtmlToPdf(html, metadata)
  }

  def generatePdf(data: TodistusData, metadata: TodistusMetadata, outputStream: OutputStream): Unit = {
    val html = generateHtml(data.templateName, data)
    convertHtmlToPdf(html, metadata, outputStream)
  }

  def generateHtml(data: TodistusData): String = {
    generateHtml(data.templateName, data)
  }

  private def generateHtml(templateName: String, data: TodistusData): String = {
    val context = new Context()
    context.setVariables(data.toTemplateVariables.asJava)
    val result = templateEngine.process(templateName, context)

    result
  }

  private def convertHtmlToPdf(html: String, metadata: TodistusMetadata): Array[Byte] = {
    val outputStream = new ByteArrayOutputStream()
    convertHtmlToPdf(html, metadata, outputStream)
    outputStream.toByteArray
  }

  private def convertHtmlToPdf(html: String, metadata: TodistusMetadata, outputStream: OutputStream): Unit = {
    // TODO: TOR-2400: Älä tee builderiä aina uudestaan, ja lisää cache?
    val builder = new PdfRendererBuilder()
    builder.useDefaultPageSize(210.0f, 297.0f, BaseRendererBuilder.PageSizeUnits.MM)
    builder.useFastMode()
    builder.usePdfUaAccessibility(true) // TODO: TOR-2400: riittääkö tämä, vai tarvitaanko muitakin optioita?
    builder.useSVGDrawer(new BatikSVGDrawer())

    builder.withHtmlContent(html, null)

    // Lisää producer-tieto
    builder.withProducer(s"Koski (commit: ${metadata.commitHash})")

    // TODO: TOR-2400: Fontit ovat nyt base64:nä HTML:ssä, jotta täsmälleen saman koodin saa toimimaan JAR:in sisältä ja tiedostojärjestelmästä, kun
    // haluaa rendata esim. Chromella. Lisää nämä ja poista @font-face:t HTML:stä, jos haluat käyttää näitä. Tämä lähinnä helpottaisi HTML-tiedostojen
    // editointia, mutta myös muistinkäyttö olisi tehokkaampaa, jos fontteja ei olisi erikseen muistissa jokaisella templatella. Jotta fontit saisi
    // pysymään muisti-cachessa, se pitäisi rakentaa itse byte-array -datalla: openhtmltopdf lukee fontit uudestaan aina pdf:ää tehdessään.
//    registerFont(builder, "fonts/OpenSans-Regular.ttf", 400)
//    registerFont(builder, "fonts/OpenSans-SemiBold.ttf", 600)
//    registerFont(builder, "fonts/OpenSans-Bold.ttf", 700)

    builder.toStream(outputStream)

    logger.info("START builder)")

    val renderer = builder.buildPdfRenderer()

    renderer.setListener(new PDFCreationListener {
      override def preOpen(pdfBoxRenderer: PdfBoxRenderer): Unit = {
        metadata.toMap.foreach { case (key, value) => pdfBoxRenderer.getOutputDevice.addMetadata(key, value)}
      }

      override def preWrite(pdfBoxRenderer: PdfBoxRenderer, i: Int): Unit = {}

      override def onClose(pdfBoxRenderer: PdfBoxRenderer): Unit = {}
    })

    try {
      renderer.createPDF()
    }
    finally {
      renderer.close()
    }
    logger.info("END builder")
  }

  private def registerFont(builder: PdfRendererBuilder, fontUrl: String, weight: Int) = {
    builder.useFont(() => {
      val fontResourceName = "/todistus-templates/" + fontUrl
      logger.info("Getting font " + fontResourceName)
      getClass.getResourceAsStream(fontResourceName)
    },
      "Open Sans",
      Integer.valueOf(weight),
      FontStyle.NORMAL,
      true
    )
  }
}
