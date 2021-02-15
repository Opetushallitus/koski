package fi.oph.koski.koskiuser

trait RequiresSession extends KoskiSpecificAuthenticationSupport with HasKoskiSpecificSession {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireSession
  }
}

