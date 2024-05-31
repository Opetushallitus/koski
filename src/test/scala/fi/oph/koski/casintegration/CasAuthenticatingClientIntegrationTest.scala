package fi.oph.koski.casintegration

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import fi.oph.koski.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.Status.Ok
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.global


class CasAuthenticatingClientIntegrationTest extends AnyFreeSpec with Matchers {
  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  private def uriFromString(uri: String): Uri = {
    Uri.fromString(uri) match {
      case Right(result) => result
      case Left(failure) => throw new IllegalArgumentException("Cannot create URI: " + uri + ": " + failure)
    }
  }
  private def requiredEnv(name: String) = sys.env.getOrElse(name, throw new RuntimeException("Environment property " + name + " missing"))

  "CasAuthenticatingClient integration test" taggedAs(IntegrationTestTag) in {
    val virkailijaUrl: String = requiredEnv("VIRKAILIJA_ROOT")
    val virkailijaUser: String = requiredEnv("VIRKAILIJA_USER")
    val virkailijaPassword: String = requiredEnv("VIRKAILIJA_PASSWORD")

    val blazeClient = BlazeClientBuilder[IO].withExecutionContext(global).resource.allocated.map(_._1).unsafeRunSync()
    val casClient = new CasClient(virkailijaUrl + "/cas", blazeClient, "my-caller-id")
    val casAuthenticatingClient = CasAuthenticatingClient(
      casClient,
      CasParams("/kayttooikeus-service", virkailijaUser, virkailijaPassword),
      blazeClient,
      "koski",
      "JSESSIONID"
    )
    val request = Request[IO](uri = uriFromString(virkailijaUrl + "/kayttooikeus-service/henkilo/current/omattiedot"))
    val result = casAuthenticatingClient.run(request).use {
      case Ok(response) => response.as[String]
      case other => throw new RuntimeException("Response code " + other)
    }
  }
}

object IntegrationTestTag extends Tag("casintegration")
