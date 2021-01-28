package fi.oph.koski.koskiuser

trait RequiresKansalainen extends KoskiAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireKansalainen
  }
}

