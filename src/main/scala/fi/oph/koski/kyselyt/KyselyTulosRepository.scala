package fi.oph.koski.kyselyt

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import software.amazon.awssdk.auth.credentials.{AwsSessionCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{PutObjectRequest, CreateBucketRequest}

import java.io.InputStream
import java.net.URI
import java.util.UUID
import scala.jdk.CollectionConverters.mapAsJavaMap
import scala.jdk.CollectionConverters._

class KyselyTulosRepository(config: Config) {
  val useAWS = Environment.isServerEnvironment(config)
  lazy val region: Region = Region.of(config.getString("kyselyt.s3.region"))
  lazy val bucketName: String = config.getString("kyselyt.s3.bucket")
  lazy val endpointOverride: URI = URI.create(config.getString("kyselyt.s3.endpoint"))

  val s3: S3Client = {
    val awsS3 = S3Client.builder().region(region)
    val awsOrLocalS3 = if (useAWS) awsS3 else {
      awsS3
        .endpointOverride(endpointOverride)
        .credentialsProvider(localstackCredentialsProvider)
    }
    awsOrLocalS3.build()
  }

  if (!useAWS) {
    // Bucketin automaattinen luonti ainoastaan Localsackin kanssa
    createBucketIfDoesNotExist
  }

  def putStream[T](queryId: UUID, name: String, inputStream: InputStream, contentLength: Long): String = {
    val key = s"$queryId/$name"

    val request = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(key)
      .metadata(mapAsJavaMap(Map {
        "query" -> queryId.toString
      }))
    val requestBody = RequestBody.fromInputStream(inputStream, contentLength)
    s3.putObject(request.build(), requestBody)

    key
  }
  
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
}

case class QueryResult(
  name: String,
  content: Stream[Char],
)

class StringInputStream(stream: Stream[Char]) extends InputStream {
  private val iter = stream.iterator
  override def read(): Int = if (iter.hasNext) iter.next else -1
}

object StringInputStream {
  def apply(string: String) = new StringInputStream(string.toStream)
}

class StringListInputStream(stream: Stream[String]) extends InputStream {
  private var stringIterator = stream.iterator
  private var charIterator: Option[Iterator[Char]] = None

  override def read(): Int = {
    if (charIterator.isEmpty && stringIterator.hasNext) {
      charIterator = Some(stringIterator.next.toStream.iterator)
    }
    charIterator.map { iter =>
      if (iter.hasNext) {
        iter.next
      } else {
        charIterator = None
        read()
      }
    }.getOrElse(-1)
  }
}
