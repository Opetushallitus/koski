package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.KoskiSpecificBaseServlet
import fi.oph.koski.sso.KoskiSpecificSSOSupport

trait KoskiSpecificAuthenticationSupport extends AuthenticationSupport with KoskiSpecificBaseServlet with KoskiSpecificSSOSupport {

  override def koskiSessionOption: Option[KoskiSpecificSession] =
    getUser.toOption.map(createSession)

  def requireVirkailijaOrPalvelukäyttäjä = {
    getUser match {
      case Right(user) if user.kansalainen =>
        haltWithStatus(KoskiErrorCategory.forbidden.vainVirkailija())
      case Right(user) =>
        val session = createSession(user)
        if (!session.hasPalvelurooli(_.palveluName == "KOSKI")) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu ilman Koski-palvelun käyttöoikeuksia"))
        }
        if (session.hasLuovutuspalveluAccess || session.hasTilastokeskusAccess || session.hasKelaAccess) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
        }
      case Left(error) =>
        haltWithStatus(error)
    }
  }

  def createSession(user: AuthenticationUser): KoskiSpecificSession = KoskiSpecificSession(user, request, application.käyttöoikeusRepository)
}
