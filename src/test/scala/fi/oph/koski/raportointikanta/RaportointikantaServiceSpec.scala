package fi.oph.koski.raportointikanta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.util.Wait
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import rx.Scheduler.Worker
import rx.functions.Action0
import rx.lang.scala.{Scheduler => ScalaScheduler}
import rx.{Scheduler, Subscription}

import java.util.concurrent.TimeUnit

class RaportointikantaServiceSpec extends AnyFreeSpec with Matchers with BeforeAndAfterAll with RaportointikantaTestMethods {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    tempRaportointiDb.dropAndCreateObjects()
  }

  "Loads raportointikanta is idempotent" in {
    val scheduler = new DelayedScheduler

    service.isLoading should be(false)

    service.loadRaportointikanta(force = false, scheduler) should be(true)

    service.isLoadComplete should be(false)
    service.isLoading should be(true)

    // doesn't do anything and returns false because load is already in progress
    service.loadRaportointikanta(force = false, scheduler) should be(false)

    scheduler.okToContinue = true
    Wait.until(service.isLoadComplete)

    service.isLoadComplete should be(true)
    service.isLoading should be(false)
  }

  "Loading can be forced to started again" in {
    val scheduler = new DelayedScheduler

    service.isLoading should be(false)
    service.loadRaportointikanta(force = false, scheduler) should be(true)
    service.isLoading should be(true)
    service.loadRaportointikanta(force = true, scheduler) should be(true)
    service.isLoading should be(true)
  }

  private lazy val service = KoskiApplicationForTests.raportointikantaService
}

class DelayedScheduler extends ScalaScheduler {
  var okToContinue: Boolean = false

  val asJavaScheduler: rx.Scheduler = new Scheduler {
    override def createWorker: Worker = new DelayedWorker
  }

  class DelayedWorker extends Worker {
    private var subscribed = true
    override def schedule(action: Action0): Subscription = {
      new Thread {
        override def run(): Unit = {
          Wait.until(okToContinue, timeoutMs = 5*60*1000)
          action.call()
        }
      }.start()
      this
    }

    override def schedule(action: Action0, delayTime: Long, unit: TimeUnit): Subscription = ???
    override def unsubscribe(): Unit = subscribed = false
    override def isUnsubscribed: Boolean = !subscribed
  }
}
