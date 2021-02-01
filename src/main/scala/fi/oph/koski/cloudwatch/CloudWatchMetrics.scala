package fi.oph.koski.cloudwatch


import java.sql.Timestamp

import fi.oph.common.log.Logging
import fi.oph.koski.config.Environment
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{Dimension, MetricDatum, PutMetricDataRequest, StandardUnit}

object CloudWatchMetrics extends Logging {
  private lazy val client = CloudWatchClient.builder().build()

  def putRaportointikantaLoadtime(start: Timestamp, completed: Timestamp) = {
    val timeInSeconds = (completed.getTime - start.getTime) / 1000.0

    if (Environment.isLocalDevelopmentEnvironment) {
      logger.info(s"Mocking cloudwatch metric: raportointikanta loading took $timeInSeconds seconds")
    } else {
      putRaportointikantaLoadtimeToAWS(timeInSeconds)
    }
  }

  private def putRaportointikantaLoadtimeToAWS(seconds: Double) = {
    val namespace = "Raportointikanta"

    val dimension = Dimension.builder()
      .name("LOAD_TIMES")
      .value("TIME")
      .build()

    val metric = MetricDatum.builder()
      .metricName("RAPORTOINTIKANTA_LOAD_TIME")
      .unit(StandardUnit.SECONDS)
      .value(seconds)
      .dimensions(dimension)
      .build()

    client.putMetricData(PutMetricDataRequest.builder().metricData(metric).namespace(namespace).build())
  }
}
