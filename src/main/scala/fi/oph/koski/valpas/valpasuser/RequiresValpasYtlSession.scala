package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.valpas.oppija.ValpasErrorCategory

trait RequiresValpasYtlSession extends ValpasAuthenticationSupport with HasValpasSession {
  implicit def session: ValpasSession = koskiSessionOption.get

  before() {
    requiresYtl()
  }

  private def requiresYtl(): Unit = {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasYtlAccess)) {
          haltWithStatus(ValpasErrorCategory.forbidden())
        }
    }
  }
}
