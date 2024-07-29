package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresHakemuspalvelu extends KoskiSpecificAuthenticationSupport {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresHakemuspalvelu
  }

  private def requiresHakemuspalvelu {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasHakemuspalveluAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
