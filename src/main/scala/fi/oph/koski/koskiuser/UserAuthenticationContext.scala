package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.sso.SSOTicketSessionRepository
import fi.vm.sade.security.ldap.DirectoryClient

trait UserAuthenticationContext {
  def directoryClient: DirectoryClient
  def käyttöoikeusRepository: KäyttöoikeusRepository
  def config: Config
  def serviceTicketRepository: SSOTicketSessionRepository
  def sessionTimeout: SessionTimeout
}
