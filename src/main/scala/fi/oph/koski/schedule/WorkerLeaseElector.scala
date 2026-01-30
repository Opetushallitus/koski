package fi.oph.koski.schedule

import fi.oph.koski.executors.NamedThreadFactory
import fi.oph.koski.log.Logging

import java.time.Duration
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

class WorkerLeaseElector(
  repository: WorkerLeaseRepository,
  name: String,
  holderId: String,
  slots: Int,
  leaseDuration: Duration,
  heartbeatInterval: Duration
) extends Logging {
  private var executor: ScheduledExecutorService = newExecutor()
  @volatile private var currentSlot: Option[Int] = None
  @volatile private var started = false

  def hasLease: Boolean = currentSlot.isDefined

  def start(onAcquired: Int => Unit = _ => (), onLost: Int => Unit = _ => ()): Unit = synchronized {
    if (executor.isShutdown || executor.isTerminated) {
      executor = newExecutor()
    }
    if (!started) {
      started = true
      val intervalMillis = math.max(100, heartbeatInterval.toMillis)
      executor.scheduleAtFixedRate(() => tick(onAcquired, onLost), 0, intervalMillis, TimeUnit.MILLISECONDS)
    }
  }

  def shutdown(): Unit = synchronized {
    currentSlot.foreach(slot => repository.release(name, slot, holderId))
    executor.shutdown()
    currentSlot = None
    started = false
  }

  private def tick(onAcquired: Int => Unit, onLost: Int => Unit): Unit = synchronized {
    currentSlot match {
      case Some(slot) =>
        val renewed = repository.tryAcquireOrRenew(name, slot, holderId, leaseDuration)
        if (!renewed) {
          currentSlot = None
          onLost(slot)
        }
      case None =>
        val slotOrder = preferredSlotOrder
        slotOrder.find(slot => repository.tryAcquireOrRenew(name, slot, holderId, leaseDuration)) match {
          case Some(slot) =>
            currentSlot = Some(slot)
            onAcquired(slot)
          case None =>
        }
    }
  }

  private def preferredSlotOrder: Seq[Int] = {
    if (slots <= 1) {
      Seq(1)
    } else {
      val start = Math.floorMod(holderId.hashCode, slots) + 1
      (0 until slots).map(i => ((start - 1 + i) % slots) + 1)
    }
  }

  private def newExecutor(): ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor(NamedThreadFactory(s"lease-$name"))
}
