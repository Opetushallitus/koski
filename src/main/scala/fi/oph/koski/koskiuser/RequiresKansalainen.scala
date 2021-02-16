package fi.oph.koski.koskiuser

trait RequiresKansalainen extends KoskiSpecificAuthenticationSupport with HasKoskiSpecificSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireKansalainen
  }
}

