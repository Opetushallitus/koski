package fi.oph.koski.koskiuser

trait HasSession {
  implicit def koskiSession: Session
}

trait HasKoskiSpecificSession extends HasSession {
  implicit def koskiSession: KoskiSpecificSession
}
