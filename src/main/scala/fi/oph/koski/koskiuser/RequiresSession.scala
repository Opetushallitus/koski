package fi.oph.koski.koskiuser

trait RequiresSession extends KoskiCookieAndBasicAuthenticationSupport with HasSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireSession
  }
}

