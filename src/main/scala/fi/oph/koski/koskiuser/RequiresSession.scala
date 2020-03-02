package fi.oph.koski.koskiuser

trait RequiresSession extends AuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireSession
  }
}

