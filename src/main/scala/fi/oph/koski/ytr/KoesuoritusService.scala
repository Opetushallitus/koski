package fi.oph.koski.ytr

import java.io.{InputStream, OutputStream}

import com.typesafe.config.Config
import fi.oph.koski.log.Logging
import fi.oph.koski.util._
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.model._

trait KoesuoritusService {
  def writeKoesuoritus(key: String, os: OutputStream): Unit
  def koesuoritusExists(key: String): Boolean
}

class RemoteKoesuoritusService(config: YtrS3Config) extends KoesuoritusService with Logging {
  private val s3 = new YtrS3(config)
  private val bucket = s3.bucket

  override def writeKoesuoritus(key: String, os: OutputStream): Unit = try {
    s3.client.getObject(objectRequest(key), ResponseTransformer.toOutputStream[GetObjectResponse](os))
  } finally {
    os.close()
  }

  override def koesuoritusExists(key: String): Boolean = try {
    s3.client.headObject(headRequest(key))
    true
  } catch {
    case e: S3Exception =>
      logger.warn(s"Unable to access s3://$bucket/$key ${e.getMessage}")
      false
  }

  private def objectRequest(key: String) = GetObjectRequest.builder.bucket(bucket).key(key).build
  private def headRequest(key: String) = HeadObjectRequest.builder.bucket(bucket).key(key).build
}

object MockKoesuoritusService extends KoesuoritusService with EnvVariables {
  private lazy val resource: Resource = optEnv("PDF_DIR")
    .map(new FileResource(_))
    .getOrElse(new ClasspathResource("/mockdata/ytr"))

  override def writeKoesuoritus(key: String, outputStream: OutputStream): Unit =
    resource.resourceSerializer(stripKey(key))(writeTo(outputStream))

  override def koesuoritusExists(key: String): Boolean = resource.exists(stripKey(key))

  private def writeTo(os: OutputStream) = (is: InputStream) =>
    Streams.pipeTo(is, os)

  private def stripKey(key: String): String =
    key.split("/").lastOption.getOrElse("")
}

object KoesuoritusService {
  def apply(config: YtrS3Config): KoesuoritusService = if (config.bucket == "mock") {
    MockKoesuoritusService
  } else {
    new RemoteKoesuoritusService(config)
  }
}
