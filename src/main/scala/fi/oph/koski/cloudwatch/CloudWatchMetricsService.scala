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
  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp, lastUpdated: Timestamp): Unit

  protected def durationInSeconds(start: Timestamp, end: Timestamp): Double = (end.getTime - start.getTime) / 1000.0
}

class MockCloudWatchMetricsService extends CloudWatchMetricsService with Logging {
  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp, lastUpdated: Timestamp): Unit = {
    val seconds = durationInSeconds(start, end)
    logger.debug(s"Mocking cloudwatch metric: raportointikanta loading took $seconds seconds. Last updated : $lastUpdated")
  }
}

class AwsCloudWatchMetricsService extends CloudWatchMetricsService {
  private val client = CloudWatchClient.builder().build()

  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp, lastUpdated: Timestamp): Unit = {
    val namespace = "RaportointikantaLoader"

    val dimensionLoadTime = Dimension.builder()
      .name("LOAD_TIMES")
      .value("TIME")
      .build()

    val metricLoadTime = MetricDatum.builder()
      .metricName("RAPORTOINTIKANTALOADER_LOAD_TIME")
      .unit(StandardUnit.SECONDS)
      .value(durationInSeconds(start, end))
      .dimensions(dimensionLoadTime)
      .build()

    client.putMetricData(PutMetricDataRequest.builder().metricData(metricLoadTime).namespace(namespace).build())


    val dimensionUpdatedLast = Dimension.builder()
      .name("UPDATED_LAST")
      .value("TIME")
      .build()

    val metricUpdatedLast = MetricDatum.builder()
      .metricName("RAPORTOINTIKANTALOADER_UPDATED_LAST")
      .unit(StandardUnit.SECONDS)
      .value(durationInSeconds(lastUpdated, new Timestamp(System.currentTimeMillis())))
      .dimensions(dimensionUpdatedLast)
      .build()

    client.putMetricData(PutMetricDataRequest.builder().metricData(metricUpdatedLast).namespace(namespace).build())
  }
}
