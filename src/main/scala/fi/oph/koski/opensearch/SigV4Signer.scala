package fi.oph.koski.opensearch

import cats.effect.IO
import fi.oph.koski.http.RequestSigner
import fs2.Stream
import org.http4s.Header.Raw
import org.http4s.{Headers, Request}
import org.typelevel.ci.CIString
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, DefaultCredentialsProvider}
import software.amazon.awssdk.http.auth.aws.signer.{AwsV4FamilyHttpSigner, AwsV4HttpSigner}
import software.amazon.awssdk.http.auth.spi.signer.{HttpSigner, SignRequest}
import software.amazon.awssdk.http.{ContentStreamProvider, SdkHttpFullRequest, SdkHttpMethod}
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity

import java.io.ByteArrayInputStream
import java.net.URI
import java.time.Clock
import scala.jdk.CollectionConverters._

/** Signs outbound http4s requests with AWS Signature V4 against the OpenSearch
  * service. Used when the domain has FGAC enabled and requires IAM-signed
  * requests. Credentials are resolved via the default AWS SDK chain (ECS task
  * role in dev/qa/prod, SSO/env vars for local-against-cloud).
  *
  * Whole-body materialization: SigV4 requires the SHA-256 of the full payload
  * to be in the signature, so streaming signing isn't an option (AWS-managed
  * OpenSearch rejects the UNSIGNED-PAYLOAD escape hatch under FGAC). Buffering
  * is fine for OS search/index traffic (KBs to low MBs). Don't use this signer
  * for large bulk uploads or streaming payloads. */
class SigV4Signer(
  region: String,
  credentialsProvider: AwsCredentialsProvider = DefaultCredentialsProvider.builder().build(),
  clock: Clock = Clock.systemUTC()
) extends RequestSigner {

  private val signer = AwsV4HttpSigner.create()
  private val signingName = "es"

  // Headers that are framing/transport concerns rather than content. Not
  // included in signing input (SDK derives them) and not re-emitted to
  // http4s after signing (http4s/blaze sets them itself at transport time).
  private val FramingHeaders: Set[String] = Set(
    "host",
    "content-length",
    "transfer-encoding",
    "expect",
    "connection"
  )

  override def sign(request: Request[IO]): IO[Request[IO]] = {
    // Defensive: SigV4 needs an absolute URI to derive the canonical Host. In
    // normal Koski usage Http.processRequest calls addRoot first, so this
    // always holds — but cheap to guard against misuse.
    require(
      request.uri.host.isDefined,
      s"SigV4Signer requires an absolute URI with host; got: ${request.uri}"
    )

    request.body.compile.toVector.map(_.toArray).map { bodyBytes =>
      val sdkRequest = toSdkRequest(request, bodyBytes)

      val signRequestBuilder = SignRequest
        // Type ascription is needed because SignRequest.builder is generic
        // over the identity type — without it Scala can't pick the overload.
        .builder(credentialsProvider.resolveCredentials(): AwsCredentialsIdentity)
        .request(sdkRequest)
        .putProperty(AwsV4FamilyHttpSigner.SERVICE_SIGNING_NAME, signingName)
        .putProperty(AwsV4HttpSigner.REGION_NAME, region)
        .putProperty(HttpSigner.SIGNING_CLOCK, clock)
      payloadProviderFor(bodyBytes).foreach(signRequestBuilder.payload)
      val signed = signer.sign(signRequestBuilder.build())

      // Re-attach signed headers to the http4s request. Skip framing headers
      // that http4s/blaze sets itself at send time — re-emitting them would
      // duplicate or conflict with what the underlying transport injects, and
      // for Host specifically the signed value must match what blaze actually
      // sends or AWS rejects the signature.
      val signedHeaders: List[Raw] = signed.request().headers().asScala.toList.flatMap {
        case (name, values) =>
          if (FramingHeaders.contains(name.toLowerCase)) Nil
          else values.asScala.toList.map(value => Raw(CIString(name), value))
      }

      request
        .withBodyStream(Stream.emits(bodyBytes).covary[IO])
        .withHeaders(Headers(signedHeaders))
    }
  }

  private def toSdkRequest(req: Request[IO], body: Array[Byte]): SdkHttpFullRequest = {
    val builder = SdkHttpFullRequest.builder()
      .method(SdkHttpMethod.fromValue(req.method.name))
      .uri(URI.create(req.uri.toString))

    // Don't smuggle framing headers (Host, Content-Length, Transfer-Encoding,
    // Expect, Connection) into the SDK request. The SDK derives Host from the
    // URI, and Content-Length / Transfer-Encoding are computed by http4s/blaze
    // at send time. Including them here can sign a value that doesn't match
    // what gets sent.
    req.headers.headers.foreach { h =>
      if (!FramingHeaders.contains(h.name.toString.toLowerCase)) {
        builder.appendHeader(h.name.toString, h.value)
      }
    }

    payloadProviderFor(body).foreach(builder.contentStreamProvider)

    builder.build()
  }

  private def payloadProviderFor(body: Array[Byte]): Option[ContentStreamProvider] =
    if (body.nonEmpty) Some(() => new ByteArrayInputStream(body)) else None
}
