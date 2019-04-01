package fi.oph.koski.koskiuser

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

trait RequiresLuovutuspalvelu extends AuthenticationSupport {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasLuovutuspalveluAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden.vainViranomainen())
        }
    }
  }
}
