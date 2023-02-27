package fi.oph.koski.meta

import fi.oph.koski.typemodel.TsFileUpdater
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.io.Source

class TypescriptExportsSpec extends AnyFreeSpec with Matchers {
  "Typescript exports" - {
    "Tarkistetaan että Typescript-tyypitykset on päivitetty (ajamalla make ts-types)" - {
      val files = TsFileUpdater
        .updateTypeFiles(dryRun = true)
        .groupBy(_.fullPath(TsFileUpdater.targetPath))
        .map(_._2.head)

      files.foreach { t => s"${t.fullPath("")}" in {
        val file = new File(t.fullPath(TsFileUpdater.targetPath).toString)
        withClue("Tiedosto on olemassa:") {
          file.exists() should equal(true)
        }

        val source = Source.fromFile(file)
        val content = source.getLines.mkString
        source.close()

        withClue("Tiedoston sisältö ei ole ajantasainen:") {
          removeFormatting(content) should equal(removeFormatting(t.content))
        }
      }}
    }
  }

  private def removeFormatting(s: String): String =
    s
      .replaceAll("[\\s\\n]", "")
      .replaceAll("\"", "'")
      .replaceAll("[,|;]", "")
}
