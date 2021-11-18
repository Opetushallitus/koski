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
  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp, succeeded: Option[Boolean]): Unit

  protected def durationInSeconds(start: Timestamp, end: Timestamp): Double = (end.getTime - start.getTime) / 1000.0
}

class MockCloudWatchMetricsService extends CloudWatchMetricsService with Logging {
  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp, succeeded: Option[Boolean]): Unit = {
    val seconds = durationInSeconds(start, end)
    logger.debug(s"Mocking cloudwatch metric: raportointikanta loading took $seconds seconds. Succeeded: ${succeeded.getOrElse("None")}")
  }
}

class AwsCloudWatchMetricsService extends CloudWatchMetricsService with Logging {
  private val client = CloudWatchClient.builder().build()

  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp, succeeded: Option[Boolean]): Unit = {
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

    succeeded.map(succeeded =>  {

      val dimensionSucceededCounter = Dimension.builder()
        .name("SUCCEEDED_COUNT")
        .value("COUNT")
        .build()

      val successCount =  if(succeeded) {
        1.0
      } else {
        0.0
      }
      logger.info(s"Sending success counter with value: $successCount")

      val metricSuccessCount = MetricDatum.builder()
        .metricName("RAPORTOINTIKANTALOADER_SUCCESS_COUNT")
        .unit(StandardUnit.COUNT)
        .value(successCount)
        .dimensions(dimensionSucceededCounter)
        .build()

      client.putMetricData(PutMetricDataRequest.builder().metricData(metricSuccessCount).namespace(namespace).build())
    })
  }
}
