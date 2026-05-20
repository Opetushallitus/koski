package fi.oph.koski.virta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.executors.NamedThreadFactory
import fi.oph.koski.log.Logging

import java.time.{ZoneId, ZonedDateTime}
import java.util.concurrent.{Executors, TimeUnit}

class VirtaCacheInvalidationScheduler(application: KoskiApplication) extends Logging {
  private val helsinkiZone = ZoneId.of("Europe/Helsinki")
  private val executor = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("virta-cache-invalidation"))

  def start(): Unit = {
    val initialDelay = secondsUntilNext6AM()
    logger.info(s"Virta-välimuistin tyhjennys ajastettu: ${initialDelay / 3600}h ${(initialDelay % 3600) / 60}min kuluttua (klo 06:00 Helsinki)")
    executor.scheduleAtFixedRate(
      () => invalidate(),
      initialDelay,
      24 * 3600,
      TimeUnit.SECONDS
    )
  }

  def shutdown(): Unit = executor.shutdown()

  private def invalidate(): Unit = {
    val virtaCaches = application.cacheManager.caches.filter(_.name.startsWith("Virta"))
    logger.info(s"Tyhjennetään Virta-välimuistit (${virtaCaches.map(_.name).mkString(", ")})")
    virtaCaches.foreach(_.invalidateCache())
  }

  private def secondsUntilNext6AM(): Long = {
    val now = ZonedDateTime.now(helsinkiZone)
    val todayAt6 = now.withHour(6).withMinute(0).withSecond(0).withNano(0)
    val next6AM = if (now.isBefore(todayAt6)) todayAt6 else todayAt6.plusDays(1)
    java.time.Duration.between(now, next6AM).getSeconds
  }
}
