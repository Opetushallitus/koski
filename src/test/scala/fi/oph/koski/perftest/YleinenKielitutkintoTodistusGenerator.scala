package fi.oph.koski.perftest

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.log.Logging
import fi.oph.koski.util.EnvVariables
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.tailrec
import scala.util.Random

// Dokumentaatio: Katso /documentation/todistus.md
object YleinenKielitutkintoTodistusGenerator extends App with EnvVariables with Logging {

  private val threadCount = env("PERFTEST_THREADS", "5").toInt
  private val roundCount = env("PERFTEST_ROUNDS", "10").toInt
  private val warmupRounds = env("WARMUP_ROUNDS", "5").toInt
  private val successThreshold = env("PERFTEST_SUCCESS_THRESHOLD_PERCENTAGE", "95").toInt
  private val minThroughput = env("PERFTEST_MIN_THROUGHPUT", "0.5").toDouble

  logger.info(s"Starting YKI todistus performance test: $threadCount threads, $roundCount rounds per thread, $warmupRounds warmup rounds")

  val scenario = new YkiTodistusPerfTestScenario()
  val stats = new PerfTestStats()

  // Warmup phase
  logger.info(s"Running $warmupRounds warmup rounds...")
  runRounds(warmupRounds, isWarmup = true)

  // Actual test phase
  logger.info(s"Running ${threadCount * roundCount} test rounds...")
  val startTime = System.currentTimeMillis()
  runRounds(threadCount * roundCount, isWarmup = false)
  val endTime = System.currentTimeMillis()

  // Report results
  val totalDuration = (endTime - startTime) / 1000.0
  val successPercentage = if (stats.totalCount > 0) (stats.successCount * 100) / stats.totalCount else 0
  val actualThroughput = stats.totalCount / totalDuration

  logger.info("=" * 80)
  logger.info("YKI Todistus Generation Performance Test Results")
  logger.info("=" * 80)
  stats.reportByOperation()
  logger.info(s"Total operations: ${stats.totalCount}")
  logger.info(s"Successful: ${stats.successCount} (${successPercentage}%)")
  logger.info(s"Failed: ${stats.failedCount}")
  logger.info(s"Total duration: ${totalDuration}s")
  logger.info(f"Average throughput: $actualThroughput%.2f operations/sec")
  logger.info("=" * 80)

  // Check thresholds
  var testPassed = true

  if (successPercentage < successThreshold) {
    logger.error(s"Test failed: only $successPercentage% succeeded, required $successThreshold%")
    testPassed = false
  } else {
    logger.info(s"Success rate check passed: $successPercentage% succeeded (threshold: $successThreshold%)")
  }

  if (actualThroughput < minThroughput) {
    logger.error(f"Test failed: throughput $actualThroughput%.2f ops/sec, required $minThroughput%.2f ops/sec")
    testPassed = false
  } else {
    logger.info(f"Throughput check passed: $actualThroughput%.2f ops/sec (threshold: $minThroughput%.2f ops/sec)")
  }

  if (testPassed) {
    logger.info("All checks passed!")
    System.exit(0)
  } else {
    logger.error("Test failed - see errors above")
    System.exit(1)
  }

  private def runRounds(rounds: Int, isWarmup: Boolean): Unit = {
    val counter = new AtomicInteger(0)
    val threads = (1 to threadCount).map { threadId =>
      new Thread(new Runnable {
        override def run(): Unit = {
          Iterator.continually(counter.getAndIncrement()).takeWhile(_ < rounds).foreach { round =>
            try {
              val (success, duration, operation) = scenario.executeTodistusWorkflow()
              if (!isWarmup) {
                stats.record(operation, success, duration)
              }
            } catch {
              case e: Exception =>
                logger.error(s"Thread $threadId round $round failed: ${e.getMessage}")
                if (!isWarmup) {
                  stats.record("ERROR", success = false, 0)
                }
            }
          }
        }
      }, s"perftest-$threadId")
    }

    threads.foreach(_.start())
    threads.foreach(_.join())
  }
}

object YkiTodistusPerfTestScenario {
  private sealed trait JobOutcome
  private case object JobCompleted extends JobOutcome
  private case class JobFailed(state: String, error: Option[String]) extends JobOutcome
  private case class JobInUnexpectedState(state: String) extends JobOutcome
  private case object JobTimedOut extends JobOutcome

  private case class JobStatus(state: String, error: Option[String])
}

class YkiTodistusPerfTestScenario extends KoskidevHttpSpecification with Matchers with Logging with EnvVariables {
  import YkiTodistusPerfTestScenario._

  implicit val formats: org.json4s.Formats = org.json4s.DefaultFormats

  private val pollIntervalMs = env("YKI_TODISTUS_POLL_INTERVAL_MS", "2000").toLong
  private val maxWaitMs = env("YKI_TODISTUS_MAX_WAIT_MS", "300000").toLong
  private val digitalWeight = env("YKI_TODISTUS_DIGITAL_WEIGHT", "95").toInt
  private val oidsFile = env("YKI_TODISTUS_OIDS_FILE", "src/test/resources/yki_perftest_opiskeluoikeus_oids.txt")

  private val oidProvider = new RandomYkiOpiskeluoikeusOid(oidsFile)

  // Todistus variants with weighted distribution
  private val digitalVariants = List("fi", "sv", "en")
  private val printVariants = List(
    "fi_tulostettava_uusi", "fi_tulostettava_paivitys",
    "sv_tulostettava_uusi", "sv_tulostettava_paivitys",
    "en_tulostettava_uusi", "en_tulostettava_paivitys"
  )

