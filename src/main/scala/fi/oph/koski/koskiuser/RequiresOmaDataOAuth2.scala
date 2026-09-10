package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

trait RequiresOmaDataOAuth2 extends KoskiLuovutuspalveluHeaderAuthenticationSupport {
  private val candidatesAttribute = "omaDataOAuth2Candidates"
  private val sessionsAttribute = "omaDataOAuth2Sessions"

  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.getOrElse(
    throw new IllegalStateException("OmaData OAuth2 -istuntoa ei ole kiinnitetty client_id:hen")
  )

  private def candidates: Either[HttpStatus, Seq[AuthenticationUser]] =
    Option(request.getAttribute(candidatesAttribute).asInstanceOf[Either[HttpStatus, Seq[AuthenticationUser]]]) match {
      case Some(result) => result
      case None =>
        val result = authenticateUserCandidates
        request.setAttribute(candidatesAttribute, result)
        result
    }

  def omaDataOAuth2Sessions: Seq[KoskiSpecificSession] =
    Option(request.getAttribute(sessionsAttribute).asInstanceOf[Seq[KoskiSpecificSession]]) match {
      case Some(sessions) => sessions
      case None =>
        val (withAccess, withoutAccess) = candidates.getOrElse(Nil).map(createSession).partition(_.hasSomeOmaDataOAuth2Access)
        withoutAccess.foreach(session =>
          defaultLogger.error(
            s"Luovutuspalvelu client certificate ${request.header("x-amzn-mtls-clientcert-subject").getOrElse("")} " +
              s"maps to user ${session.user.username} without OmaData OAuth2 access"
          )
        )
        request.setAttribute(sessionsAttribute, withAccess)
        withAccess
    }

  def pinOmaDataOAuth2Session(clientId: String): Option[KoskiSpecificSession] =
    omaDataOAuth2Sessions.find(_.user.username == clientId).map { session =>
      setUser(Right(session.user))
      session
    }

  before() {
    candidates match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (omaDataOAuth2Sessions.isEmpty) {
          haltWithStatus(KoskiErrorCategory.forbidden.vainOmaDataOAuth2())
        }
    }
  }
}
