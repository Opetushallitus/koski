package fi.oph.koski.todistus

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging
import software.amazon.awssdk.auth.credentials.{AwsSessionCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest

import java.net.URI
import scala.jdk.CollectionConverters._

class TodistusResultRepository(config: Config) extends Logging {
  val useAWS = Environment.isServerEnvironment(config)
  lazy val region: Region = Region.of(config.getString("todistus.s3.region"))
  lazy val rawBucketName: String = config.getString("todistus.s3.rawBucket")
  lazy val signedBucketName: String = config.getString("todistus.s3.signedBucket")
  lazy val endpointOverride: URI = URI.create(config.getString("todistus.s3.endpoint"))

  val s3: S3Client = {
    val awsS3 = S3Client.builder().region(region)
    val awsOrLocalS3 = if (useAWS) awsS3 else {
      logger.warn("Using Localstack for S3")
      awsS3
        .endpointOverride(endpointOverride)
        .credentialsProvider(localstackCredentialsProvider)
    }
    awsOrLocalS3.build()
  }

  if (!useAWS) {
    // Bucketin automaattinen luonti ainoastaan Localstackin kanssa
    createBucketIfDoesNotExist(rawBucketName)
    createBucketIfDoesNotExist(signedBucketName)
  }

  // TODO: TOR-2400: Toteuta tiedostokirjoitukset ja lukemiset S3:sta

  private def createBucketIfDoesNotExist(bucketName: String) = {
    if (!s3.listBuckets().buckets().asScala.exists(bucket => bucket.name() == bucketName)) {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
    }
  }

  private lazy val localstackCredentialsProvider = StaticCredentialsProvider.create(localstackCredentials)
  private lazy val localstackCredentials = AwsSessionCredentials.builder()
    .accessKeyId("000000000000")
    .secretAccessKey("1234")
    .sessionToken("1234")
    .build()
}
