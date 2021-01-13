package fi.oph.common.koskiuser

trait RequiresKansalainen extends AuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireKansalainen
  }
}

