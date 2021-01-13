package fi.oph.common.koskiuser

trait HasKoskiSession {
  implicit def koskiSession: KoskiSession
}
