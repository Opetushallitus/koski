package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.vm.sade.security.ldap.DirectoryClient

trait UserAuthenticationContext {
  def directoryClient: DirectoryClient
  def käyttöoikeusRepository: KayttooikeusRepository
  def config: Config
  def serviceTicketRepository: CasTicketSessionRepository
  def sessionTimeout: SessionTimeout
}
