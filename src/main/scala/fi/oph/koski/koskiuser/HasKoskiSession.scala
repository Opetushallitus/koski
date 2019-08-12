package fi.oph.koski.koskiuser

trait HasKoskiSession {
  implicit def koskiSession: KoskiSession
}
