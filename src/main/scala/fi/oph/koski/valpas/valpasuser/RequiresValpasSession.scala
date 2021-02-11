package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiAuthenticationSupport, HasKoskiSession, KoskiSession}

trait RequiresValpasSession extends ValpasAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get
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
          koskiSessionOption.exists(isValpasSessionWithLogging)
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }

  def isValpasSession(session: KoskiSession): Boolean =
    session.orgKäyttöoikeudet
      .flatMap(_.organisaatiokohtaisetPalveluroolit)
      .intersect(Set(
        ValpasPalvelurooli(ValpasRooli.OPPILAITOS),
        ValpasPalvelurooli(ValpasRooli.KUNTA)))
      .nonEmpty

  def isValpasSessionWithLogging(session: KoskiSession): Boolean = {
    logger.info("session:" + session)
    val kaikkiKäyttöoikeudet = session.kaikkiKäyttöoikeudet
    logger.info("kaikkiKäyttöoikeudet:" + kaikkiKäyttöoikeudet)
    val orgKäyttöoikeudet = session.orgKäyttöoikeudet
    logger.info("orgKäyttöoikeudet:" + orgKäyttöoikeudet)
    orgKäyttöoikeudet
      .flatMap(_.organisaatiokohtaisetPalveluroolit)
      .intersect(Set(
        ValpasPalvelurooli(ValpasRooli.OPPILAITOS),
        ValpasPalvelurooli(ValpasRooli.KUNTA)))
      .nonEmpty
  }
}
