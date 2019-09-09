package fi.oph.koski.ytr

import java.io.{InputStream, OutputStream}

import com.typesafe.config.Config
import fi.oph.koski.log.Logging
import fi.oph.koski.util.ClasspathResources
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.model._

trait KoesuoritusService {
  def writeKoesuoritus(key: String, os: OutputStream): Unit
  def koesuoritusExists(key: String): Boolean
}

class RemoteKoesuoritusService(config: Config) extends KoesuoritusService with Logging {
  private val bucket = config.getString("ytr.aws.bucket")
  private val s3 = new YtrS3(config)

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

object MockKoesuoritusService extends KoesuoritusService {
  override def writeKoesuoritus(key: String, os: OutputStream): Unit =
    ClasspathResources.readResourceIfExists("/mockdata/ytr/" + key, writeTo(os))

  override def koesuoritusExists(key: String): Boolean = ClasspathResources.getResource("/mockdata/ytr/" + key).isDefined

  private def writeTo(os: OutputStream) = (is: InputStream) =>
    Iterator.continually(is.read).takeWhile(_ != -1).foreach(os.write)
}

object KoesuoritusService {
  def apply(config: Config): KoesuoritusService = if (config.getString("ytr.aws.bucket") == "mock") {
    MockKoesuoritusService
  } else {
    new RemoteKoesuoritusService(config)
  }
}
