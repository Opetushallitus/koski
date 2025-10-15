package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

trait RequiresKela extends KoskiSpecificAuthenticationSupport with CookieAndBasicAuthAuthenticationSupport
  with LuovutuspalveluHeaderAuthenticationSupport with HasKoskiSpecificSession {

  // TODO Poista Kela UI
  override def authenticateUser: Either[HttpStatus, AuthenticationUser] = {
    super[CookieAndBasicAuthAuthenticationSupport].authenticateUser
      .left.flatMap(_ => super[LuovutuspalveluHeaderAuthenticationSupport].authenticateUser)
  }

  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresKela
  }

  private def requiresKela {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasKelaAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
