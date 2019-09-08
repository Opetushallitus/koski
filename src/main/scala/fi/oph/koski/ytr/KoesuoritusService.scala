package fi.oph.koski.ytr

import java.io.{InputStream, OutputStream}

import com.typesafe.config.Config
import fi.oph.koski.util.ClasspathResources
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse}

trait KoesuoritusService {
  def writeKoesuoritus(key: String, os: OutputStream): Unit
}

class RemoteKoesuoritusService(config: Config) extends KoesuoritusService {
  private val bucket = config.getString("ytr.aws.bucket")
  private val s3 = new YTRS3(config)

  override def writeKoesuoritus(key: String, os: OutputStream): Unit = try {
    s3.client.getObject(mkRequest(key), ResponseTransformer.toOutputStream[GetObjectResponse](os))
  } finally {
    os.close()
  }

  private def mkRequest(key: String) = GetObjectRequest.builder.bucket(bucket).key(key).build
}

object MockKoesuoritusService extends KoesuoritusService {
  override def writeKoesuoritus(key: String, os: OutputStream): Unit =
    ClasspathResources.readResourceIfExists("/mockdata/ytr/" + key, writeTo(os))

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
