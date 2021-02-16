package fi.oph.koski.koskiuser

trait RequiresSession extends KoskiSpecificAuthenticationSupport with HasSession {
  implicit def session: Session = koskiSessionOption.get

  before() {
    requireSession
  }
}

