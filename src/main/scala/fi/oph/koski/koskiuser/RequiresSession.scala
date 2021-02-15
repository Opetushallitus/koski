package fi.oph.koski.koskiuser

trait RequiresSession extends KoskiSpecificAuthenticationSupport with HasSession {
  implicit def koskiSession: Session = koskiSessionOption.get

  before() {
    requireSession
  }
}

