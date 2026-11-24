package fi.oph.koski.localization

import com.fasterxml.jackson.core.{JsonFactory, JsonToken}
import fi.oph.koski.valpas.localization.ValpasLocalizationConfig
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.text.Normalizer
import java.util.Locale

class DefaultTextsSpec extends AnyFreeSpec with Matchers {
  private val resources =
    List(new KoskiLocalizationConfig, new ValpasLocalizationConfig).map(_.defaultFinnishTextsResourceFilename)

  // A JSON parser keeps the last of two equal keys, so a pasted-in duplicate silently
  // overrides the earlier text instead of failing.
  "Default texts have no duplicate keys" - {
    resources.foreach { resource =>
      resource in {
        val keys = fieldNames(resource)
        keys.diff(keys.distinct).distinct shouldBe empty
      }
    }
  }

  // Lokalisointipalvelu returns the keys in this order, and fetch_prod_localizations.sh
  // writes them in it, so a key placed anywhere else moves at the next refresh.
  "Default texts are in order, comparing letters and digits only, ignoring case and accents" - {
    resources.foreach { resource =>
      resource in {
        val keys = fieldNames(resource)
        val misplaced = keys.zip(keys.drop(1)).collect {
          case (previous, key) if sortKey(previous) > sortKey(key) => s"\"$key\" should come before \"$previous\""
        }
        misplaced shouldBe empty
      }
    }
  }

  private def sortKey(key: String): String =
    Normalizer.normalize(key, Normalizer.Form.NFD).toLowerCase(Locale.ROOT).replaceAll("[^0-9a-z]", "")

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
