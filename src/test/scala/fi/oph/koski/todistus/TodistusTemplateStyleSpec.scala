package fi.oph.koski.todistus

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.io.Source

class TodistusTemplateStyleSpec extends AnyFreeSpec with Matchers {

  private val templateDir = new File("src/main/resources/todistus-templates")

  private val templateFiles: List[File] = {
    require(templateDir.isDirectory, s"Template directory not found: ${templateDir.getAbsolutePath}")
    templateDir.listFiles()
      .filter(f => f.getName.startsWith("kielitutkinto_yleinenkielitutkinto_") && f.getName.endsWith(".html"))
      .sortBy(_.getName)
      .toList
  }

  private def extractStyle(html: String): String = {
    val startTag = "<style>"
    val endTag = "</style>"
    val startIdx = html.indexOf(startTag)
    val endIdx = html.indexOf(endTag)
    require(startIdx >= 0 && endIdx > startIdx, "Template must contain a <style> section")
    html.substring(startIdx + startTag.length, endIdx).trim
  }

  private def readFile(file: File): String = {
    val source = Source.fromFile(file, "UTF-8")
    try source.mkString finally source.close()
  }

  "Kaikkien kielitutkintotodistus-templatejen <style>-osiot ovat identtiset" in {
    templateFiles.length should be >= 9

    val styles = templateFiles.map { file =>
      file.getName -> extractStyle(readFile(file))
    }

    val referenceStyle = styles.head._2
    styles.tail.foreach { case (name, style) =>
      withClue(s"Template '$name' style differs from '${styles.head._1}':") {
        style shouldBe referenceStyle
      }
    }
  }
}
