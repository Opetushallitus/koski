package fi.oph.koski.koskiuser

trait RequiresSession extends KoskiAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireSession
  }
}

