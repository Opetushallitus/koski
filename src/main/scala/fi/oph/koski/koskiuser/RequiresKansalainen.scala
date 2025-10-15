package fi.oph.koski.koskiuser

trait RequiresKansalainen extends KoskiCookieAndBasicAuthenticationSupport with HasKoskiSpecificSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireKansalainen
  }
}

