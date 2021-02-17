package fi.oph.koski.koskiuser

trait HasKoskiSpecificSession {
  implicit def koskiSession: KoskiSpecificSession
}
