package fi.oph.koski.api.misc

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class HealthCheckSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec with BeforeAndAfterEach {
  override def beforeEach(): Unit =  {
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    KoskiApplicationForTests.cacheManager.invalidateAllCaches()
    super.afterEach()
  }

  "GET /api/healthcheck/internal" - {
    "Returns success by default" in {
      get("api/healthcheck/internal") {
        verifyResponseStatusOk()
      }
    }

    "When healthcheck logic check fails by returning false, returns 500" in {
      get("api/healthcheck/internal") {
        verifyResponseStatusOk()
      }

      KoskiApplicationForTests.healthCheck.initializeMockSystem(1, (c) => {
        false
      })
      KoskiApplicationForTests.healthCheck.getMockSystemCounter shouldBe 1

      Thread.sleep(1000)
      get("api/healthcheck/internal") {
        verifyResponseStatusOk() // Still ok from cache, returns false on the background
      }
      Thread.sleep(1000)
      KoskiApplicationForTests.healthCheck.getMockSystemCounter shouldBe 0
      get("api/healthcheck/internal") {
        verifyResponseStatusOk(500)
      }
      KoskiApplicationForTests.healthCheck.getMockSystemCounter shouldBe -1
    }

    "When healthcheck logic throws exception, returns 500" in {
      KoskiApplicationForTests.healthCheck.initializeMockSystem(2, (c) => {
        if (c < 1) {
          throw new Exception("Test exception")
        }
        true
      })

      get("api/healthcheck/internal") {
        verifyResponseStatusOk()
      }
      KoskiApplicationForTests.healthCheck.getMockSystemCounter shouldBe 1
      Thread.sleep(1000)
      get("api/healthcheck/internal") {
        verifyResponseStatusOk() // Still from cache, throws exception on the background
      }
      Thread.sleep(1000)
      KoskiApplicationForTests.healthCheck.getMockSystemCounter shouldBe 0
      get("api/healthcheck/internal") {
        verifyResponseStatusOk(500)
      }
      KoskiApplicationForTests.healthCheck.getMockSystemCounter shouldBe -1
    }

    "When healthcheck times out, does not keep returning healthy" in {
      KoskiApplicationForTests.healthCheck.initializeMockSystem(2, (c) => {
        if (c < 1) {
          Thread.sleep(6000)
          throw new Exception("Test exception after long wait")
        }
        true
      })

      get("api/healthcheck/internal") {
        verifyResponseStatusOk()
      }
      Thread.sleep(1000)
      get("api/healthcheck/internal") {
        verifyResponseStatusOk() // Still from cache, triggers long background refresh
      }
      Thread.sleep(500)
      get("api/healthcheck/internal") {
        verifyResponseStatusOk() // Still from cache, since background refresh is still running and expire duration not passed
      }
      Thread.sleep(2000)
      get("api/healthcheck/internal") {
        verifyResponseStatus(500) // Should trigger error, since hard expire duration passed
      }
      // The previous call did not cause a retry of the check, since the background refresh was still running:
      KoskiApplicationForTests.healthCheck.getMockSystemCounter shouldBe 0

      get("api/healthcheck/internal") {
        verifyResponseStatus(500)
      }
      // Now a retry was run:
      KoskiApplicationForTests.healthCheck.getMockSystemCounter shouldBe -1
    }
  }
}
