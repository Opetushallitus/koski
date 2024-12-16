package fi.oph.koski.omaopintopolkuloki

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

object AuditLogDynamoDB extends Logging {
  val AuditLogTableName = "AuditLog"

  def buildDb(config: Config): DynamoDbClient = buildClient(config)

  private def buildClient(config: Config): DynamoDbClient = {
    if (Environment.isServerEnvironment(config)) {
      logger.info("Connecting to DynamoDB running in AWS")
      DynamoDbClient.create()
    } else {
      val client = DynamoDbClient.builder()
        .endpointOverride(new java.net.URI("http://localhost:8000"))
        .region(Region.EU_WEST_1)
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("mock", "mock")))
        .build()

      AuditLogMockData.createTable(client)
      AuditLogMockData.insertMockData(client)
      client
    }
  }
}
