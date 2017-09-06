package fi.oph.koski.perftest

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.log.Logging

abstract class PerfTestScenario extends KoskidevHttpSpecification with Logging {
  def roundCount: Int = sys.env.getOrElse("PERFTEST_ROUNDS", "10").toInt
  def warmupRoundCount: Int = sys.env.getOrElse("WARMUP_ROUNDS", "20").toInt
  def serverCount: Int = sys.env.getOrElse("KOSKI_SERVER_COUNT", "2").toInt
  def threadCount: Int = sys.env.getOrElse("PERFTEST_THREADS", "10").toInt
  def operation(round: Int): List[Operation]
  def name = getClass.getSimpleName

  override def logger = super.logger
}
