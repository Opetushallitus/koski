package fi.oph.koski.koskiuser

trait RequiresKansalainen extends KoskiSpecificAuthenticationSupport with HasKoskiSpecificSession {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireKansalainen
  }
}

