package fi.oph.tor.localization

import org.scalatest.{FreeSpec, Matchers}

class LocalizedStringSpec extends FreeSpec with Matchers {
  "String interpolation" - {
    import LocalizedStringImplicits.LocalizedStringInterpolator
    "With available translations" in {
      val x = Finnish("äks", en = Some("ex"))
      val y = Finnish("yy", en = Some("why"))
      localized"hello, $x and $y".get("en") should equal("hello, ex and why")
      localized"hello, $x and $y".get("fi") should equal("hello, äks and yy")
    }
    "With translations missing" in {
      val x = LocalizedString.english("x")
      val y = LocalizedString.english("y")
      localized"hello, $x and $y".get("en") should equal("hello, x and y")
      localized"hello, $x and $y".get("fi") should equal("hello, x and y")
    }
  }
}
