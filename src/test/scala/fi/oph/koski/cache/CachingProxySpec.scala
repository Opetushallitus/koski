package fi.oph.koski.cache

import org.scalatest.{FreeSpec, Matchers}

class CachingProxySpec extends FreeSpec with Matchers {
  "CachingProxy" - {

    "Calls underlying impl" in {
      val service = new TestServiceImpl
      val cached = makeCache(service)
      cached.getThing(1) should equal("1")
    }
    "Caches value" in {
      val service = new TestServiceImpl
      val cached = makeCache(service)
      cached.getThing(1)
      cached.getThing(1)
      service.calls should equal(1)
    }

    "Calls underlying impl only once even with multiple concurrent calls" in {
      val service = new TestServiceImpl
      val cached = makeCache(service)
      service.calls should equal(0)
      (1 to 20).foreach { x =>
        new Thread {
          override def run() = cached.getThing(1)
        }.start
      }
      Thread.sleep(200)
      service.calls should equal(1)
    }
  }

  def makeCache(service: TestService) = CachingProxy[TestService](CachingStrategy.cacheAllRefresh(10, 1), service)

}

trait TestService {
  def getThing(id: Int): String
}

class TestServiceImpl extends TestService{
  var calls = 0
  override def getThing(id: Int) = {
    this.synchronized {
      calls = calls + 1
    }
    Thread.sleep(100)
    id.toString
  }
}
