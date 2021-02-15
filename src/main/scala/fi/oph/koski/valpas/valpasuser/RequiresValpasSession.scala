package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiAuthenticationSupport, HasKoskiSession, KoskiSpecificSession}

trait RequiresValpasSession extends ValpasAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get
  def valpasSession = koskiSession

  before() {
    requireValpasSession
  }

  def requireValpasSession = {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(isValpasSession)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }

  def isValpasSession(session: KoskiSpecificSession): Boolean =
    session.orgKäyttöoikeudet
      .flatMap(_.organisaatiokohtaisetPalveluroolit)
      .intersect(Set(
        ValpasPalvelurooli(ValpasRooli.OPPILAITOS),
        ValpasPalvelurooli(ValpasRooli.KUNTA)))
      .nonEmpty
}
