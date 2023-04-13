package fi.oph.koski.ytr

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, KoskiApplication, SecretsManager}
import fi.oph.koski.log.NotLoggable
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

case class YtrS3Config(accessKeyId: String, secretAccessKey: String, roleArn: String, externalId: String, bucket: String) extends NotLoggable

class YtrS3(config: YtrS3Config) {
  private val YtrS3Config(accessKeyId, secretAccessKey, roleArn, externalId, ytrS3Bucket) = config
  private val credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))

  private val assumeYTRRole = AssumeRoleRequest.builder
    .roleArn(roleArn)
    .externalId(externalId)
    .roleSessionName("koski-ytr-role")
    .build

  private val stsClient: StsClient = StsClient.builder.region(Region.EU_WEST_1).credentialsProvider(credentials).build

  private val assumeRole = StsAssumeRoleCredentialsProvider.builder
    .stsClient(stsClient)
    .refreshRequest(assumeYTRRole)
    .build

  val bucket: String = ytrS3Bucket

  val client: S3Client = S3Client.builder.region(Region.EU_NORTH_1).credentialsProvider(assumeRole).build
}

object YtrS3Config {
  def getEnvironmentConfig(application: KoskiApplication): YtrS3Config = {
    if (Environment.usesAwsSecretsManager) this.fromSecretsManager else this.fromConfig(application.config)
  }

  def fromConfig(config: Config): YtrS3Config = YtrS3Config(
    config.getString("ytr.aws.accessKeyId"),
    config.getString("ytr.aws.secretAccessKey"),
    config.getString("ytr.aws.roleArn"),
    config.getString("ytr.aws.externalId"),
    config.getString("ytr.aws.bucket")
  )

  def fromSecretsManager: YtrS3Config = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("YTR S3 secrets", "YTR_S3_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[YtrS3Config](secretId)
  }
}
