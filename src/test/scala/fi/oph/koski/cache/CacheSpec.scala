package fi.oph.koski.cache

import java.util.concurrent.TimeUnit._

import fi.oph.koski.util.Invocation
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration.Duration

class CacheSpec extends FreeSpec with Matchers {
  implicit val manager = GlobalCacheManager

  "Cache" - {
    "Without background refresh" - {
      val counter = new Counter()
      val cache = Cache.cacheAllNoRefresh("testcache", Duration(100, MILLISECONDS), 100)

      "Caching" in {
        cache(counter.incInvocation("a")) should equal("a1")
        cache(counter.incInvocation("a")) should equal("a1")
        Thread.sleep(200)
        cache(counter.incInvocation("a")) should equal("a2")
      }
    }
    "With background refresh" - {
      "Evicts oldest entry" in {
        val counter = new Counter()
        val cache = Cache.cacheAllRefresh("testcache", Duration(100, MILLISECONDS), 2)
        cache(counter.incInvocation("a")) should equal("a1")
        cache(counter.incInvocation("b")) should equal("b1")
        cache(counter.incInvocation("c")) should equal("c1")
        cache(counter.incInvocation("a")) should equal("a2")
        cache(counter.incInvocation("c")) should equal("c1")
      }
      "Performs eviction only when maxSize exceeded by 10%" in {
        val counter = new Counter()
        val cache = Cache.cacheAllRefresh("testcache", Duration(1000, MILLISECONDS), 10)
        (1 to 11) foreach { n =>
          cache(counter.incInvocation(n + ".")) should equal(n + ".1")
          Thread.sleep(1)
        }
        cache.getEntry(counter.incInvocation("1.")).isDefined should equal(true) // Eviction threshold not exceeded yet
        (12 to 12) foreach { n =>
          cache(counter.incInvocation(n + ".")) should equal(n + ".1")
        }
        // 2 first entries evicted
        cache.getEntry(counter.incInvocation("1.")).isDefined should equal(false)
        cache.getEntry(counter.incInvocation("1.")).isDefined should equal(false)
        cache(counter.incInvocation("1.")) should equal("1.2")
        cache(counter.incInvocation("2.")) should equal("2.2")

      }
      "Refreshes all keys on background" in {
        val counter = new Counter()
        val cache = Cache.cacheAllRefresh("testcache", Duration(100, MILLISECONDS), 100)
        cache(counter.incInvocation("a")) should equal("a1")
        cache(counter.incInvocation("a")) should equal("a1")
        cache(counter.incInvocation("b")) should equal("b1")
        Thread.sleep(150)
        // Test that refresh has happened on the background
        counter.get("a") should equal(2)
        counter.get("b") should equal(2)
        // Also verify that refreshed values are gotten through the cache
        cache(counter.incInvocation("a")) should equal("a2")
        cache(counter.incInvocation("b")) should equal("b2")
      }
      "Randomizes scheduled refresh time" in {
        val counter = new Counter()
        val cache = Cache.cacheAllRefresh("testcache", Duration(100, DAYS), 100).asInstanceOf[RefreshingCache]
        cache.callAsync(counter.incInvocation("a"))
        cache.callAsync(counter.incInvocation("b"))

        def getRefreshTime(key: String) = cache.getEntry(counter.incInvocation(key)).get.getScheduledRefreshTime

        getRefreshTime("a") should not equal(getRefreshTime("b"))
      }
      "When fetch fails" - {
        class TestException extends RuntimeException("testing")
        val cache = Cache.cacheAllRefresh("testcache", Duration(10, MILLISECONDS), 10)
        "Initial fetch -> throws exception" in {
          intercept[TestException] {
            cache.apply(Invocation({x: String => throw new TestException}, "a"))
          }
        }
        "Background fetch -> tries again after cache period" in {
          var result = {x: String => x}
          val invocation = Invocation({ x: String => result(x) }, "b")
          cache.apply(invocation) should equal("b")
          result = {x: String => throw new TestException}
          Thread.sleep(50)
          cache.apply(invocation) should equal("b")
          result = {x: String => x + "2"}
          Thread.sleep(50)
          cache.apply(invocation) should equal("b2")
        }
      }
      "Multiple clients requesting at same time -> fetch only once" in {
        val counter = new Counter()
        val cache = Cache.cacheAllRefresh("testcache", Duration(100, DAYS), 100)
        (1 to 20).par.map { n => cache.apply(counter.incInvocation("a")) }.toList.distinct should equal(List("a1"))
      }
    }
  }
  /*
    Guava cache just doesn't cut it!

    "Guava caches" in {
      val counter = new Counter()

      val cacheBuilder = CacheBuilder
        .newBuilder()
        .recordStats()
        .maximumSize(2)

      val cache: LoadingCache[String, String] = cacheBuilder.build(new CacheLoader[String, String] {
        override def load(key: String): String = counter.inc(key)
      })

      cache.apply("a") should equal("a1")
      cache.apply("b") should equal("b1")
      cache.apply("c") should equal("c1")
      cache.apply("c") should equal("c1")
      cache.apply("a") should equal("a2")
      cache.asMap().get("a") should equal("a2")
      cache.asMap().get("b") should equal(null) // now contains a and c, "a" being most recent
      cache.refresh("c")
      Thread.sleep(100)
      cache.apply("b") should equal("b2")
      cache.asMap().get("c") should equal(null) // c1 should be evicted regardless of the refresh call earlier
      cache.asMap().get("a") should equal("a2") // a2 should not be evicted
    }
  }
  */

  class Counter {
    private var counts: Map[String, Int] = Map.empty

    private val incFunc = this.inc _

    def inc(key: String): String = synchronized {
      val current = counts.getOrElse(key, 0)
      val v = current + 1
      counts += (key -> v)
      Thread.sleep(10)
      //println("Generated " + key + "=" + v)
      key + v
    }

    def get(key: String) = synchronized {
      counts.getOrElse(key, 0)
    }

    def incInvocation(key: String) = Invocation(incFunc, key)
  }
}
