package fi.oph.koski.koskiuser

trait HasSession {
  implicit def session: Session
}

trait HasKoskiSpecificSession extends HasSession {
  implicit def session: KoskiSpecificSession
}
