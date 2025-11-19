package fi.oph.koski.todistus

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.todistus.BucketType.BucketType
import software.amazon.awssdk.auth.credentials.{AwsSessionCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.ContentStreamProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{CreateBucketRequest, GetObjectRequest, GetUrlRequest, PutObjectRequest, PutObjectResponse}
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest

import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import java.time.Duration
import scala.collection.JavaConverters.mapAsJavaMap
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class TodistusResultRepository(config: Config) extends Logging {
  val useAWS = Environment.isServerEnvironment(config)
  lazy val region: Region = Region.of(config.getString("todistus.s3.region"))
  lazy val rawBucketName: String = config.getString("todistus.s3.rawBucket")
  lazy val stampedBucketName: String = config.getString("todistus.s3.stampedBucket")
  lazy val presignDuration: Duration = config.getDuration("todistus.s3.presignDuration")
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
    createBucketIfDoesNotExist(stampedBucketName)
  }

  def getStream(bucketType: BucketType, id: String, contentType: String = "application/pdf"): Either[HttpStatus, InputStream] = {
    val key = objectKey(bucketType, id)
    Try {
      s3.getObject(buildGetRequest(bucketType, key, contentType))
    }
      .toEither.left.map(t => {
        val error = s"Failed to get ${key}"
        logger.error(t)(error)
        KoskiErrorCategory.internalError(error)
      })
  }

  def putStream(bucketType: BucketType, id: String, provider: ContentStreamProvider, contentType: String = "application/pdf"): Either[HttpStatus, Unit] = {
    val key = objectKey(bucketType, id)

    Try {
      val request = buildPutRequest(bucketType, id, key, contentType)
      val requestBody = RequestBody.fromContentProvider(provider, contentType)
      logPut(bucketType, key, contentType)
      s3.putObject(request, requestBody)
    }
      .toEither.left.map(t => {
        val error = s"Failed to put ${key}"
        logger.error(t)(error)
        KoskiErrorCategory.internalError(error)
      })
      .map(_ => ())
  }

  def putFile(bucketType: BucketType, id: String, file: Path, contentType: String = "application/pdf" ): Either[HttpStatus, Unit] = {
    val key = objectKey(bucketType, id)
    Try {
      val request = buildPutRequest(bucketType, id, key, contentType)
      val requestBody = RequestBody.fromFile(file)
      logPut(bucketType, key, contentType)
      s3.putObject(request, requestBody)
    }
      .toEither.left.map(t => {
        val error = s"Failed to put ${key} from file ${file.toString}"
        logger.error(t)(error)
        KoskiErrorCategory.internalError(error)
      })
      .map(_ => ())
  }

  def getPresignedDownloadUrl(bucketType: BucketType, filename: String, id: String): String = {
    val key = objectKey(bucketType, id)
    val awsPresigner = S3Presigner.builder().region(region)
    val presigner = (if (useAWS) {
      awsPresigner
    } else {
      awsPresigner
        .endpointOverride(endpointOverride)
        .credentialsProvider(localstackCredentialsProvider)
    }).build()

    val objectRequest = GetObjectRequest.builder()
      .bucket(bucketName(bucketType))
      .key(key)
      .responseContentDisposition(s"""attachment; filename="$filename"""")
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

  def objectKey(bucketType: BucketType, id: String): String = s"$id/${bucketType.toString.toLowerCase}.pdf"

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

  private def buildPutRequest(bucketType: BucketType, id: String, key: String, contentType: String) =
    PutObjectRequest.builder()
      .bucket(bucketName(bucketType))
      .key(key)
      .contentType(contentType)
      .metadata(mapAsJavaMap(Map {
        "todistusJobId" -> id
      }))
      .build()


  private def buildGetRequest(bucketType: BucketType, key: String, contentType: String): GetObjectRequest = {
    GetObjectRequest.builder()
      .bucket(bucketName(bucketType))
      .key(key)
      .responseContentType(contentType)
      .build()
  }

  private def bucketName(bucketType: BucketType): String = bucketType match {
    case BucketType.RAW => rawBucketName
    case BucketType.STAMPED => stampedBucketName
  }

  private def logPut(bucketType: BucketType, key: String, contentType: String): Unit =
    logger.info(s"Put result to S3: ${s3.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName(bucketType)).key(key).build())} ($contentType)")
}

object BucketType extends Enumeration {
  type BucketType = Value
  val
    RAW,
    STAMPED = Value
}

