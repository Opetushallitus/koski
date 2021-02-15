package fi.oph.koski.koskiuser

trait RequiresKansalainen extends KoskiAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireKansalainen
  }
}

