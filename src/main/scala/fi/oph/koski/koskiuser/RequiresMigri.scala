package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresMigri extends KoskiSpecificAuthenticationSupport {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresMigri
  }

  private def requiresMigri {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasMigriAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
