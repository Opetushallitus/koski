package fi.oph.koski.cache

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

import fi.oph.koski.util.Invocation
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration.Duration

class CacheSpec extends FreeSpec with Matchers {
  implicit val manager = GlobalCacheManager

  "Cache" - {
    "Without background refresh" - {
      val counter = new Counter()
      val cache = Cache.cacheAllNoRefresh("testcache", Duration(100, MILLISECONDS), 1)
      val invocation = Invocation(counter.inc _, "a")

      "First invocation" in {
        cache.apply(invocation) should equal("1")
      }

      "Second invocation: uses cached" in {
        cache.apply(invocation) should equal("1")
      }

      "After expiration: re-calculates value" in {
        Thread.sleep(200)
        cache.apply(invocation) should equal("2")
      }

    }
    "With background refresh" - {
      val counter = new Counter()
      val invocation = Invocation(counter.inc _, "a")
      val cache = Cache.cacheAllRefresh("testcache", Duration(100, MILLISECONDS), 1)

      "Returns stale value, triggering update on background" in {
        cache.apply(invocation) should equal("1")
        cache.apply(invocation) should equal("1")
        Thread.sleep(200)
        cache.apply(invocation) should equal("1")
        Thread.sleep(100)
        cache.apply(invocation) should equal("2")
      }
    }
  }

  class Counter {
    private var invocationCount = new AtomicInteger(0)

    def count = invocationCount.get

    def inc(value: String): String = {
      val v = invocationCount.incrementAndGet
      println("Increasing to " + v)
      Thread.sleep(10)
      println("Done increasing")
      "" + v
    }
  }
}
