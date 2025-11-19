package fi.oph.koski.cache

import fi.oph.koski.TestEnvironment
import fi.oph.koski.util.{Futures, Invocation}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.collection.parallel.CollectionConverters._

class CacheSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  implicit val manager = GlobalCacheManager

  "Cache" - {
    "Without background refresh" - {
      val counter = new Counter()
      val cache = ExpiringCache("testcache", 100.milliseconds, 100)

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
        val cache = RefreshingCache("testcache", 100.milliseconds, 2)
        cache(counter.incInvocation("a")) should equal("a1")
        cache(counter.incInvocation("b")) should equal("b1")
        cache(counter.incInvocation("c")) should equal("c1")
        cache(counter.incInvocation("a")) should equal("a2")
        cache(counter.incInvocation("c")) should equal("c1")
      }
      "Performs eviction only when maxSize exceeded by 10%" in {
        val counter = new Counter()
        val cache = RefreshingCache("testcache", 1.second, 10)
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
        cache.getEntry(counter.incInvocation("2.")).isDefined should equal(false)
        cache(counter.incInvocation("1.")) should equal("1.2")
        cache(counter.incInvocation("2.")) should equal("2.2")

      }
      "Refreshes all keys on background" in {
        val counter = new Counter()
        val cache = RefreshingCache("testcache", 100.milliseconds, 100)
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
        val cache = RefreshingCache("testcache", 100.days, 100).asInstanceOf[RefreshingCache]
        cache.callAsync(counter.incInvocation("a"))
        cache.callAsync(counter.incInvocation("b"))
        Thread.sleep(100)

        def getRefreshTime(key: String) = cache.getEntry(counter.incInvocation(key)).get.getScheduledRefreshTime

        getRefreshTime("a") should not equal(getRefreshTime("b"))
      }
      "When fetch fails" - {
        class TestException extends RuntimeException("testing")
        "Initial fetch -> throws exception and tries again on next call" in {
          val cache = RefreshingCache("testcache", 10.milliseconds, 10)
          var perform: (String => String) = {x: String => throw new TestException}
          val invocation = Invocation({ x: String => perform(x) }, "a")

          intercept[TestException] {
            cache.apply(invocation)
          }
          perform = {x: String => "hello"}
          Thread.sleep(50)
          cache.apply(invocation) should equal("hello")
        }
        "Initial fetch -> throws exception, schedules fetch" in {
          val cache = RefreshingCache("testcache", 10.milliseconds, 10)
          var perform: (String => String) = {x: String => throw new TestException}
          val invocation = Invocation({ x: String => perform(x) }, "a")

          intercept[TestException] {
            cache.apply(invocation)
          }
          perform = {x: String => "hello"}
          Thread.sleep(50)
          val currentValue = cache.getEntry(invocation).get.valueFuture
          Futures.await(currentValue) should equal("hello")
        }
        "Background fetch -> tries again after cache period" in {
          val cache = RefreshingCache("testcache", 10.milliseconds, 10)
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
        "Throws exception from underlying impl without wrapping by Java reflection APIs" in {
          val cache = RefreshingCache("testcache", 10.milliseconds, 10)
          val counter = new Counter()
          val invocation = counter.incInvocation("666")
          val caught = intercept[Exception] {
            cache.apply(invocation)
          }
          assert(caught.getClass.getName == "java.lang.Exception")
          assert(caught.getMessage == "Test exception")
          cache.invalidateCache()
        }
      }
      "Multiple clients requesting at same time -> fetch only once" in {
        val counter = new Counter()
        val cache = RefreshingCache("testcache", 100.days, 100)
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


}
