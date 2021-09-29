package fi.oph.koski.http

import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class KoskiErrorCategorySpec extends AnyFreeSpec with TestEnvironment with Matchers {
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
