package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{HasSession, KoskiSpecificAuthenticationSupport, KoskiSpecificSession, Palvelurooli, Session}

trait RequiresValpasSession extends ValpasAuthenticationSupport with HasValpasSession {
  implicit def session: ValpasSession = koskiSessionOption.get

  before() {
    requireValpasSession
  }

  def requireValpasSession = {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(isValpasSession)) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
        }
    }
  }

  def isValpasSession(session: ValpasSession): Boolean =
    hasValpasPalvelurooli(session.orgKäyttöoikeudet.flatMap(_.organisaatiokohtaisetPalveluroolit)) ||
    hasValpasPalvelurooli(session.globalKäyttöoikeudet.flatMap(_.globalPalveluroolit))

  private def hasValpasPalvelurooli(palveluroolit: Set[Palvelurooli]): Boolean =
    palveluroolit.exists(hyväksytytValpasPalveluroolit.contains)

  private val hyväksytytValpasPalveluroolit: List[Palvelurooli] = List(
    ValpasPalvelurooli(ValpasRooli.OPPILAITOS_HAKEUTUMINEN),
    ValpasPalvelurooli(ValpasRooli.OPPILAITOS_SUORITTAMINEN),
    ValpasPalvelurooli(ValpasRooli.OPPILAITOS_MAKSUTTOMUUS),
    ValpasPalvelurooli(ValpasRooli.KUNTA)
  )
}
