package fi.oph.koski.localization

import com.fasterxml.jackson.core.{JsonFactory, JsonToken}
import fi.oph.koski.valpas.localization.ValpasLocalizationConfig
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DefaultTextsSpec extends AnyFreeSpec with Matchers {
  // A JSON parser keeps the last of two equal keys, so a pasted-in duplicate silently
  // overrides the earlier text instead of failing.
  "Default texts have no duplicate keys" - {
    List(new KoskiLocalizationConfig, new ValpasLocalizationConfig).map(_.defaultFinnishTextsResourceFilename).foreach { resource =>
      resource in {
        val keys = fieldNames(resource)
        keys.diff(keys.distinct).distinct shouldBe empty
      }
    }
  }

  private def fieldNames(resource: String): List[String] = {
    val parser = new JsonFactory().createParser(getClass.getResourceAsStream(resource))
    try {
      Iterator.continually(parser.nextToken())
        .takeWhile(_ != null)
        .filter(_ == JsonToken.FIELD_NAME)
        .map(_ => parser.currentName())
        .toList
    } finally {
      parser.close()
    }
  }
}
