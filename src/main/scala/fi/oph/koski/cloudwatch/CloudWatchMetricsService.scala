package fi.oph.koski.cloudwatch


import com.typesafe.config.Config

import java.sql.Timestamp
import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{Dimension, MetricDatum, PutMetricDataRequest, StandardUnit}

object CloudWatchMetricsService {
  def apply(config: Config): CloudWatchMetricsService = {
    if (Environment.isServerEnvironment(config)) {
      new AwsCloudWatchMetricsService
    } else {
      new MockCloudWatchMetricsService
    }
  }
}

trait CloudWatchMetricsService {
  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp): Unit

  protected def durationInSeconds(start: Timestamp, end: Timestamp): Double = (end.getTime - start.getTime) / 1000.0
}

class MockCloudWatchMetricsService extends CloudWatchMetricsService with Logging {
  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp): Unit = {
    val seconds = durationInSeconds(start, end)
    logger.debug(s"Mocking cloudwatch metric: raportointikanta loading took $seconds seconds")
  }
}

class AwsCloudWatchMetricsService extends CloudWatchMetricsService {
  private val client = CloudWatchClient.builder().build()

  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp): Unit = {
    val namespace = "Raportointikanta"

    val dimension = Dimension.builder()
      .name("LOAD_TIMES")
      .value("TIME")
      .build()

    val metric = MetricDatum.builder()
      .metricName("RAPORTOINTIKANTA_LOAD_TIME")
      .unit(StandardUnit.SECONDS)
      .value(durationInSeconds(start, end))
      .dimensions(dimension)
      .build()

    client.putMetricData(PutMetricDataRequest.builder().metricData(metric).namespace(namespace).build())
  }
}
