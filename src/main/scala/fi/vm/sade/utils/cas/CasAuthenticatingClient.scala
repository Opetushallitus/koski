package fi.vm.sade.utils.cas

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.std.Hotswap
import fi.vm.sade.utils.cas.CasClient.SessionCookie
import org.http4s.client.Client
import org.http4s.{Request, Response, Status}
import org.typelevel.ci.CIString


/**
 *  Middleware that handles CAS authentication automatically. Sessions are maintained by keeping
 *  a central cache of session cookies per service url. If a session cookie is not found for requested service, it is obtained using
 *  CasClient. Stale sessions are detected and refreshed automatically.
 */
object CasAuthenticatingClient extends Logging {
  val DefaultSessionCookieName = "JSESSIONID"

  private val sessions: collection.mutable.Map[CasParams, SessionCookie] = collection.mutable.Map.empty

  def apply(
    casClient: CasClient,
    casParams: CasParams,
    serviceClient: Client[IO],
    clientCallerId: String,
    sessionCookieName: String = DefaultSessionCookieName
  ): Client[IO] = {
    def openWithCasSession(request: Request[IO], hotswap: Hotswap[IO, Response[IO]]): IO[Response[IO]] = {
      getCasSession(casParams).flatMap(requestWithCasSession(request, hotswap, retry = true))
    }

    def requestWithCasSession
      (request: Request[IO], hotswap: Hotswap[IO, Response[IO]], retry: Boolean)
      (sessionCookie: SessionCookie)
    : IO[Response[IO]] = {
      val fullRequest = FetchHelper.addDefaultHeaders(
        request.addCookie(sessionCookieName, sessionCookie),
        clientCallerId
      )
      // Hotswap use inspired by http4s Retry middleware:
      hotswap.swap(serviceClient.run(fullRequest)).flatMap {
        case r: Response[IO] if sessionExpired(r) && retry =>
          logger.info("Session for " + casParams + " expired")
          refreshSession(casParams).flatMap(requestWithCasSession(request, hotswap, retry = false))
        case r: Response[IO] => IO.pure(r)
      }
    }

    def isRedirectToLogin(resp: Response[IO]): Boolean =
      resp.headers.get(CIString("Location")).exists(_.exists(header =>
        header.value.contains("/cas/login") || header.value.contains("/cas-oppija/login")
      ))

    def sessionExpired(resp: Response[IO]): Boolean =
      isRedirectToLogin(resp) || resp.status == Status.Unauthorized

    def getCasSession(params: CasParams): IO[SessionCookie] = {
      synchronized(sessions.get(params)) match {
        case None =>
          logger.debug(s"No existing $sessionCookieName found for " + params + ", creating new")
          refreshSession(params)
        case Some(session) =>
          IO.pure(session)
      }
    }

    def refreshSession(params: CasParams): IO[SessionCookie] = {
      casClient.fetchCasSession(params, sessionCookieName).map { session =>
        logger.debug("Storing new session for " + params)
        synchronized(sessions.put(params, session))
        session
      }
    }

    Client { req =>
      Hotswap.create[IO, Response[IO]].flatMap { hotswap =>
        Resource.eval(openWithCasSession(req, hotswap))
      }
    }
  }
}
