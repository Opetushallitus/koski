package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.{AuthenticationSupport, AuthenticationUser}
import fi.oph.koski.valpas.servlet.ValpasBaseServlet
import fi.oph.koski.valpas.sso.ValpasSSOSupport

trait ValpasAuthenticationSupport extends AuthenticationSupport with ValpasBaseServlet with ValpasSSOSupport {
  def createSession(user: AuthenticationUser): ValpasSession = ValpasSession(user, request, application.käyttöoikeusRepository)

  override def koskiSessionOption: Option[ValpasSession] =
    getUser.toOption.map(createSession)
}
