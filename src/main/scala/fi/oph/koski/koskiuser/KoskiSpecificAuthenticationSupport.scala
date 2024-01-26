package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.KoskiSpecificBaseServlet
import fi.oph.koski.sso.KoskiSpecificSSOSupport

trait KoskiSpecificAuthenticationSupport extends AuthenticationSupport with KoskiSpecificBaseServlet with KoskiSpecificSSOSupport {

  override def koskiSessionOption: Option[KoskiSpecificSession] =
    getUser.toOption.map(createSession)

  def requireVirkailijaOrPalvelukäyttäjä: AuthenticationUser = {
    getUser match {
      case Right(user) if user.kansalainen =>
        haltWithStatus(KoskiErrorCategory.forbidden.vainVirkailija())
      case Right(user) =>
        val session = createSession(user)
        if (!session.hasPalvelurooli(_.palveluName == "KOSKI")) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu ilman Koski-palvelun käyttöoikeuksia"))
        }
        if (session.hasHSLAccess || session.hasSuomiFiAccess || session.hasTilastokeskusAccess || session.hasKelaAccess || session.hasYtlAccess || session.hasValviraAccess || session.hasMigriAccess) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
        }
        user
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

  def requireMuokkausoikeus(): Unit = {
    if (!createSession(requireVirkailijaOrPalvelukäyttäjä).hasAnyWriteAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu ilman muokkausoikeuksia"))
    }
  }
}
