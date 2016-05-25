package fi.oph.koski.http

import org.scalatest.{FreeSpec, Matchers}

class KoskiErrorCategorySpec extends FreeSpec with Matchers {
  KoskiErrorCategory.children foreach { category =>
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
