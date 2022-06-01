package fi.oph.koski.perftest

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.log.Logging
import fi.oph.koski.util.EnvVariables

trait PerfTestScenario extends KoskidevHttpSpecification with EnvVariables with Logging {
  def roundCount: Int = env("PERFTEST_ROUNDS", "10").toInt
  def warmupRoundCount: Int = env("WARMUP_ROUNDS", "20").toInt
  def serverCount: Int = env("KOSKI_SERVER_COUNT", "2").toInt
  def threadCount: Int = env("PERFTEST_THREADS", "10").toInt
  def operation(round: Int): List[Operation]
  def maximumExpectedDurationMs: Int = Int.MaxValue
  def name = getClass.getSimpleName
  def readBody: Boolean = false
  def bodyValidator: Boolean = true

  override def logger = super.logger
}
