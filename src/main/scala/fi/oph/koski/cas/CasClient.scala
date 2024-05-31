package fi.oph.koski.cas

import cats.data.EitherT
import cats.effect.IO
import org.http4s.EntityDecoder.collectBinary
import org.http4s.Status.{Created, Locked}
import org.http4s.client._
import org.http4s._
import org.typelevel.ci.CIString

import scala.util.{Failure, Success, Try}
import scala.xml._


object CasClient {
  type SessionCookie = String
  type Username = String
  type OppijaAttributes = Map[String, String]
  type TGTUrl = Uri
  type ServiceTicket = String

  val textOrXmlDecoder: EntityDecoder[IO, String] =
    EntityDecoder.decodeBy(MediaRange.`text/*`, MediaType.application.xml)(msg =>
      collectBinary(msg).map(bs => new String(
        bs.toArray,
        msg.charset.getOrElse(org.http4s.Charset.`UTF-8`).nioCharset
      ))
    )
}

/**
 *  Facade for establishing sessions with services protected by CAS, and also validating CAS service tickets.
 */
class CasClient(casBaseUrl: Uri, client: Client[IO], callerId: String) extends Logging {
  import CasClient._

  def this(casServer: String, client: Client[IO], callerId: String) = this(Uri.fromString(casServer).right.get, client, callerId)

  def validateServiceTicketWithOppijaAttributes(service: String)(serviceTicket: ServiceTicket): IO[OppijaAttributes] = {
    validateServiceTicket[OppijaAttributes](casBaseUrl, client, service, decodeOppijaAttributes)(serviceTicket)
  }

  def validateServiceTicketWithVirkailijaUsername(service: String)(serviceTicket: ServiceTicket): IO[Username] = {
    validateServiceTicket[Username](casBaseUrl, client, service, decodeVirkailijaUsername)(serviceTicket)
  }

  def validateServiceTicket[R](service: String)(serviceTicket: ServiceTicket, responseHandler: Response[IO] => IO[R]): IO[R] = {
    validateServiceTicket[R](casBaseUrl, client, service, responseHandler)(serviceTicket)
  }

  private def validateServiceTicket[R](casBaseUrl: Uri, client: Client[IO], service: String, responseHandler: Response[IO] => IO[R])(serviceTicket: ServiceTicket): IO[R] = {
    val pUri: Uri = casBaseUrl.addPath("serviceValidate")
      .withQueryParam("ticket", serviceTicket)
      .withQueryParam("service", service)

    val request = Request[IO](Method.GET, pUri)
    FetchHelper.fetch[R](client, callerId, request, responseHandler)
  }

  def authenticateVirkailija(user: CasUser): IO[Boolean] = {
    TicketGrantingTicketClient.getTicketGrantingTicket(casBaseUrl, client, user, callerId)
      .map(_tgtUrl => true) // Authentication succeeded if we received a tgtUrl
  }

  /**
   *  Establishes session with the requested service by
   *
   *  1) getting a CAS ticket granting ticket (TGT)
   *  2) getting a CAS service ticket
   *  3) getting a session cookie from the service.
   *
   *  Returns the session that can be used for communications later.
   */
  def fetchCasSession(params: CasParams, sessionCookieName: String): IO[SessionCookie] = {
    val serviceUri = Uri.resolve(casBaseUrl, params.service.securityUri)

    for (
      st <- getServiceTicketWithRetryOnce(params, serviceUri);
      session <- SessionCookieClient.getSessionCookieValue(client, serviceUri, sessionCookieName, callerId)(st)
    ) yield {
      session
    }
  }

  private def getServiceTicketWithRetryOnce(params: CasParams, serviceUri: TGTUrl): IO[ServiceTicket] = {
    getServiceTicket(params, serviceUri).attempt.flatMap {
      case Right(success) =>
        IO(success)
      case Left(throwable) =>
        logger.warn("Fetching TGT or ST failed. Retrying once (and only once) in case the error was ephemeral.", throwable)
        retryServiceTicket(params, serviceUri)
    }
  }

  private def retryServiceTicket(params: CasParams, serviceUri: TGTUrl): IO[ServiceTicket] = {
    getServiceTicket(params, serviceUri).attempt.map {
      case Right(retrySuccess) =>
        logger.info("Fetching TGT and ST was successful after one retry.")
        retrySuccess
      case Left(retryThrowable) =>
        logger.error("Fetching TGT or ST failed also after one retry.", retryThrowable)
        throw retryThrowable
    }
  }

