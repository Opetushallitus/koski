package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.HasSession

trait HasValpasSession extends HasSession {
  implicit def koskiSession: ValpasSession
}
