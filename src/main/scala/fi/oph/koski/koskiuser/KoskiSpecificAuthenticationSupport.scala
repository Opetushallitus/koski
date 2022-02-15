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
        if (session.hasLuovutuspalveluAccess || session.hasTilastokeskusAccess || session.hasKelaAccess || session.hasYtlAccess || session.hasValviraAccess) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
        }
      case Left(error) =>
        haltWithStatus(error)
    }
  }

  def requirePalvelurooli(rooli: Palvelurooli) = {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasPalvelurooli(_ == rooli))) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }

  def createSession(user: AuthenticationUser): KoskiSpecificSession = KoskiSpecificSession(user, request, application.käyttöoikeusRepository)
}
