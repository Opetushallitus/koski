package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresTilastokeskus extends KoskiLuovutuspalveluHeaderAuthenticationSupport with HasKoskiSpecificSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresTilastokeskus
  }

  private def requiresTilastokeskus{
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasTilastokeskusAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden.vainTilastokeskus())
        }
    }
  }
}
