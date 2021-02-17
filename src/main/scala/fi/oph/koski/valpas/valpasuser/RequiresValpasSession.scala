package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{HasSession, KoskiSpecificAuthenticationSupport, Session, KoskiSpecificSession}

trait RequiresValpasSession extends ValpasAuthenticationSupport with HasValpasSession {
  implicit def session: ValpasSession = koskiSessionOption.get
  def valpasSession = session

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

  def isValpasSession(session: Session): Boolean =
    session.orgKäyttöoikeudet
      .flatMap(_.organisaatiokohtaisetPalveluroolit)
      .intersect(Set(
        ValpasPalvelurooli(ValpasRooli.OPPILAITOS_HAKEUTUMINEN),
        ValpasPalvelurooli(ValpasRooli.OPPILAITOS_SUORITTAMINEN),
        ValpasPalvelurooli(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS),
        ValpasPalvelurooli(ValpasRooli.KUNTA)))
      .nonEmpty
}