  private def getServiceTicket(params: CasParams, serviceUri: TGTUrl): IO[ServiceTicket] = {
    for {
      tgt <- TicketGrantingTicketClient.getTicketGrantingTicket(casBaseUrl, client, params.user, callerId)
      st <- ServiceTicketClient.getServiceTicketFromTgt(client, serviceUri, callerId)(tgt)
    } yield {
      st
    }
  }

  private val oppijaServiceTicketDecoder: EntityDecoder[IO, OppijaAttributes] =
    textOrXmlDecoder
      .map(s => Utility.trim(scala.xml.XML.loadString(s)))
      .flatMapR[OppijaAttributes] { serviceResponse =>
        Try {
          val attributes: NodeSeq = (serviceResponse \ "authenticationSuccess" \ "attributes")

          List("mail", "clientName", "displayName", "givenName", "personOid", "personName", "firstName", "nationalIdentificationNumber",
            "impersonatorNationalIdentificationNumber", "impersonatorDisplayName")
            .map(key => (key, (attributes \ key).text))
            .toMap
        } match {
          case Success(decoded) => DecodeResult.successT(decoded)
          case Failure(ex) =>
            DecodeResult.failureT(InvalidMessageBodyFailure(
              "Oppija Service Ticket validation response decoding failed: Failed to parse required values from response body",
              Some(ex))
            )
        }
      }

  private val virkailijaServiceTicketDecoder: EntityDecoder[IO, Username] =
    textOrXmlDecoder
      .map(s => Utility.trim(scala.xml.XML.loadString(s)))
      .flatMapR[Username] { serviceResponse => {
          val user = (serviceResponse \ "authenticationSuccess" \ "user")
          user.length match {
            case 1 => DecodeResult.successT(user.text)
            case _ =>
              DecodeResult.failureT(InvalidMessageBodyFailure(
                s"Virkailija Service Ticket validation response decoding failed: response body is of wrong form ($serviceResponse)"
              ))
          }
        }
      }

  private def casFailure[R](debugLabel: String, resp: Response[IO]): EitherT[IO, DecodeFailure, R] = {
    textOrXmlDecoder
      .decode(resp, strict = false)
      .flatMap(body => DecodeResult.failureT[IO, R](InvalidMessageBodyFailure(
        s"Decoding $debugLabel failed: CAS returned non-ok status code ${resp.status.code}: $body"
      )))
      .leftFlatMap(failure => DecodeResult.failureT[IO, R](InvalidMessageBodyFailure(
        s"Decoding $debugLabel failed: CAS returned non-ok status code ${resp.status.code}: ${failure.message}"
      )))
  }

  /**
   * Decode CAS Oppija's service ticket validation response to various oppija attributes.
   */
  def decodeOppijaAttributes: Response[IO] => IO[OppijaAttributes] = { response =>
    decodeCASResponse[OppijaAttributes](response, "oppija attributes", oppijaServiceTicketDecoder)
  }

  /**
   * Decode CAS Virkailija's service ticket validation response to username.
   */
  def decodeVirkailijaUsername: Response[IO] => IO[Username] = { response =>
    decodeCASResponse[Username](response, "username", virkailijaServiceTicketDecoder)
  }

  private def decodeCASResponse[R](response: Response[IO], debugLabel: String, decoder: EntityDecoder[IO, R]): IO[R] = {
    val decodeResult = if (response.status.isSuccess) {
      decoder
        .decode(response, strict = false)
        .leftMap(decodeFailure => new CasClientException(s"Decoding $debugLabel failed: " + decodeFailure.message))
    } else {
      casFailure(debugLabel, response)
    }
    decodeResult.rethrowT
  }
}