  /**
    * Executes the full todistus workflow: generate → poll → download
    * Returns (success, durationMs, operationName)
    */
  def executeTodistusWorkflow(): (Boolean, Long, String) = {
    val startTime = System.currentTimeMillis()
    val opiskeluoikeusOid = oidProvider.next()
    val variant = selectVariant()

    def elapsed = System.currentTimeMillis() - startTime

    try {
      // Step 1: Generate todistus
      val jobId = generateTodistus(variant, opiskeluoikeusOid)
      val jobDescription = s"variant=$variant, oid=$opiskeluoikeusOid, jobId=$jobId"

      // Step 2: Poll until complete
      pollUntilComplete(jobId) match {
        case JobCompleted =>
          // Step 3: Download PDF
          if (downloadPdf(jobId)) {
            (true, elapsed, s"todistus/$variant")
          } else {
            logger.warn(s"Failed to download PDF for $jobDescription")
            (false, elapsed, s"todistus/$variant/DOWNLOAD_FAILED")
          }
        case JobFailed(state, error) =>
          logger.warn(s"Todistus job ended in state $state for $jobDescription: ${error.getOrElse("(no error message)")}")
          (false, elapsed, s"todistus/$variant/$state")
        case JobInUnexpectedState(state) =>
          logger.warn(s"Unexpected job state $state for $jobDescription")
          (false, elapsed, s"todistus/$variant/UNEXPECTED_STATE")
        case JobTimedOut =>
          logger.warn(s"Todistus generation timed out after $maxWaitMs ms for $jobDescription")
          (false, elapsed, s"todistus/$variant/TIMEOUT")
      }
    } catch {
      case e: Exception =>
        logger.error(e)(s"Workflow failed for variant=$variant, oid=$opiskeluoikeusOid: ${e.getClass.getName}: ${e.getMessage}")
        (false, elapsed, s"todistus/$variant/EXCEPTION")
    }
  }

  private def selectVariant(): String = {
    val random = Random.nextInt(100)
    if (random < digitalWeight) {
      // 95% chance: pick digital variant
      digitalVariants(Random.nextInt(digitalVariants.length))
    } else {
      // 5% chance: pick print variant
      printVariants(Random.nextInt(printVariants.length))
    }
  }

  private def generateTodistus(variant: String, opiskeluoikeusOid: String): String = {
    get(s"api/todistus/generate/$variant/$opiskeluoikeusOid", headers = authHeaders()) {
      status should (be(200) or be(201))
      val json = org.json4s.jackson.JsonMethods.parse(body)
      (json \ "id").extractOpt[String] match {
        case Some(id) => id
        case None => throw new RuntimeException(s"No job ID in response: $body")
      }
    }
  }

  private def pollUntilComplete(jobId: String): JobOutcome = {
    val deadline = System.currentTimeMillis() + maxWaitMs
    val completedStates = Set("COMPLETED")
    val failedStates = Set("ERROR", "EXPIRED")
    val pollingStates = Set("QUEUED", "GATHERING_INPUT", "GENERATING_RAW_PDF", "SAVING_RAW_PDF", "STAMPING_PDF", "SAVING_STAMPED_PDF")

    @tailrec
    def poll(): JobOutcome = {
      val jobStatus = getJobStatus(jobId)
      if (completedStates.contains(jobStatus.state)) {
        JobCompleted
      } else if (failedStates.contains(jobStatus.state)) {
        JobFailed(jobStatus.state, jobStatus.error)
      } else if (pollingStates.contains(jobStatus.state)) {
        if (System.currentTimeMillis() >= deadline) {
          JobTimedOut
        } else {
          Thread.sleep(pollIntervalMs)
          poll()
        }
      } else {
        JobInUnexpectedState(jobStatus.state)
      }
    }

    poll()
  }

  private def getJobStatus(jobId: String): JobStatus = {
    get(s"api/todistus/status/$jobId", headers = authHeaders()) {
      status should be(200)
      val json = org.json4s.jackson.JsonMethods.parse(body)
      (json \ "state").extractOpt[String] match {
        case Some(state) => JobStatus(state, (json \ "error").extractOpt[String])
        case None => throw new RuntimeException(s"No state in response: $body")
      }
    }
  }

  private def downloadPdf(jobId: String): Boolean = {
    get(s"todistus/download/$jobId", headers = authHeaders()) {
      status == 200
    }
  }
}

class PerfTestStats {
  private val stats = new scala.collection.mutable.HashMap[String, OperationStats]()
  private val lock = new Object()

  var successCount: Int = 0
  var failedCount: Int = 0

  def totalCount: Int = successCount + failedCount

  def record(operation: String, success: Boolean, durationMs: Long): Unit = lock.synchronized {
    if (success) successCount += 1 else failedCount += 1

    val opStats = stats.getOrElseUpdate(operation, new OperationStats(operation))
    opStats.record(success, durationMs)
  }

  def reportByOperation(): Unit = {
    stats.values.toList.sortBy(_.operation).foreach { opStats =>
      val avgDuration = if (opStats.count > 0) opStats.totalDurationMs / opStats.count else 0
      val successPct = if (opStats.count > 0) (opStats.successCount * 100) / opStats.count else 0
      println(f"  ${opStats.operation}%-40s Count: ${opStats.count}%5d, Success: ${opStats.successCount}%5d ($successPct%3d%%), Avg: ${avgDuration}%6d ms")
    }
  }
}

class OperationStats(val operation: String) {
  var count: Int = 0
  var successCount: Int = 0
  var totalDurationMs: Long = 0

  def record(success: Boolean, durationMs: Long): Unit = {
    count += 1
    if (success) successCount += 1
    totalDurationMs += durationMs
  }
}
