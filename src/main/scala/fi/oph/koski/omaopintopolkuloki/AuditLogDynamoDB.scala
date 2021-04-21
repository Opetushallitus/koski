package fi.oph.koski.omaopintopolkuloki

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging

object AuditLogDynamoDB extends Logging {
  val AuditLogTableName = "AuditLog"

  def buildDb(config: Config): DynamoDB = new DynamoDB(buildClient(config))

  private def buildClient(config: Config): AmazonDynamoDB = {
    if (Environment.isServerEnvironment(config)) {
      logger.info("Connecting to DynamoDB running in AWS")
      AmazonDynamoDBClientBuilder.defaultClient()
    } else {
      val client = AmazonDynamoDBClientBuilder
        .standard()
        .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8000", "local"))
        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("mock", "mock")))
        .build()

      AuditLogMockData.createTable(client)
      AuditLogMockData.insertMockData(client)
      client
    }
  }
}