private[cas] object ServiceTicketClient {
  import CasClient._

  private val stPattern = "(ST-.*)".r

  def getServiceTicketFromTgt(client: Client[IO], service: Uri, callerId: String)(tgtUrl: TGTUrl): IO[ServiceTicket] = {
    val urlForm = UrlForm("service" -> service.toString())
    val request = Request[IO](Method.POST, tgtUrl).withEntity(urlForm)

    def handler(response: Response[IO]): IO[ServiceTicket] = {
      response match {
        case r: Response[IO] if r.status.isSuccess => r.as[String].map {
          case stPattern(st) => st
          case nonSt: Any => throw new CasClientException(s"Service Ticket decoding failed at ${tgtUrl}: response body is of wrong form ($nonSt)")
        }
        case r: Response[IO] => r.as[String].map(body =>
          throw new CasClientException(s"Service Ticket decoding failed at ${tgtUrl}: unexpected status ${r.status.code}: $body")
        )
      }
    }
    FetchHelper.fetch(client, callerId, request, handler)
  }
}

private[cas] object TicketGrantingTicketClient extends Logging {
  import CasClient.TGTUrl

  private val tgtPattern = "(.*TGT-.*)".r

  def getTicketGrantingTicket(casBaseUrl: Uri, client: Client[IO], user: CasUser, callerId: String): IO[TGTUrl] = {
    val tgtUri: TGTUrl = casBaseUrl.addPath("v1/tickets")
    val urlForm = UrlForm("username" -> user.username, "password" -> user.password)
    val request = Request[IO](Method.POST, tgtUri).withEntity(urlForm)

    def handler(response: Response[IO]): IO[TGTUrl] = {
      response match {
        case Created(resp) =>
          val found: TGTUrl = resp.headers.get(CIString("Location")).map(_.head.value) match {
            case Some(tgtPattern(tgtUrl)) =>
              Uri.fromString(tgtUrl).fold(
                (pf: ParseFailure) => throw new CasClientException(pf.message),
                (tgt) => tgt
              )
            case Some(nonTgtUrl) =>
              throw new CasClientException(s"TGT decoding failed at ${tgtUri}: location header has wrong format $nonTgtUrl")
            case None =>
              throw new CasClientException(s"TGT decoding failed at ${tgtUri}: no location header")
          }
          IO.pure(found)
        case Locked(_) =>
          throw new CasAuthenticationException(s"Access denied: username ${user.username} is locked")
        case resp: Response[IO] => resp.as[String].map(body =>
          if (body.contains("authentication_exceptions") || body.contains("error.authentication.credentials.bad")) {
            throw new CasAuthenticationException(s"Access denied: bad credentials")
          } else {
            throw new CasClientException(s"TGT decoding failed at ${tgtUri}: invalid TGT creation status: ${resp.status.code}: $body")
          }
        )
      }
    }
    FetchHelper.fetch(client, callerId, request, handler)
  }
}

private[cas] object SessionCookieClient {
  import CasClient._

  def getSessionCookieValue
    (client: Client[IO], service: Uri, sessionCookieName: String, callerId: String)
    (serviceTicket: ServiceTicket)
  : IO[SessionCookie] = {
    val sessionIdUri: Uri = service.withQueryParam("ticket", serviceTicket)
    val request = Request[IO](Method.GET, sessionIdUri)

    def handler(response: Response[IO]): IO[SessionCookie] = {
      response match {
        case resp: Response[IO] if resp.status.isSuccess =>
          IO.pure(
            resp.cookies.find(_.name == sessionCookieName).map(_.content)
              .getOrElse(throw new CasClientException(s"Decoding $sessionCookieName failed at ${sessionIdUri}: no cookie found"))
          )
        case resp: Response[IO] =>
          resp.as[String].map(body =>
            throw new CasClientException(s"Decoding $sessionCookieName failed at ${sessionIdUri}: service returned non-ok status code ${resp.status.code}: $body")
          )
      }
    }

    FetchHelper.fetch(client, callerId, request, handler)
  }
}

object FetchHelper {
  def addDefaultHeaders(request: Request[IO], callerId: String): Request[IO] = {
    request.putHeaders(
      Header.Raw(CIString("Caller-Id"), callerId),
      Header.Raw(CIString("CSRF"), callerId)
    ).addCookie("CSRF", callerId)
  }

  def fetch[A](client: Client[IO], callerId: String, request: Request[IO], handler: Response[IO] => IO[A]): IO[A] =
    client.run(addDefaultHeaders(request, callerId)).use(handler)
}

class CasClientException(message: String) extends RuntimeException(message)

class CasAuthenticationException(message: String) extends CasClientException(message)
