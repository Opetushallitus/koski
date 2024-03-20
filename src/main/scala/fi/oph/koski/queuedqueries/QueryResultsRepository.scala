package fi.oph.koski.queuedqueries

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging
import software.amazon.awssdk.auth.credentials.{AwsSessionCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.ContentStreamProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{CreateBucketRequest, GetObjectRequest, GetUrlRequest, PutObjectRequest}
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest

import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import java.time.Duration
import java.util.UUID
import scala.jdk.CollectionConverters._

class QueryResultsRepository(config: Config) extends Logging {
  val useAWS = Environment.isServerEnvironment(config)
  lazy val region: Region = Region.of(config.getString("kyselyt.s3.region"))
  lazy val bucketName: String = config.getString("kyselyt.s3.bucket")
  lazy val presignDuration: Duration = config.getDuration("kyselyt.s3.presignDuration")
  lazy val endpointOverride: URI = URI.create(config.getString("kyselyt.s3.endpoint"))

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
    createBucketIfDoesNotExist
  }

  def putStream(queryId: UUID, name: String, provider: ContentStreamProvider, contentType: String): Unit = {
    val key = objectKey(queryId, name)
    val request = buildPutRequest(queryId, key, contentType)
    val requestBody = RequestBody.fromContentProvider(provider, contentType)
    logPut(key, contentType)
    s3.putObject(request, requestBody)
  }

  def putFile(queryId: UUID, name: String, file: Path, contentType: String): Unit = {
    val key = objectKey(queryId, name)
    val request = buildPutRequest(queryId, key, contentType)
    val requestBody = RequestBody.fromFile(file)
    logPut(key, contentType)
    s3.putObject(request, requestBody)
  }

  def getPresignedDownloadUrl(queryId: UUID, name: String): String = {
    val key = objectKey(queryId, name)
    val awsPresigner = S3Presigner.builder().region(region)
    val presigner = (if (useAWS) {
      awsPresigner
    } else {
      awsPresigner
        .endpointOverride(endpointOverride)
        .credentialsProvider(localstackCredentialsProvider)
    }).build()

    val objectRequest = GetObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .build()

    val presignRequest = GetObjectPresignRequest.builder()
      .signatureDuration(presignDuration)
      .getObjectRequest(objectRequest)
      .build()

    presigner
      .presignGetObject(presignRequest)
      .url()
      .toExternalForm
  }

  def objectKey(queryId: UUID, name: String): String = s"$queryId/$name"

  private def createBucketIfDoesNotExist =
    if (!s3.listBuckets().buckets().asScala.exists(bucket => bucket.name() == bucketName)) {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
    }

  private lazy val localstackCredentialsProvider = StaticCredentialsProvider.create(localstackCredentials)
  private lazy val localstackCredentials = AwsSessionCredentials.builder()
    .accessKeyId("000000000000")
    .secretAccessKey("1234")
    .sessionToken("1234")
    .build()

  private def buildPutRequest(queryId: UUID, key: String, contentType: String) =
    PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .contentType(contentType)
      .metadata(mapAsJavaMap(Map {
        "query" -> queryId.toString
      }))
      .build()

  private def logPut(key: String, contentType: String) =
    logger.info(s"Put result to S3: ${s3.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build())} ($contentType)")
}
