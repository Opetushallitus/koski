package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresValpasKansalainenSession extends ValpasAuthenticationSupport with HasValpasSession {
  implicit def session: ValpasSession = koskiSessionOption.get

  before() {
    requireValpasKansalainenSession
  }

  def requireValpasKansalainenSession = {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case Right(user) =>
        if (!user.kansalainen) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
        }
    }
  }
}
