package fi.oph.koski.koskiuser

trait RequiresSession extends KoskiSpecificAuthenticationSupport with HasSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireSession
  }
}

