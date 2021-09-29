package fi.oph.koski.localization

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.{Finnish, LocalizedString}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LocalizedStringSpec extends AnyFreeSpec with Matchers {
  "String concatenation" - {
    "With available translations" in {
      val x = Finnish("äks", en = Some("ex"))
      val y = Finnish("yy", en = Some("why"))
      val localizedSentence = LocalizedString.concat("hello, ", x, " and ", y)
      localizedSentence.get("en") should equal("hello, ex and why")
      localizedSentence.get("fi") should equal("hello, äks and yy")
    }
    "With translations missing" in {
      val x = LocalizedString.english("x")
      val y = LocalizedString.english("y")
      val localizedSentence = LocalizedString.concat("hello, ", x, " and ", y)
      localizedSentence.get("en") should equal("hello, x and y")
      localizedSentence.get("fi") should equal("hello, x and y")
    }
  }
}
