package fi.oph.koski.omaopintopolkuloki

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBClientBuilder}
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging

object AuditLogDynamoDB extends Logging {
  lazy private val dynamoClient = {
    if (Environment.isLocalDevelopmentEnvironment) {
      val client = AmazonDynamoDBClientBuilder
          .standard()
          .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8000", "local"))
          .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("mock", "mock")))
          .build()

      AuditLogMockData.createTable(client)
      AuditLogMockData.insertMockData(client)
      client
    } else {
      logger.info("Connecting to DynamoDB running in AWS")
      AmazonDynamoDBClientBuilder.defaultClient()
    }
  }

  val AuditLogTableName = "AuditLog"
  lazy val db: DynamoDB = new DynamoDB(dynamoClient)
}
