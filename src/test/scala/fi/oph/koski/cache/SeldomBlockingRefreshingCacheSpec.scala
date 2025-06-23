package fi.oph.koski.cache

import fi.oph.koski.TestEnvironment
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.DurationInt

class SeldomBlockingRefreshingCacheSpec extends AnyFreeSpec with TestEnvironment with Matchers with BeforeAndAfterAll {
  implicit val manager: GlobalCacheManager.type = GlobalCacheManager

  var executor: ExecutorService = _
  var executionContext: ExecutionContextExecutor = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    executor = Executors.newCachedThreadPool()
    executionContext = ExecutionContext.fromExecutor(executor)
  }

  override def afterAll(): Unit = {
    super.afterAll()

    executor.shutdown()
  }

  "Returns previous value and triggers background refresh" in {
    val counter = new Counter()

    val cache = new SeldomBlockingRefreshingCache(
      "testcache",
      SeldomBlockingRefreshingCache.Params(
        duration = 500.milliseconds,
        refreshDuration = 100.milliseconds,
        maxSize = 10,
        executor = executionContext
      )
    )

    cache(counter.incInvocation("a")) should equal("a1")
    cache(counter.incInvocation("a")) should equal("a1")
    Thread.sleep(150)
    // Returns old value, triggers refresh
    cache(counter.incInvocation("a")) should equal("a1")
    Thread.sleep(50)
    // Returns refreshed value
    cache(counter.incInvocation("a")) should equal("a2")
  }

  "After hard expire duration does not return previous value but retrieves new value immediately" in {
    val counter = new Counter()

    val cache = new SeldomBlockingRefreshingCache(
      "testcache",
      SeldomBlockingRefreshingCache.Params(
        duration = 100.milliseconds,
        refreshDuration = 30.milliseconds,
        maxSize = 10,
        executor = executionContext
      )
    )

    cache(counter.incInvocation("a")) should equal("a1")
    cache(counter.incInvocation("a")) should equal("a1")
    Thread.sleep(150)
    // Refreshes and retrieves new value synchronously
    cache(counter.incInvocation("a")) should equal("a2")
  }

  "Does not eat exception at initial call" in {
    val counter = new Counter()

    val cache = new SeldomBlockingRefreshingCache(
      "testcache",
      SeldomBlockingRefreshingCache.Params(
        duration = 500.milliseconds,
        refreshDuration = 100.milliseconds,
        maxSize = 10,
        executor = executionContext
      )
    )

    val invocation = counter.incInvocation("666")
    val caught = intercept[Exception] {
      cache.apply(invocation)
    }
    assert(caught.getClass.getName == "java.util.concurrent.ExecutionException")
    assert(caught.getMessage == "java.lang.Exception: Test exception")

    assert(caught.getCause.getClass.getName == "java.lang.Exception")
    assert(caught.getCause.getMessage == "Test exception")
  }

  "Invalidates the cache and stops returning stale values when exception is thrown during background refresh" in {
    val counter = new Counter()

    val cache = new SeldomBlockingRefreshingCache(
      "testcache",
      SeldomBlockingRefreshingCache.Params(
        duration = 500.milliseconds,
        refreshDuration = 100.milliseconds,
        maxSize = 10,
        executor = executionContext
      )
    )

    val invocation = counter.incInvocation("777")
    cache.apply(invocation) should equal("7771")
    Thread.sleep(100)
    cache.apply(invocation) should equal("7771")
    Thread.sleep(100)

    val caught = intercept[Exception] {
      cache.apply(invocation)
    }
    assert(caught.getClass.getName == "java.util.concurrent.ExecutionException")
    assert(caught.getMessage == "java.lang.Exception: Test exception")

    assert(caught.getCause.getClass.getName == "java.lang.Exception")
    assert(caught.getCause.getMessage == "Test exception")
  }

  "Recovers from exception and continues returning working values" in {
    val counter = new Counter()

    val cache = new SeldomBlockingRefreshingCache(
      "testcache",
      SeldomBlockingRefreshingCache.Params(
        duration = 500.milliseconds,
        refreshDuration = 100.milliseconds,
        maxSize = 10,
        executor = executionContext
      )
    )

    val invocation = counter.incInvocation("888")
    cache.apply(invocation) should equal("8881")
    Thread.sleep(100)
    cache.apply(invocation) should equal("8881")
    Thread.sleep(100)

    val caught = intercept[Exception] {
      cache.apply(invocation)
    }
    assert(caught.getClass.getName == "java.util.concurrent.ExecutionException")
    assert(caught.getMessage == "java.lang.Exception: Test exception")

    assert(caught.getCause.getClass.getName == "java.lang.Exception")
    assert(caught.getCause.getMessage == "Test exception")

    Thread.sleep(100)
    cache.apply(invocation) should equal("8884")
  }

}
