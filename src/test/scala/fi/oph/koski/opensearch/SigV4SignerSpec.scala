package fi.oph.koski.opensearch

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.Stream
import org.http4s.{Method, Request, Uri}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}

import java.time.{Clock, Instant, ZoneOffset}

class SigV4SignerSpec extends AnyFreeSpec with Matchers {

  private val fakeCreds = StaticCredentialsProvider.create(
    AwsBasicCredentials.create("AKIAFAKETESTKEY", "wJalrXUtnFEMI/K7MDENG/fakeSecretKey")
  )
  private val signer = new SigV4Signer("eu-west-1", fakeCreds)

  // Fixed-clock signer for deterministic / regression tests.
  private val fixedClock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC)
  private val deterministicSigner = new SigV4Signer("eu-west-1", fakeCreds, fixedClock)

  "SigV4Signer" - {
    "adds AWS4-HMAC-SHA256 Authorization header to a GET request" in {
      val req = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString("https://vpc-koski-opensearch-xxxx.eu-west-1.es.amazonaws.com/_cluster/health")
      )

      val signed = signer.sign(req).unsafeRunSync()

      val authHeader = signed.headers.get(org.typelevel.ci.CIString("Authorization"))
      authHeader.isDefined shouldBe true
      authHeader.get.head.value should startWith("AWS4-HMAC-SHA256 ")
      authHeader.get.head.value should include("Credential=AKIAFAKETESTKEY/")
      authHeader.get.head.value should include("es/aws4_request")
    }

    "includes x-amz-date and x-amz-content-sha256 headers" in {
      val req = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString("https://vpc-koski-opensearch-xxxx.eu-west-1.es.amazonaws.com/perustiedot-v3/_search")
      ).withBodyStream(Stream.emits("""{"query":{"match_all":{}}}""".getBytes("UTF-8")).covary[IO])

      val signed = signer.sign(req).unsafeRunSync()

      signed.headers.get(org.typelevel.ci.CIString("x-amz-date")).isDefined shouldBe true
      signed.headers.get(org.typelevel.ci.CIString("x-amz-content-sha256")).isDefined shouldBe true
    }

    "preserves the request body bytes through signing" in {
      val body = """{"query":{"match_all":{}}}"""
      val req = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString("https://example.com/_search")
      ).withBodyStream(Stream.emits(body.getBytes("UTF-8")).covary[IO])

      val signed = signer.sign(req).unsafeRunSync()
      val resultBody = signed.body.compile.toVector.map(_.toArray).map(new String(_, "UTF-8")).unsafeRunSync()

      resultBody shouldBe body
    }

    "differentiates signatures when the query string changes" in {
      // Canonical query string handling is a common SigV4 footgun — make sure
      // different query strings produce different signatures.
      val r1 = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString("https://vpc-koski-opensearch-xxxx.eu-west-1.es.amazonaws.com/_search?scroll=1m&size=100")
      )
      val r2 = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString("https://vpc-koski-opensearch-xxxx.eu-west-1.es.amazonaws.com/_search?scroll=2m&size=100")
      )

      val sig1 = deterministicSigner.sign(r1).unsafeRunSync()
        .headers.get(org.typelevel.ci.CIString("Authorization")).get.head.value
      val sig2 = deterministicSigner.sign(r2).unsafeRunSync()
        .headers.get(org.typelevel.ci.CIString("Authorization")).get.head.value

      sig1 should not equal sig2
    }

    "signs deterministically with a fixed clock (regression guard)" in {
      // Same request + same credentials + same clock should produce identical
      // signatures. If this breaks, canonicalization (signed headers list,
      // query-string ordering, payload hash, etc.) has changed and the
      // surrounding behavior needs scrutiny.
      val req = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString("https://vpc-koski-opensearch-xxxx.eu-west-1.es.amazonaws.com/perustiedot-v3/_search?size=10")
      )

      val sig1 = deterministicSigner.sign(req).unsafeRunSync()
        .headers.get(org.typelevel.ci.CIString("Authorization")).get.head.value
      val sig2 = deterministicSigner.sign(req).unsafeRunSync()
        .headers.get(org.typelevel.ci.CIString("Authorization")).get.head.value

      sig1 shouldBe sig2
    }

    "rejects a request without an absolute URI" in {
      val req = Request[IO](
        method = Method.GET,
        uri = Uri.unsafeFromString("/perustiedot-v3/_search")  // no host
      )

      // Wrapped in IO, so we have to materialize to see the failure.
      val ex = intercept[IllegalArgumentException] {
        signer.sign(req).unsafeRunSync()
      }
      ex.getMessage should include("absolute URI")
    }
  }
}
