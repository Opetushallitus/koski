package fi.oph.tor.http

import org.scalatest.{FreeSpec, Matchers}

class TorErrorCategorySpec extends FreeSpec with Matchers {
  TorErrorCategory.children foreach { category =>
    testCategory(category.key, category)
  }

  private def testCategory(key: String, category: ErrorCategory): Unit = {
    key - {
      "ok" in {}
      category.children foreach {
        case (key, category) => testCategory(key, category)
      }
    }
  }
}
