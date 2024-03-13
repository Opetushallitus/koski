package fi.oph.koski.cloudwatch


import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.{Dimension, MetricDatum, PutMetricDataRequest, StandardUnit}

import java.sql.Timestamp

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

  def putQueuedQueryMetric(queryState: String): Unit

  protected def durationInSeconds(start: Timestamp, end: Timestamp): Double = (end.getTime - start.getTime) / 1000.0
}

class MockCloudWatchMetricsService extends CloudWatchMetricsService with Logging {
  def putRaportointikantaLoadtime(start: Timestamp, end: Timestamp, succeeded: Option[Boolean]): Unit = {
    val seconds = durationInSeconds(start, end)
    logger.debug(s"Mocking cloudwatch metric: raportointikanta loading took $seconds seconds. Succeeded: ${succeeded.getOrElse("None")}")
  }

  def putQueuedQueryMetric(queryState: String): Unit = {
    logger.debug(s"Mocking cloudwatch metric: Queries -> State -> ${queryState.capitalize} with value 1.0 sent")
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

  def putQueuedQueryMetric(queryState: String): Unit = {
    val namespace = "Queries"

    val counterDimension = Dimension.builder()
      .name("State")
      .value("State")
      .build()

    val metric = MetricDatum.builder()
      .metricName(queryState.toUpperCase)
      .unit(StandardUnit.COUNT)
      .value(1.0)
      .dimensions(counterDimension)
      .build()

    val request = PutMetricDataRequest.builder()
      .metricData(metric)
      .namespace(namespace)
      .build()

    client.putMetricData(request)
  }

}
