package fi.oph.koski.opensearch

import cats.effect.IO
import fi.oph.koski.http.RequestSigner
import fs2.Stream
import org.http4s.Header.Raw
import org.http4s.{Headers, Request}
import org.typelevel.ci.CIString
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, DefaultCredentialsProvider}
import software.amazon.awssdk.http.auth.aws.signer.{AwsV4FamilyHttpSigner, AwsV4HttpSigner}
import software.amazon.awssdk.http.auth.spi.signer.SignRequest
import software.amazon.awssdk.http.{ContentStreamProvider, SdkHttpFullRequest, SdkHttpMethod}
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity

import java.io.ByteArrayInputStream
import java.net.URI
import scala.jdk.CollectionConverters._

/** Signs outbound http4s requests with AWS Signature V4 against the OpenSearch
  * service. Used when the domain has FGAC enabled and requires IAM-signed
  * requests. Credentials are resolved via the default AWS SDK chain (ECS task
  * role in dev/qa/prod, SSO/env vars for local-against-cloud). */
class SigV4Signer(
  region: String,
  credentialsProvider: AwsCredentialsProvider = DefaultCredentialsProvider.builder().build()
) extends RequestSigner {

  private val signer = AwsV4HttpSigner.create()
  private val signingName = "es"

  override def sign(request: Request[IO]): IO[Request[IO]] =
    request.body.compile.toVector.map(_.toArray).map { bodyBytes =>
      val sdkRequest = toSdkRequest(request, bodyBytes)

      val signRequestBuilder = SignRequest
        .builder(credentialsProvider.resolveCredentials(): AwsCredentialsIdentity)
        .request(sdkRequest)
        .putProperty(AwsV4FamilyHttpSigner.SERVICE_SIGNING_NAME, signingName)
        .putProperty(AwsV4HttpSigner.REGION_NAME, region)
      payloadProviderFor(bodyBytes).foreach(signRequestBuilder.payload)
      val signed = signer.sign(signRequestBuilder.build())

      val signedHeaders: List[Raw] = signed.request().headers().asScala.toList.flatMap {
        case (name, values) =>
          values.asScala.toList.map(value => Raw(CIString(name), value))
      }

      request
        .withBodyStream(Stream.emits(bodyBytes).covary[IO])
        .withHeaders(Headers(signedHeaders))
    }

  private def toSdkRequest(req: Request[IO], body: Array[Byte]): SdkHttpFullRequest = {
    val builder = SdkHttpFullRequest.builder()
      .method(SdkHttpMethod.fromValue(req.method.name))
      .uri(URI.create(req.uri.toString))

    req.headers.headers.foreach { h =>
      builder.appendHeader(h.name.toString, h.value)
    }

    payloadProviderFor(body).foreach(builder.contentStreamProvider)

    builder.build()
  }

  private def payloadProviderFor(body: Array[Byte]): Option[ContentStreamProvider] =
    if (body.nonEmpty) Some(() => new ByteArrayInputStream(body)) else None
}
