package fi.oph.koski.koskiuser

trait RequiresKansalainen extends AuthenticationSupport {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireKansalainen
  }
}

