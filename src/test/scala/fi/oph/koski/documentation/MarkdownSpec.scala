package fi.oph.koski.documentation

import fi.oph.koski.util.EnvVariables
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Path}
import scala.xml.Unparsed

class MarkdownSpec extends AnyFreeSpec with Matchers with EnvVariables {
  private lazy val runningOnCI = optEnv("CI").isDefined
  private val fixturePath = Path.of("src/test/resources/markdown/markdown-fixture.md")
  private val expectedPath = Path.of("src/test/resources/markdown/markdown-fixture.html")
  private lazy val fixture = Files.readString(fixturePath)

  "Markdown renderöidään HTML:ksi ilman virhettä" in {
    // On a parser exception markdownToXhtml logs the error and falls back to the raw Markdown as Text
    Markdown.markdownToXhtml(fixture) shouldBe an[Unparsed]
  }

  "Dokumentaatiossa käytetyt Markdown-rakenteet renderöidään kuten ennenkin" in {
    val html = Markdown.markdownToXhtmlString(fixture)
    Option.when(Files.exists(expectedPath))(Files.readString(expectedPath)) match {
      case Some(`html`) => succeed
      case _ if runningOnCI =>
        fail(s"Rendered HTML differs from $expectedPath. Run MarkdownSpec locally and commit the rewritten file.")
      case _ =>
        Files.writeString(expectedPath, html)
        fail(
          s"Rendered HTML differed from $expectedPath, so the file was rewritten. " +
            "Review the change with git diff and commit it if the new rendering is acceptable."
        )
    }
  }
}
