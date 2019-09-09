package fi.oph.koski.ytr

import com.typesafe.config.Config
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

class YtrS3(config: Config) {
  private val accessKeyId = config.getString("ytr.aws.accessKeyId")
  private val secretAccessKey =   config.getString("ytr.aws.secretAccessKey")
  private val roleArn = config.getString("ytr.aws.roleArn")
  private val externalId = config.getString("ytr.aws.externalId")

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

  val client: S3Client = S3Client.builder.region(Region.EU_NORTH_1).credentialsProvider(assumeRole).build
}
